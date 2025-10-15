package com.gal.afiliaciones.infrastructure.client.generic.employeractivation;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Consult Employer Activation Client with automatic telemetry.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultEmployerActivationClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<Object>> consult(String idTipoDoc, String idEmpresa, Integer idSubEmpresa) {
        String url = properties.getEmployerActivationConsultUrl()
                + "?idTipoDoc=" + idTipoDoc
                + "&idEmpresa=" + idEmpresa
                + "&idSubEmpresa=" + idSubEmpresa;
        
        log.info("Calling consult employer activation endpoint: {}", url);
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}

