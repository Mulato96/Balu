package com.gal.afiliaciones.infrastructure.client.generic.siarp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.config.util.SiarpProperties;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
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
public class ConsultSiarpAffiliateStatusClient {

    private static final String SERVICE_NAME = "SIARP";
    private static final String OPERATION = "consultaEstadoAfiliado";

    private final SiarpTokenService siarpTokenService;
    private final SiarpProperties siarpProperties;
    private final ObjectMapper objectMapper;

    public Mono<List<TmpAffiliateStatusDTO>> consult(String tDocEmp, String idEmp, String tDocAfi, String idAfi) {
        String base = System.getProperty("siarp.consultaEstadoAfiliado.url", siarpProperties.getConsultaEstadoAfiliadoUrl());
        if (base == null || base.isBlank()) {
            log.warn("[ConsultSiarpAffiliateStatusClient] Missing siarp.consultaEstadoAfiliado.url tDocEmp={} idEmp={} tDocAfi={} idAfi={}", tDocEmp, idEmp, tDocAfi, idAfi);
            return Mono.just(List.of());
        }

        String tDocEmpLc = tDocEmp == null ? null : tDocEmp.trim().toLowerCase(Locale.ROOT);
        String tDocAfiLc = tDocAfi == null ? null : tDocAfi.trim().toLowerCase(Locale.ROOT);
        String url = base + "?tDocEmp=" + tDocEmpLc + "&idEmp=" + idEmp + "&tDocAfi=" + tDocAfiLc + "&idAfi=" + idAfi;
        AtomicLong startNs = new AtomicLong();
        return siarpTokenService.getRaw(url, null)
                .doOnSubscribe(sub -> {
                    startNs.set(System.nanoTime());
                    log.debug("[ConsultSiarpAffiliateStatusClient] Requesting {} {} url={} tDocEmp(lc)={} tDocAfi(lc)={}", SERVICE_NAME, OPERATION, url, tDocEmpLc, tDocAfiLc);
                })
                
                .map(raw -> {
                    try {
                        List<TmpAffiliateStatusDTO> list = objectMapper.readValue(raw, new TypeReference<List<TmpAffiliateStatusDTO>>() {});
                        long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                        log.debug("[ConsultSiarpAffiliateStatusClient] Success {} {} items={} elapsedMs={}", SERVICE_NAME, OPERATION,
                                list != null ? list.size() : 0, elapsedMs);
                        return list;
                    } catch (Exception e) {
                        throw new com.gal.afiliaciones.config.ex.SiarpClientException("Failed to parse SIARP estado response: " + e.getMessage(), e);
                    }
                })
                .doOnError(err -> {
                    long elapsedMs = (System.nanoTime() - startNs.get()) / 1_000_000L;
                    if (err instanceof WebClientResponseException wcre) {
                        String body = wcre.getResponseBodyAsString();
                        int status = wcre.getStatusCode().value();
                        log.warn("[ConsultSiarpAffiliateStatusClient] HTTP error {} {} status={} elapsedMs={} url={} body={} err={}",
                                SERVICE_NAME, OPERATION, status, elapsedMs, url, body, wcre.getMessage());
                        
                    } else {
                        log.error("[ConsultSiarpAffiliateStatusClient] Unexpected error {} {} elapsedMs={} url={} err={}",
                                SERVICE_NAME, OPERATION, elapsedMs, url, err.getMessage(), err);
                        
                    }
                });
    }
}


