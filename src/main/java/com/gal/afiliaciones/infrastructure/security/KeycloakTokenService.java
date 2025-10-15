package com.gal.afiliaciones.infrastructure.security;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class KeycloakTokenService {

    @Value("${keycloak.SERVER.URL}")
    private String authServerUrl;

    @Value("${keycloak.REALM.NAME}")
    private String realm;

    @Value("${keycloak.resource.service}")
    private String clientId;

    @Value("${keycloak.credentials.secret.service}")
    private String clientSecret;

    private String cachedAccessToken;
    private Instant expirationTime = Instant.now();

    private final WebClient webClient;

    public KeycloakTokenService(WebClient.Builder builder) {
        ConnectionProvider connectionProvider=ConnectionProvider.builder("shared-pool")
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofHours(2))
                .evictInBackground(Duration.ofSeconds(30))   // ≤ idle
                .metrics(true)
                .build();
        HttpClient client=HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)
                .responseTimeout(Duration.ofSeconds(30));


        this.webClient = builder.clientConnector(new ReactorClientHttpConnector(client)).build();
    }

    public synchronized String getAccessToken() {
        // Si el token existe y aún no caduca, devuélvelo
        if (cachedAccessToken != null && Instant.now().isBefore(expirationTime)) {
            return cachedAccessToken;
        }

        String tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

        Map<String, Object> response =
                webClient.post()
                        .uri(tokenEndpoint)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        if (response != null) {
            String newToken = (String) response.get("access_token");
            Integer expiresIn = (Integer) response.get("expires_in");
            if (newToken != null && expiresIn != null) {
                cachedAccessToken = newToken;
                // Estimamos hora de expiración (restamos unos segundos por seguridad)
                expirationTime = Instant.now().plusSeconds(expiresIn - 10);
                return cachedAccessToken;
            }
        }
        throw new IllegalStateException("No se pudo obtener el token de Keycloak ");
    }
}
