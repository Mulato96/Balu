package com.gal.afiliaciones.infrastructure.security;

import com.gal.afiliaciones.config.util.SiarpProperties;
import com.gal.afiliaciones.infrastructure.client.generic.TokenClientResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SiarpTokenService {

    private final WebClient coreWebClient;
    private final SiarpProperties properties;

    private final Object lock = new Object();
    private String cachedAccessToken;
    private Instant expirationTime = Instant.EPOCH;

    private WebClient clientWithAuthFilter;

    @PostConstruct
    public void init() {
        // Use telemetry-enabled coreWebClient and add SIARP auth filter
        clientWithAuthFilter = coreWebClient.mutate()
                .filter(this::addTokenIfTargetMatches)
                .build();
    }

    public <T> Mono<List<T>> getList(String url, ParameterizedTypeReference<List<T>> typeRef) {
        return clientWithAuthFilter
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(typeRef)
                .defaultIfEmpty(List.of());
    }

    public <T, R> Mono<R> exchange(HttpMethod method, String url, T requestBody, Class<R> responseType) {
        return clientWithAuthFilter
                .method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType);
    }

    public Mono<String> getRaw(String url, Map<String, String> headers) {
        return clientWithAuthFilter
                .get()
                .uri(url)
                .headers(h -> {
                    h.setAccept(List.of(MediaType.APPLICATION_JSON));
                    if (headers != null) headers.forEach(h::set);
                })
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, ex -> {
                    // Return the raw error response body instead of throwing exception
                    return Mono.just(ex.getResponseBodyAsString());
                });
    }

    private Mono<ClientResponse> addTokenIfTargetMatches(ClientRequest request, ExchangeFunction next) {
        if (request.url().toString().startsWith(properties.getTargetPrefix())) {
            return Mono.fromCallable(this::getToken)
                    .flatMap(token -> {
                        ClientRequest newRequest = ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(token))
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

            WebClient tokenClient = WebClient.builder().build();

            Map<String, String> form = Map.of(
                    "grant_type", "client_credentials",
                    "client_id", properties.getClientId(),
                    "client_secret", properties.getClientSecret()
            );

            TokenClientResponse response = tokenClient.post()
                    .uri(properties.getTokenUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue("grant_type=" + form.get("grant_type") +
                            "&client_id=" + form.get("client_id") +
                            "&client_secret=" + form.get("client_secret"))
                    .retrieve()
                    .bodyToMono(TokenClientResponse.class)
                    .block();

            if (response != null && response.getAccess_token() != null) {
                cachedAccessToken = response.getAccess_token();
                long expiresIn = response.getExpires_in() != null ? response.getExpires_in() - 10 : 300;
                expirationTime = Instant.now().plusSeconds(expiresIn);
            } else {
                throw new IllegalStateException("No se pudo obtener el token de acceso (SIARP).");
            }

            return cachedAccessToken;
        }
    }

    // Diagnostics
    public String getAccessToken() {
        return getToken();
    }

    public long getSecondsRemaining() {
        synchronized (lock) {
            long remaining = expirationTime.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(0, remaining);
        }
    }
}


