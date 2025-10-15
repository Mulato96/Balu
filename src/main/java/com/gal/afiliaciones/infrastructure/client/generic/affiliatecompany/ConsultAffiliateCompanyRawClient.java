package com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany;

import org.springframework.stereotype.Component;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Consult Affiliate Company Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultAffiliateCompanyRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    private static final String AND_ID_AFFILIADO = "&idAfiliado=";
    private static final String TIPO_DOC = "?idTipoDoc=";

    public Mono<String> consultRaw(String tipoDoc, String idAfiliado) {
        String url = properties.getBusUrlAffiliate()
                + TIPO_DOC + tipoDoc + AND_ID_AFFILIADO + idAfiliado;
        
        log.info("Calling consult affiliate company endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }

    public Mono<String> consultRawV2(String idTipoDoc, String idAfiliado) {
        String url = properties.getBusUrlAffiliate()
                + TIPO_DOC + idTipoDoc + AND_ID_AFFILIADO + idAfiliado;

        log.debug("[ConsultAffiliateCompanyRawClient] (v2) Requesting affiliate company data (raw) from: {}", url);

        return busTokenService.getRaw(url)
                .defaultIfEmpty("[]");
    }

    public Mono<String> consultRawV1(String idTipoDoc, String idAfiliado) {
        String url = properties.getBusUrlAffiliateV1()
                + TIPO_DOC + idTipoDoc + AND_ID_AFFILIADO + idAfiliado;

        log.debug("[ConsultAffiliateCompanyRawClient] (v1) Requesting affiliate company data (raw) from: {}", url);

        return busTokenService.getRaw(url)
                .defaultIfEmpty("[]");
    }
}

