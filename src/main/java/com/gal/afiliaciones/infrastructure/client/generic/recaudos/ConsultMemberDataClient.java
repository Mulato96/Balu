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
 * Consult Member/Contributor Data (Datos Cotizante) from Recaudos module.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultMemberDataClient {
    private final BusTokenService busTokenService;
    private final CollectProperties properties;

    public Mono<List<Object>> consult(String tDocApo, String nDocApo, String tDocAfi, String nDocAfi, 
                                      String fechaPago, String anoPer, String mesPer) {
        String url = properties.getMemberDataConsultUrl()
                + "?tDocApo=" + tDocApo
                + "&nDocApo=" + nDocApo
                + "&tDocAfi=" + tDocAfi
                + "&nDocAfi=" + nDocAfi
                + "&fechaPago=" + fechaPago
                + "&anoPer=" + anoPer
                + "&mesPer=" + mesPer;
        
        log.info("Calling consult member data endpoint: {}", url);
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}

