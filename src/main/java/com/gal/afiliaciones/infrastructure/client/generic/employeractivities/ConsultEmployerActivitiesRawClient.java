package com.gal.afiliaciones.infrastructure.client.generic.employeractivities;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Employer Activities Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultEmployerActivitiesRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> consultRaw(String idTipoDoc, String idEmpresa, Integer idSubEmpresa) {
        String url = properties.getEmployerActivitiesConsultUrl()
                + "?idTipoDoc=" + idTipoDoc
                + "&idEmpresa=" + idEmpresa
                + "&idSubEmpresa=" + idSubEmpresa;
        
        log.info("Calling consult employer activities endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

