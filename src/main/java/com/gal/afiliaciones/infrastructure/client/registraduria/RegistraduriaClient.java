package com.gal.afiliaciones.infrastructure.client.registraduria;

import com.gal.afiliaciones.infrastructure.service.RegistraduriaKeycloakTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistraduriaClient {

    @Qualifier("registraduriaWebClient")
    private final WebClient webClient;

    private final RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService;

    @Value("${registraduria.soap.url}")
    private String registraduriaSoapUrl;

    @Value("${registraduria.soap.action}")
    private String registraduriaSoapAction;

    /**
     * Consult identity card information from Registraduria SOAP service
     * @param documentNumber Document number to consult
     * @return SOAP response as string
     */
    public Mono<String> consultIdentityCard(String documentNumber) {
        log.info("Calling Registraduria SOAP service for document: {} at URL: {}", documentNumber, registraduriaSoapUrl);

        try {
            // Get access token synchronously using the specific service
            String token = registraduriaKeycloakTokenService.getAccessToken();

            String soapRequest = buildSoapRequest(documentNumber);

            return webClient
                    .post()
                    .uri(registraduriaSoapUrl)
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .header("SOAPAction", registraduriaSoapAction)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(soapRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(response -> log.info("Registraduria SOAP call successful for document: {}", documentNumber))
                    .doOnError(error -> {
                        String errorMessage = getErrorMessage(error);
                        log.error("Error calling Registraduria SOAP service for document {}: {}", documentNumber, errorMessage);
                    })
                    .onErrorMap(this::mapError);

        } catch (Exception e) {
            log.error("Error obtaining access token for document {}: {}", documentNumber, e.getMessage());
            return Mono.error(new RuntimeException("Error obtaining access token: " + e.getMessage(), e));
        }
    }

    /**
     * Build SOAP request XML for identity card consultation
     * Using the correct format based on the user's example
     */
    private String buildSoapRequest(String documentNumber) {
        return String.format("""
                <solicitudConsultaEstadoConsulta>
                    <nuip>%s</nuip>
                </solicitudConsultaEstadoConsulta>
                """, documentNumber);
    }

    /**
     * Get user-friendly error message
     */
    private String getErrorMessage(Throwable error) {
        if (error instanceof UnknownHostException) {
            return String.format("Cannot resolve host '%s'. Please check the URL configuration or network connectivity.",
                    extractHostFromUrl(registraduriaSoapUrl));
        } else if (error.getMessage().contains("Failed to resolve")) {
            return String.format("DNS resolution failed for '%s'. The service URL may be incorrect or the service is not available.",
                    extractHostFromUrl(registraduriaSoapUrl));
        } else if (error.getMessage().contains("Connection refused")) {
            return "Connection refused. The service may be down or not accessible from this network.";
        } else if (error.getMessage().contains("timeout")) {
            return "Request timeout. The service is taking too long to respond.";
        } else if (error.getMessage().contains("401") || error.getMessage().contains("Unauthorized")) {
            return "Authentication failed. Please check the Registraduria Keycloak credentials and token.";
        } else if (error.getMessage().contains("403") || error.getMessage().contains("Forbidden")) {
            return "Access forbidden. Please check the service permissions.";
        } else {
            return error.getMessage();
        }
    }

    /**
     * Map errors to more specific exceptions
     */
    private Throwable mapError(Throwable error) {
        String errorMessage = getErrorMessage(error);
        return new RuntimeException("Registraduria service error: " + errorMessage, error);
    }

    /**
     * Extract host from URL
     */
    private String extractHostFromUrl(String url) {
        try {
            if (url.startsWith("http://")) {
                return url.substring(7).split("/")[0];
            } else if (url.startsWith("https://")) {
                return url.substring(8).split("/")[0];
            }
            return url.split("/")[0];
        } catch (Exception e) {
            return url;
        }
    }

}