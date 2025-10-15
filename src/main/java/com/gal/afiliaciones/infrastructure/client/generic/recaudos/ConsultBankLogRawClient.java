package com.gal.afiliaciones.infrastructure.client.generic.recaudos;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult Bank Payment Log Raw Client from Recaudos module.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultBankLogRawClient {
    private final BusTokenService busTokenService;
    private final CollectProperties properties;

    public Mono<String> consultRaw(String nDocApo, String anoPer, String mesPer) {
        return consultRaw(nDocApo, anoPer, mesPer, null);
    }

    public Mono<String> consultRaw(String nDocApo, String anoPer, String mesPer, String fechaPago) {
        StringBuilder urlBuilder = new StringBuilder(properties.getBankLogConsultUrl())
                .append("?nDocApo=").append(nDocApo)
                .append("&anoPer=").append(anoPer)
                .append("&mesPer=").append(mesPer);
        
        if (fechaPago != null && !fechaPago.isEmpty()) {
            urlBuilder.append("&fechaPago=").append(fechaPago);
        }
        
        String url = urlBuilder.toString();
        log.info("Calling consult bank log endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

