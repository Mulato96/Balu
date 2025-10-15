package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Legal Representative Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultLegalRepresentativeRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> consultRaw(String tipoDoc, String idEmpresa, Integer idSubEmpresa) {
        String url = properties.getLegalRepresentativeUrl()
                + "?idTipoDoc=" + tipoDoc
                + "&idEmpresa=" + idEmpresa
                + "&idSubEmpresa=" + idSubEmpresa;
        
        log.info("Calling consult legal representative endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

