package com.gal.afiliaciones.infrastructure.client.generic.branch;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Branch (Sucursales) Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultBranchRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> consultRaw(String idTipoDoc, String idEmpresa) {
        String url = properties.getBranchConsultUrl()
                + "?idTipoDoc=" + idTipoDoc
                + "&idEmpresa=" + idEmpresa;
        
        log.info("Calling consult branch endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

