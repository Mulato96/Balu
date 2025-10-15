package com.gal.afiliaciones.infrastructure.client.generic.sat.v2;

import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * SAT Transferable Employer Client v2 with enhanced telemetry.
 * Part of integrations v2 architecture.
 * 
 * This version demonstrates the power of integrations v2:
 * - NO manual logging code needed
 * - NO PositivaLogService dependency  
 * - HTTP tracking happens automatically via WebClient interceptor
 * - Clean, focused business logic only
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SatConsultTransferableEmployerClientV2 {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    /**
     * Consults SAT for transferable employer information.
     * 
     * HTTP call tracking happens automatically via WebClient interceptor.
     * No manual logging code needed!
     * 
     * @param request The transferable employer request
     * @return TransferableEmployerResponse with SAT data
     * @throws SatUpstreamError if SAT service is unavailable
     */
    public TransferableEmployerResponse consult(TransferableEmployerRequest request) {
        log.info("SAT transferable employer consultation: tipoDoc={}, numeroDoc={}, consecutivo={}", 
            request.getTipoDocumentoEmpleador(), 
            request.getNumeroDocumentoEmpleador(), 
            request.getConsecutivoNITEmpleador());
        
        try {
            // Just make the call - tracking happens automatically!
            TransferableEmployerResponse response = busTokenService
                .exchange(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request, TransferableEmployerResponse.class)
                .block();
            
            log.info("SAT consultation successful: causal={}, empresaTrasladable={}, codigoARL={}", 
                response != null ? response.getCausal() : null,
                response != null ? response.getEmpresaTrasladable() : null, 
                response != null ? response.getCodigoArl() : null);
            
            return response;
            
        } catch (WebClientResponseException ex) {
            // HTTP error details automatically captured by interceptor
            log.error("SAT consultation failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
            
            if (ex.getStatusCode().is5xxServerError()) {
                throw new SatUpstreamError("El servicio del SAT no se encuentra disponible en este momento. Por favor, intenta nuevamente más tarde.");
            }
            
            throw ex;
        }
    }

    /**
     * Returns raw SAT response for debugging.
     * HTTP tracking still happens automatically.
     * Returns raw error responses instead of throwing exceptions.
     */
    public String consultRaw(TransferableEmployerRequest request) {
        log.debug("SAT raw consultation: {}", request);
        
        // Use exchangeRaw to get raw error responses without throwing exceptions
        return busTokenService
            .exchangeRaw(HttpMethod.POST, properties.getSatConsultTransferableEmployerUrl(), request)
            .block();
    }
}
