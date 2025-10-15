package com.gal.afiliaciones.infrastructure.client.generic.siarp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.config.util.SiarpProperties;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import com.gal.afiliaciones.infrastructure.security.SiarpTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultSiarpAffiliateClient {

    private static final String SERVICE_NAME = "SIARP";
    private static final String OPERATION = "consultaAfiliado2";

    private final SiarpTokenService siarpTokenService;
    private final SiarpProperties siarpProperties;
    private final ObjectMapper objectMapper;

    public Mono<List<EmployerEmployeeDTO>> consult(String tDoc, String idAfiliado) {
        String base = siarpProperties.getConsultaAfiliado2Url();
        if (base == null || base.isBlank()) {
            log.warn("[ConsultSiarpAffiliateClient] Missing siarp.consultaAfiliado2.url tDoc={} idAfiliado={}", tDoc, idAfiliado);
            return Mono.just(List.of());
        }

        String tDocLc = tDoc == null ? null : tDoc.trim().toLowerCase(Locale.ROOT);
        String url = base + "?tDoc=" + tDocLc + "&idAfiliado=" + idAfiliado;

        AtomicLong startNs = new AtomicLong();
        return siarpTokenService.getRaw(url, null)
                .doOnSubscribe(sub -> {
                    startNs.set(System.nanoTime());
                    log.debug("[ConsultSiarpAffiliateClient] Requesting {} {} url={} tDoc={} (lc) idAfiliado={}", SERVICE_NAME, OPERATION, url, tDocLc, idAfiliado);
                })
                
                .map(raw -> {
                    try {
                        List<EmployerEmployeeDTO> list = objectMapper.readValue(raw, new TypeReference<List<EmployerEmployeeDTO>>() {});
                        long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                        log.debug("[ConsultSiarpAffiliateClient] Success {} {} items={} elapsedMs={}", SERVICE_NAME, OPERATION,
                                list != null ? list.size() : 0, elapsedMs);
                        return list;
                    } catch (Exception e) {
                        throw new com.gal.afiliaciones.config.ex.SiarpClientException("Failed to parse SIARP response: " + e.getMessage(), e);
                    }
                })
                .doOnError(err -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    if (err instanceof WebClientResponseException wcre) {
                        String body = wcre.getResponseBodyAsString();
                        int status = wcre.getStatusCode().value();
                        log.warn("[ConsultSiarpAffiliateClient] HTTP error {} {} status={} elapsedMs={} url={} body={} err={}",
                                SERVICE_NAME, OPERATION, status, elapsedMs, url, body, wcre.getMessage());
                        
                    } else {
                        log.error("[ConsultSiarpAffiliateClient] Unexpected error {} {} elapsedMs={} url={} err={}",
                                SERVICE_NAME, OPERATION, elapsedMs, url, err.getMessage(), err);
                        
                    }
                });
    }
}


