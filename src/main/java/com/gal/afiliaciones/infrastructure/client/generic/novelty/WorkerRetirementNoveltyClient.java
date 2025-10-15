package com.gal.afiliaciones.infrastructure.client.generic.novelty;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyResponse;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerRetirementNoveltyClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    private static final String SERVICE_NAME = "WorkerRetirementNoveltyClient";

    public WorkerRetirementNoveltyResponse send(WorkerRetirementNoveltyRequest request) {
        String url = properties.getWorkerRetirementNoveltyUrl();
        log.info("[{}] Calling endpoint: {}", SERVICE_NAME, url);
        log.debug("[{}] Sending novelty request payload: {}", SERVICE_NAME, safe(request));
        try {
            WorkerRetirementNoveltyResponse response = busTokenService
                    .exchange(HttpMethod.POST, url, request, WorkerRetirementNoveltyResponse.class)
                    .block();
            log.debug("[{}] Success response: {}", SERVICE_NAME, safe(response));
            // Telemetry is handled by WebClient interceptor; no explicit PositivaLogService needed
            return response;
        } catch (Exception ex) {
            String code = ex instanceof WebClientResponseException wcre ? String.valueOf(wcre.getStatusCode().value()) : "EX";
            String message = ex instanceof WebClientResponseException wcre ? wcre.getResponseBodyAsString() : ex.getMessage();
            log.warn("[{}] Error calling {}: code={}, message={}", SERVICE_NAME, url, code, message);
            throw ex;
        }
    }

    private String safe(Object o) {
        try { return String.valueOf(o); } catch (Exception e) { return "<unserializable>"; }
    }
}


