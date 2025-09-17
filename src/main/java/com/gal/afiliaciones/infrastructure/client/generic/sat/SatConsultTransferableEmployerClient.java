package com.gal.afiliaciones.infrastructure.client.generic.sat;

import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.application.service.positiva.PositivaLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SatConsultTransferableEmployerClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    private final PositivaLogService positivaLogService;
    private final ObjectMapper objectMapper;
    private static final String SERVICE_NAME = "SatConsultTransferableEmployerClient";
    private static final String FIELD_RESULTADO = "resultado";
    private static final String FIELD_MENSAJE = "mensaje";
    private static final String FIELD_CODIGO = "codigo";

    public TransferableEmployerResponse consult(TransferableEmployerRequest request) {
        try {
            String raw = busTokenService
                    .exchange(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request, String.class)
                    .block();
            return handleTraceabilityOnSuccess(raw, request, "consultTransferableEmployer");
        } catch (WebClientResponseException ex) {
            handleHttpError(ex, request, "consultTransferableEmployer");
            if (ex.getStatusCode().is5xxServerError()) {
                throw new SatUpstreamError("El servicio del SAT no se encuentra disponible en este momento. Por favor, intenta nuevamente más tarde.");
            }
            throw ex;
        }
    }

    public String consultRaw(TransferableEmployerRequest request) {
        try {
            String response = busTokenService
                    .exchange(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request, String.class)
                    .onErrorResume(Mono::error)
                    .block();
            // Trace based on content: error envelope vs success
            handleTraceabilityOnSuccess(response, request, "consultTransferableEmployerRaw");
            return response;
        } catch (WebClientResponseException ex) {
            handleHttpError(ex, request, "consultTransferableEmployerRaw");
            if (ex.getStatusCode().is5xxServerError()) {
                throw new SatUpstreamError("El servicio del SAT no se encuentra disponible en este momento. Por favor, intenta nuevamente más tarde.");
            }
            throw ex;
        }
    }

    private TransferableEmployerResponse handleTraceabilityOnSuccess(String raw,
                                                                     TransferableEmployerRequest request,
                                                                     String operation) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            // If service returns error envelope with resultado/mensaje/codigo
            if (node.has(FIELD_RESULTADO) && node.has(FIELD_MENSAJE) && node.has(FIELD_CODIGO)) {
                Integer result = node.path(FIELD_RESULTADO).isInt() ? node.path(FIELD_RESULTADO).asInt() : null;
                String code = node.path(FIELD_CODIGO).asText(null);
                String message = node.path(FIELD_MENSAJE).asText(null);
                positivaLogService.save(
                        SERVICE_NAME,
                        operation,
                        request.getTipoDocumentoEmpleador(),
                        request.getNumeroDocumentoEmpleador(),
                        null,
                        null,
                        null,
                        null,
                        code,
                        message,
                        raw,
                        result != null ? result : -1
                );
                // Return an empty DTO to keep signature; caller decides handling
                return TransferableEmployerResponse.builder().build();
            }
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
                // Unexpected 2xx shape → persist and raise upstream error
                positivaLogService.save(
                        SERVICE_NAME,
                        operation,
                        request.getTipoDocumentoEmpleador(),
                        request.getNumeroDocumentoEmpleador(),
                        null,
                        null,
                        null,
                        null,
                        "UNEXPECTED_200_SHAPE",
                        "Respuesta inesperada del SAT",
                        raw,
                        200
                );
                throw new SatUpstreamError("Respuesta inesperada del SAT");
            }
            // Otherwise, parse as success DTO
            TransferableEmployerResponse dto = objectMapper.readValue(raw, TransferableEmployerResponse.class);
            positivaLogService.saveFromResponse(
                    SERVICE_NAME,
                    operation,
                    request.getTipoDocumentoEmpleador(),
                    request.getNumeroDocumentoEmpleador(),
                    null,
                    null,
                    null,
                    null,
                    dto
            );
            return dto;
        } catch (Exception parseEx) {
            // Fallback: store raw response body as error with resultCode = -1
            positivaLogService.save(
                    SERVICE_NAME,
                    operation,
                    request.getTipoDocumentoEmpleador(),
                    request.getNumeroDocumentoEmpleador(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    parseEx.getMessage(),
                    raw,
                    -1
            );
            return TransferableEmployerResponse.builder().build();
        }
    }

    private void handleHttpError(WebClientResponseException ex,
                                 TransferableEmployerRequest request,
                                 String operation) {
        String body = ex.getResponseBodyAsString();
        try {
            JsonNode node = objectMapper.readTree(body);
            String code = node.path(FIELD_CODIGO).asText(null);
            String message = node.path(FIELD_MENSAJE).asText(null);
            Integer result = node.has(FIELD_RESULTADO) && node.path(FIELD_RESULTADO).isInt() ? node.path(FIELD_RESULTADO).asInt() : null;
            positivaLogService.save(
                    SERVICE_NAME,
                    operation,
                    request.getTipoDocumentoEmpleador(),
                    request.getNumeroDocumentoEmpleador(),
                    null,
                    null,
                    null,
                    null,
                    code != null ? code : String.valueOf(ex.getStatusCode().value()),
                    message != null ? message : ex.getMessage(),
                    body,
                    result != null ? result : ex.getStatusCode().value()
            );
        } catch (Exception ignore) {
            positivaLogService.saveError(
                    SERVICE_NAME,
                    operation,
                    request.getTipoDocumentoEmpleador(),
                    request.getNumeroDocumentoEmpleador(),
                    null,
                    null,
                    null,
                    null,
                    String.valueOf(ex.getStatusCode().value()),
                    body
            );
        }
    }
}


