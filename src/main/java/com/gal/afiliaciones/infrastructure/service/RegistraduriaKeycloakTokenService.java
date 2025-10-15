package com.gal.afiliaciones.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistraduriaKeycloakTokenService {

    private final WebClient.Builder webClientBuilder;

    @Value("${registraduria.keycloak.server.url}")
    private String authServerUrl;

    @Value("${registraduria.keycloak.realm.name}")
    private String realm;

    @Value("${registraduria.keycloak.client.id}")
    private String clientId;

    @Value("${registraduria.keycloak.client.secret}")
    private String clientSecret;

    private String cachedAccessToken;
    private Instant expirationTime = Instant.now();

    /**
     * Get access token for Registraduria service
     * @return Access token string
     */
    public String getAccessToken() {
        // If token exists and hasn't expired, return it
        if (cachedAccessToken != null && Instant.now().isBefore(expirationTime)) {
            log.debug("Returning cached access token for Registraduria");
            return cachedAccessToken;
        }

        log.info("Requesting new access token from Keycloak for Registraduria service");
        log.info("Keycloak URL: {}", authServerUrl);
        log.info("Realm: {}", realm);
        log.info("Client ID: {}", clientId);
        log.debug("Client Secret: {}...", clientSecret != null ? clientSecret.substring(0, Math.min(8, clientSecret.length())) : "null");

        String tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);
        log.info("Token endpoint: {}", tokenEndpoint);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        try {
            log.info("Sending token request to Keycloak...");


            log.info("Sending token request to Keycloak...");
            
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(resp -> log.info("Token response received from Keycloak"))
                    .doOnError(error -> log.error("Error in token request: {}", error.getMessage()))
                    .block();

            if (response != null) {
                log.info("Keycloak response keys: {}", response.keySet());

                
                String newToken = (String) response.get("access_token");
                Integer expiresIn = (Integer) response.get("expires_in");

                if (newToken != null && expiresIn != null) {
                    cachedAccessToken = newToken;
                    // Estimate expiration time (subtract a few seconds for safety)
                    expirationTime = Instant.now().plusSeconds(expiresIn - 10);

                    log.info("Access token obtained successfully for Registraduria service. Expires in: {} seconds", expiresIn);
                    return cachedAccessToken;
                } else {
                    log.error("Invalid response format from Keycloak. access_token: {}, expires_in: {}", 
                             newToken != null ? "present" : "null", expiresIn);
                    log.error("Full response: {}", response);
                    throw new IllegalStateException("Invalid response format from Keycloak server");
                }
            } else {
                log.error("Null response from Keycloak server");
                throw new IllegalStateException("Null response from Keycloak server");
            }
        } catch (Exception e) {
            log.error("Error obtaining access token for Registraduria service: {}", e.getMessage(), e);

            // Log specific error details
            if (e.getMessage().contains("timeout")) {
                log.error("Timeout connecting to Keycloak server: {}", tokenEndpoint);
            } else if (e.getMessage().contains("Connection refused")) {
                log.error("Connection refused to Keycloak server: {}", tokenEndpoint);
            } else if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                log.error("Unauthorized access to Keycloak. Check client_id and client_secret");
            } else if (e.getMessage().contains("404")) {
                log.error("Keycloak endpoint not found: {}", tokenEndpoint);
            }

            throw new RuntimeException("Failed to obtain access token for Registraduria service: " + e.getMessage(), e);
        }
    }

    /**
     * Clear cached token (useful for testing or when token is invalid)
     */
    public void clearCachedToken() {
        cachedAccessToken = null;
        expirationTime = Instant.now();
        log.debug("Cached access token cleared for Registraduria service");
    }
} 
