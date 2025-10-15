package com.gal.afiliaciones.config.webclient;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gal.afiliaciones.infrastructure.interceptor.WebClientTelemetryInterceptor;
import com.gal.afiliaciones.infrastructure.security.KeycloakTokenService;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig implements WebMvcConfigurer {

    @Value("${kong.balance.transversal-url}")
    private String baseUrl;

    private final KeycloakTokenService keycloakTokenService;
    private final WebClientTelemetryInterceptor telemetryInterceptor;



    /** pool compartido para TODO el microservicio */
    @Bean
    public ConnectionProvider sharedPool() {
        return ConnectionProvider.builder("shared-pool")
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofHours(2))
                .evictInBackground(Duration.ofSeconds(30))   // ≤ idle
                .metrics(true)
                .build();
    }

    /** HttpClient con timeouts coherentes */
    @Bean
    public HttpClient httpClient(ConnectionProvider sharedPool) {
        return HttpClient.create(sharedPool)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60_000)
                .responseTimeout(Duration.ofSeconds(60));
    }

    /** WebClient principal que usan TODOS los clientes internos */
    @Bean("coreWebClient")      // nombre explícito
    @Primary                    // será el webClient por defecto
    public WebClient coreWebClient(HttpClient httpClient) {

        int size = 20 * 1024 * 1024;
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(size))
                .build();

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(addAuthorizationHeader())          // token no bloqueante
                .filter(telemetryInterceptor.telemetryFilter()) // integrations v2 telemetry
                .filters(exchangeFilterFunctions -> exchangeFilterFunctions.add(logRequest()))
                .exchangeStrategies(strategies)
                .build();
        
        log.info("🔧 Created coreWebClient with telemetry interceptor enabled");
        return webClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /** WebClient específico para Confecamaras */
    @Bean("confecamarasWebClient")
    public WebClient confecamarasWebClient(HttpClient httpClient) {
        int size = 20 * 1024 * 1024;
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(telemetryInterceptor.telemetryFilter()) // integrations v2 telemetry
                .filters(exchangeFilterFunctions -> exchangeFilterFunctions.add(logRequest()))
                .exchangeStrategies(strategies)
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return (request, next) -> {
            log.debug("Request: ".concat(request.method().name()).concat(" ").concat(request.url().toString()));
            request.headers().forEach(
                    (name, values) -> values.forEach(value -> log.debug(name.concat(": ").concat(value))));
            return next.exchange(request);
        };
    }

    private ExchangeFilterFunction addAuthorizationHeader() {
        return (request, next) -> {
            if (request.url().toString().startsWith(baseUrl)) {
                String token = keycloakTokenService.getAccessToken();
                return next.exchange(
                        ClientRequest.from(request)
                                .header("Authorization", "Bearer ".concat(token))
                                .attributes(attrs -> attrs.putAll(request.attributes())) // Explicitly preserve telemetry attributes
                                .build()
                );
            }
            return next.exchange(request);
        };
    }

}