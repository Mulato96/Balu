package com.gal.afiliaciones.infrastructure.client.generic.person;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Person Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultPersonRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> consultRaw(String idTipoDocPersona, String idPersona) {
        String url = properties.getBusUrlPerson()
                + "?idTipoDoc=" + idTipoDocPersona
                + "&idPersona=" + idPersona;
        
        log.info("Calling consult person endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

