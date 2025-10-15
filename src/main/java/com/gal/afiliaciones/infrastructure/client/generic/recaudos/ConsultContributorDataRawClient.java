package com.gal.afiliaciones.infrastructure.client.generic.recaudos;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Contributor/Payer Data (Datos Aportantes) Raw Client from Recaudos module.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultContributorDataRawClient {
    private final BusTokenService busTokenService;
    private final CollectProperties properties;

    public Mono<String> consultRaw(String tDocApo, String nDocApo, String fechaPago, String anoPer, String mesPer) {
        String url = properties.getContributorDataConsultUrl()
                + "?tDocApo=" + tDocApo
                + "&nDocApo=" + nDocApo
                + "&fechaPago=" + fechaPago
                + "&anoPer=" + anoPer
                + "&mesPer=" + mesPer;
        
        log.info("Calling consult contributor data endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

