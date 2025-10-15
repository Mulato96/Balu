package com.gal.afiliaciones.infrastructure.client.generic.sat;

import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @deprecated Use {@link com.gal.afiliaciones.infrastructure.client.generic.sat.v2.SatConsultTransferableEmployerClientV2} instead.
 * This version lacks automatic telemetry tracking. Migrate to v2 for full integration monitoring.
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class SatConsultTransferableEmployerClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    private final ObjectMapper objectMapper;

    public TransferableEmployerResponse consult(TransferableEmployerRequest request) {
        try {
            String raw = busTokenService
                    .exchange(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request, String.class)
                    .block();
            return parseOrEmpty(raw);
        } catch (WebClientResponseException ex) {
            // On HTTP error, translate 5xx, else bubble up
            if (ex.getStatusCode().is5xxServerError()) {
                throw new SatUpstreamError("El servicio del SAT no se encuentra disponible en este momento. Por favor, intenta nuevamente más tarde.");
            }
            throw ex;
        }
    }

    public String consultRaw(TransferableEmployerRequest request) {
        try {
            return busTokenService
                    .exchange(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request, String.class)
                    .onErrorResume(Mono::error)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                throw new SatUpstreamError("El servicio del SAT no se encuentra disponible en este momento. Por favor, intenta nuevamente más tarde.");
            }
            throw ex;
        }
    }

    private TransferableEmployerResponse parseOrEmpty(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            // Validate expected success structure strictly
            boolean hasSuccessShape =
                    node.has("codigoARL") &&
                    node.has("tipoDocumentoEmpleador") &&
                    node.has("numeroDocumentoEmpleador") &&
                    node.has("consecutivoNITEmpleador") &&
                    node.has("empresaTrasladable") &&
                    node.has("causal") &&
                    node.has("arlAfiliacion");
            if (!hasSuccessShape) {
                throw new SatUpstreamError("Respuesta inesperada del SAT");
            }
            // Otherwise, parse as success DTO
            return objectMapper.readValue(raw, TransferableEmployerResponse.class);
        } catch (Exception parseEx) {
            return TransferableEmployerResponse.builder().build();
        }
    }
}


