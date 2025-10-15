package com.gal.afiliaciones.infrastructure.client.generic.siarp;

import com.gal.afiliaciones.config.util.SiarpProperties;
import com.gal.afiliaciones.infrastructure.security.SiarpTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultSiarpAffiliateStatusRawClient {

    private static final String SERVICE_NAME = "SIARP";
    private static final String OPERATION = "consultaEstadoAfiliado";

    private final SiarpTokenService siarpTokenService;
    private final SiarpProperties siarpProperties;

    public Mono<String> consultRaw(String tDocEmp, String idEmp, String tDocAfi, String idAfi) {
        String base = System.getProperty("siarp.consultaEstadoAfiliado.url", siarpProperties.getConsultaEstadoAfiliadoUrl());
        if (base == null || base.isBlank()) {
            log.warn("[ConsultSiarpAffiliateStatusRawClient] Missing siarp.consultaEstadoAfiliado.url tDocEmp={} idEmp={} tDocAfi={} idAfi={}", tDocEmp, idEmp, tDocAfi, idAfi);
            return Mono.just("[]");
        }

        String tDocEmpLc = tDocEmp == null ? null : tDocEmp.trim().toLowerCase(Locale.ROOT);
        String tDocAfiLc = tDocAfi == null ? null : tDocAfi.trim().toLowerCase(Locale.ROOT);
        String url = base + "?tDocEmp=" + tDocEmpLc + "&idEmp=" + idEmp + "&tDocAfi=" + tDocAfiLc + "&idAfi=" + idAfi;
        AtomicLong startNs = new AtomicLong();
        return siarpTokenService.getRaw(url, null)
                .doOnSubscribe(sub -> {
                    startNs.set(System.nanoTime());
                    log.debug("[ConsultSiarpAffiliateStatusRawClient] Requesting {} {} url={} tDocEmp(lc)={} tDocAfi(lc)={}", SERVICE_NAME, OPERATION, url, tDocEmpLc, tDocAfiLc);
                })
                
                .doOnSuccess(raw -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    int size = raw != null ? raw.length() : 0;
                    log.debug("[ConsultSiarpAffiliateStatusRawClient] Success {} {} bytes={} elapsedMs={}", SERVICE_NAME, OPERATION, size, elapsedMs);
                })
                .doOnError(err -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    if (err instanceof WebClientResponseException wcre) {
                        String body = wcre.getResponseBodyAsString();
                        int status = wcre.getStatusCode().value();
                        log.warn("[ConsultSiarpAffiliateStatusRawClient] HTTP error {} {} status={} elapsedMs={} url={} body={} err={}",
                                SERVICE_NAME, OPERATION, status, elapsedMs, url, body, wcre.getMessage());
                        
                    } else {
                        log.error("[ConsultSiarpAffiliateStatusRawClient] Unexpected error {} {} elapsedMs={} url={} err={}",
                                SERVICE_NAME, OPERATION, elapsedMs, url, err.getMessage(), err);
                        
                    }
                });
    }
}


