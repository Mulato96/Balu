package com.gal.afiliaciones.infrastructure.client.generic.recaudos;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Consult Bank Payment Log from Recaudos module.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultBankLogClient {
    private final BusTokenService busTokenService;
    private final CollectProperties properties;

    public Mono<List<Object>> consult(String nDocApo, String anoPer, String mesPer) {
        return consult(nDocApo, anoPer, mesPer, null);
    }

    public Mono<List<Object>> consult(String nDocApo, String anoPer, String mesPer, String fechaPago) {
        StringBuilder urlBuilder = new StringBuilder(properties.getBankLogConsultUrl())
                .append("?nDocApo=").append(nDocApo)
                .append("&anoPer=").append(anoPer)
                .append("&mesPer=").append(mesPer);
        
        if (fechaPago != null && !fechaPago.isEmpty()) {
            urlBuilder.append("&fechaPago=").append(fechaPago);
        }
        
        String url = urlBuilder.toString();
        log.info("Calling consult bank log endpoint: {}", url);
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}

