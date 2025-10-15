package com.gal.afiliaciones.infrastructure.security;

import java.time.Instant;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.infrastructure.client.generic.TokenClientResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class BusTokenService {

    private final WebClient coreWebClient;

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.target-url}")
    private String targetUrl;

    private final Object lock = new Object();
    private String cachedAccessToken;
    private Instant expirationTime = Instant.EPOCH;

    private static final String TARGET_URL = "https://core-positiva-apis-pre-apicast-staging.apps.openshift4.positiva.gov.co";

    private WebClient clientWithAuthFilter;

    @PostConstruct
    public void init() {
        // Use the configured coreWebClient with telemetry interceptor
        clientWithAuthFilter = coreWebClient.mutate()
                .filter(this::addTokenIfTargetMatches)
                .build();
    }

    public <T> Mono<List<T>> getList(String url, ParameterizedTypeReference<List<T>> typeRef) {
        return clientWithAuthFilter
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(typeRef)
                .defaultIfEmpty(List.of());
    }

    public Mono<String> getRaw(String url) {
        return clientWithAuthFilter
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, ex -> {
                    // Return the raw error response body instead of throwing exception
                    log.debug("Raw GET client received error response: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return reactor.core.publisher.Mono.just(ex.getResponseBodyAsString());
                })
                .defaultIfEmpty("{}");
    }

    private Mono<ClientResponse> addTokenIfTargetMatches(ClientRequest request, ExchangeFunction next) {
        if (request.url().toString().startsWith(targetUrl)) {
            return Mono.fromCallable(this::getToken)
                    .flatMap(token -> {
                        ClientRequest newRequest = ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(token))
                                .body(request.body()) // Explicitly preserve body
                                .attributes(attrs -> attrs.putAll(request.attributes())) // Explicitly preserve telemetry attributes
                                .build();
                        return next.exchange(newRequest);
                    });
        }
        return next.exchange(request);
    }

    private String getToken() {
        synchronized (lock) {
            if (cachedAccessToken != null && Instant.now().isBefore(expirationTime)) {
                return cachedAccessToken;
            }

            // Use a separate WebClient for token requests (no telemetry needed for auth)
            WebClient tokenClient = WebClient.builder().build();

            TokenClientResponse response = tokenClient.post()
                    .uri(tokenUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                    .retrieve()
                    .bodyToMono(TokenClientResponse.class)
                    .block();

            if (response != null && response.getAccess_token() != null) {
                cachedAccessToken = response.getAccess_token();
                long expiresIn = response.getExpires_in() != null ? response.getExpires_in() - 10 : 300;
                expirationTime = Instant.now().plusSeconds(expiresIn);
            } else {
                throw new RuntimeException("No se pudo obtener el token de acceso.");
            }

            return cachedAccessToken;
        }
    }
    
    public <T, R> Mono<R> exchange(HttpMethod method, String url, T requestBody, Class<R> responseType) {
        return clientWithAuthFilter
                .method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(m -> m.put("telemetryRequestBody", requestBody))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Mono<String> exchangeRaw(HttpMethod method, String url, T requestBody) {
        return clientWithAuthFilter
                .method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(m -> m.put("telemetryRequestBody", requestBody))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, ex -> {
                    // Return the raw error response body instead of throwing exception
                    log.debug("Raw client received error response: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return reactor.core.publisher.Mono.just(ex.getResponseBodyAsString());
                })
                .defaultIfEmpty("{}");
    }
}
