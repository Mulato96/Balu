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
public class ConsultSiarpAffiliateRawClient {

    private static final String SERVICE_NAME = "SIARP";
    private static final String OPERATION = "consultaAfiliado2";

    private final SiarpTokenService siarpTokenService;
    private final SiarpProperties siarpProperties;

    public Mono<String> consultRaw(String tDoc, String idAfiliado) {
        String base = siarpProperties.getConsultaAfiliado2Url();
        if (base == null || base.isBlank()) {
            log.warn("[ConsultSiarpAffiliateRawClient] Missing siarp.consultaAfiliado2.url tDoc={} idAfiliado={}", tDoc, idAfiliado);
            return Mono.just("[]");
        }

        String tDocLc = tDoc == null ? null : tDoc.trim().toLowerCase(Locale.ROOT);
        String url = base + "?tDoc=" + tDocLc + "&idAfiliado=" + idAfiliado;

        AtomicLong startNs = new AtomicLong();
        return siarpTokenService.getRaw(url, null)
                .doOnSubscribe(sub -> {
                    startNs.set(System.nanoTime());
                    log.debug("[ConsultSiarpAffiliateRawClient] Requesting {} {} url={} tDoc={} (lc) idAfiliado={}", SERVICE_NAME, OPERATION, url, tDocLc, idAfiliado);
                })
                
                .doOnSuccess(raw -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    int size = raw != null ? raw.length() : 0;
                    log.debug("[ConsultSiarpAffiliateRawClient] Success {} {} bytes={} elapsedMs={}", SERVICE_NAME, OPERATION, size, elapsedMs);
                })
                .doOnError(err -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    if (err instanceof WebClientResponseException wcre) {
                        String body = wcre.getResponseBodyAsString();
                        int status = wcre.getStatusCode().value();
                        log.warn("[ConsultSiarpAffiliateRawClient] HTTP error {} {} status={} elapsedMs={} url={} body={} err={}",
                                SERVICE_NAME, OPERATION, status, elapsedMs, url, body, wcre.getMessage());
                        
                    } else {
                        log.error("[ConsultSiarpAffiliateRawClient] Unexpected error {} {} elapsedMs={} url={} err={}",
                                SERVICE_NAME, OPERATION, elapsedMs, url, err.getMessage(), err);
                        
                    }
                });
    }
}


