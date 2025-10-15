package com.gal.afiliaciones.infrastructure.service;

import com.gal.afiliaciones.infrastructure.client.confecamaras.ConfecamarasClient;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.ConfecamarasErrorDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfecamarasConsultationService {

    private final ConfecamarasClient confecamarasClient;

    /**
     * Consult company information from Confecamaras
     * @param nit Company NIT to consult
     * @param dv Verification digit
     * @return Confecamaras response with company data
     */
    public Mono<RecordResponseDTO> consultCompany(String nit, String dv) {
        log.info("Starting Confecamaras consultation for NIT: {} DV: {}", nit, dv);

        return confecamarasClient.consultCompany(nit, dv)
                .doOnSuccess(response -> {
                    if (response.getError() != null) {
                        log.warn("Confecamaras consultation returned error for NIT {}: {} - {}", 
                                nit, response.getError().getCode(), response.getError().getMessage());
                    } else {
                        log.info("Confecamaras consultation completed successfully for NIT: {}", nit);
                        if (response.getRegistros() != null) {
                            log.info("Found {} company records for NIT: {}", response.getRegistros().size(), nit);
                        }
                    }
                })
                .doOnError(error -> log.error("Error in Confecamaras consultation for NIT {}: {}", nit, error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Confecamaras consultation failed for NIT {}: {}", nit, error.getMessage());
                    
                    // Create error response for timeout or connection errors
                    RecordResponseDTO errorResponse = new RecordResponseDTO();
                    errorResponse.setNit(nit);
                    errorResponse.setDv(dv);
                    errorResponse.setFecha_respuesta(java.time.LocalDate.now().toString());
                    errorResponse.setHora_respuesta(java.time.LocalTime.now().toString());
                    
                    ConfecamarasErrorDTO errorDto = new ConfecamarasErrorDTO();
                    if (error.getMessage().contains("timeout")) {
                        errorDto.setCode("TIMEOUT");
                        errorDto.setMessage("Timeout al consultar el servicio de Confecamaras. Intente más tarde.");
                    } else if (error.getMessage().contains("Connection refused") || error.getMessage().contains("UnknownHostException")) {
                        errorDto.setCode("CONNECTION_ERROR");
                        errorDto.setMessage("Error de conexión con el servicio de Confecamaras.");
                    } else {
                        errorDto.setCode("SERVICE_ERROR");
                        errorDto.setMessage("Error interno del servicio: " + error.getMessage());
                    }
                    errorResponse.setError(errorDto);
                    
                    return Mono.just(errorResponse);
                });
    }
}
