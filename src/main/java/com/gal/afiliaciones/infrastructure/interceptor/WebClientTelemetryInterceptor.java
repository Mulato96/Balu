package com.gal.afiliaciones.infrastructure.interceptor;

import com.gal.afiliaciones.application.service.telemetry.HttpOutboundLogger;
import com.gal.afiliaciones.infrastructure.dto.telemetry.HttpCallRecord;
import com.gal.afiliaciones.infrastructure.filter.CorrelationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
 
import reactor.core.publisher.Mono;
 

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
 
 
import org.springframework.http.HttpMethod;
 

/**
 * WebClient interceptor for HTTP outbound call telemetry.
 * Part of integrations v2 architecture.
 * 
 * Captures request/response data and logs it asynchronously without blocking reactive streams.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientTelemetryInterceptor {

    private final HttpOutboundLogger httpOutboundLogger;
    
    @Value("${spring.application.name:afiliaciones-service}")
    private String serviceName;
    
    @Value("${spring.profiles.active:unknown}")
    private String environment;
    
    @Value("${app.version:unknown}")
    private String appVersion;

    /**
     * Creates the telemetry exchange filter function.
     *
     * @return ExchangeFilterFunction for WebClient
     */
    public ExchangeFilterFunction telemetryFilter() {
        return (request, next) -> {
            log.debug("🔍 WebClient Telemetry Interceptor - HTTP call detected: {} {}",
                request.method(), request.url());

            Instant startTime = Instant.now();
            HttpCallRecord callRecord = createBaseRecord(request, startTime);

            log.debug("📝 Created HTTP call record for: {}", request.url());
            log.debug("➡️ Telemetry about to call next.exchange for {} {}", request.method(), request.url());

            // Check if we need to capture request body (POST, PUT, PATCH)
            if (shouldCaptureRequestBody(request.method())) {
                // Intercept and buffer the request body
                return captureRequestBodyFromStream(request, callRecord)
                    .flatMap(modifiedRequest -> next.exchange(modifiedRequest)
                        .flatMap(response -> captureResponseData(response, callRecord))
                        .doOnError(error -> handleError(callRecord, error))
                        .doFinally(signalType -> finalizeLogging(callRecord)));
            } else {
                // No body capture needed (GET, DELETE, etc.)
                return next.exchange(request)
                    .flatMap(response -> captureResponseData(response, callRecord))
                    .doOnError(error -> handleError(callRecord, error))
                    .doFinally(signalType -> finalizeLogging(callRecord));
            }
        };
    }

    private boolean shouldCaptureRequestBody(HttpMethod method) {
        return method == HttpMethod.POST ||
               method == HttpMethod.PUT ||
               method == HttpMethod.PATCH;
    }

    private void handleError(HttpCallRecord callRecord, Throwable error) {
        try {
            log.debug("❌ HTTP call error detected, logging telemetry: {}", error.getMessage());
            callRecord.setFinished(Instant.now());
            callRecord.setError(error);
            httpOutboundLogger.logAsync(callRecord);
        } catch (Exception e) {
            log.debug("Failed to log telemetry on error: {}", e.getMessage());
        }
    }

    private void finalizeLogging(HttpCallRecord callRecord) {
        try {
            // Always ensure we log once. If captureResponseData logged already, skip.
            if (callRecord.getFinished() == null) {
                callRecord.setFinished(Instant.now());
                log.debug("🏁 HTTP call finished (no body captured path), logging telemetry now");
                httpOutboundLogger.logAsync(callRecord);
            }
        } catch (Exception e) {
            log.debug("Failed to finalize telemetry logging: {}", e.getMessage());
        }
    }
    
    private HttpCallRecord createBaseRecord(ClientRequest request, Instant startTime) {
        HttpCallRecord callRecord = httpOutboundLogger.createRecord(serviceName, environment, appVersion);
        
        callRecord.setStarted(startTime);
        
        // Set correlation context from MDC
        callRecord.setCorrelationId(MDC.get(CorrelationFilter.MDC_CORRELATION_ID));
        callRecord.setOriginMethod(MDC.get(CorrelationFilter.MDC_ORIGIN_METHOD));
        callRecord.setOriginPath(MDC.get(CorrelationFilter.MDC_ORIGIN_PATH));
        callRecord.setOriginIp(MDC.get(CorrelationFilter.MDC_ORIGIN_IP));
        callRecord.setUserId(MDC.get(CorrelationFilter.MDC_USER_ID));
        
        // Set target information
        URI uri = request.url();
        callRecord.setTargetMethod(request.method().name());
        callRecord.setTargetUrl(uri.toString());
        callRecord.setTargetHost(uri.getHost());
        callRecord.setTargetPath(uri.getPath());
        callRecord.setTargetQuery(uri.getQuery());
        
        // Set request headers
        Map<String, String> headers = new HashMap<>();
        request.headers().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headers.put(name, values.get(0)); // Take first value
            }
        });
        callRecord.setRequestHeaders(headers);
        
        return callRecord;
    }
    
    private Mono<ClientRequest> captureRequestBodyFromStream(ClientRequest request, HttpCallRecord callRecord) {
        return Mono.defer(() -> {
            try {
                // First try to get body from attribute (for backward compatibility)
                Object bodyAttr = request.attributes().get("telemetryRequestBody");
                if (bodyAttr != null) {
                    byte[] bytes = convertToBytes(bodyAttr);
                    if (bytes.length > 0) {
                        callRecord.setRequestBodyRaw(bytes);
                        log.debug("✍️ Captured request body from attribute ({} bytes)", bytes.length);
                    }
                    return Mono.just(request);
                }

                // If no attribute, skip generic capture to avoid mutating buffers
                log.debug("ℹ️ No request body attribute provided; skipping generic capture to preserve transparency");
                return Mono.just(request);

            } catch (Exception e) {
                log.debug("Failed to set up request body capture: {}", e.getMessage());
                return Mono.just(request);
            }
        }).onErrorResume(e -> {
            log.debug("Error in request body capture, continuing without body: {}", e.getMessage());
            return Mono.just(request);
        });
    }

    private byte[] convertToBytes(Object bodyAttr) {
        try {
            if (bodyAttr instanceof byte[] bytes) {
                return bytes;
            } else if (bodyAttr instanceof String str) {
                return str.getBytes(StandardCharsets.UTF_8);
            } else {
                // Fallback to JSON serialization for POJOs
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(bodyAttr);
            }
        } catch (Exception e) {
            log.debug("Failed to convert body to bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    private Mono<ClientResponse> captureResponseData(ClientResponse response, HttpCallRecord callRecord) {
        log.debug("📊 Capturing response data: status={}", response.statusCode());

        // Preserve original metadata first
        callRecord.setResponseStatus(response.statusCode().value());

        Map<String, String> responseHeaders = new HashMap<>();
        response.headers().asHttpHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                responseHeaders.put(name, values.get(0));
            }
        });
        callRecord.setResponseHeaders(responseHeaders);

        // For error responses (4xx, 5xx), we MUST capture the body for debugging
        boolean isErrorResponse = callRecord.getResponseStatus() != null && callRecord.getResponseStatus() >= 400;
        
        if (isErrorResponse) {
            log.debug("⚠️ HTTP error detected by telemetry: status={} url={}", callRecord.getResponseStatus(), callRecord.getTargetUrl());
            String wwwAuth = response.headers().asHttpHeaders().getFirst("WWW-Authenticate");
            if (wwwAuth != null) {
                log.debug("WWW-Authenticate header: {}", wwwAuth);
            }
            // Continue to capture error body - critical for debugging
        }

        // Buffer the body to bytes for telemetry, but always re-emit transparently using mutate()
        return response.bodyToMono(byte[].class)
            .onErrorResume(e -> {
                log.debug("Telemetry response capture error: {}", e.toString());
                return Mono.just(new byte[0]);
            })
            .flatMap(bytes -> {
                // Rebuild the response that downstream will see (transparent)
                ClientResponse safe = response.mutate()
                    .body(reactor.core.publisher.Flux.just(new org.springframework.core.io.buffer.DefaultDataBufferFactory().wrap(bytes)))
                    .build();

                // Telemetry side-effects must never affect the pipeline
                try {
                    callRecord.setResponseBodyRaw(bytes);
                    callRecord.setFinished(Instant.now());
                    
                    // Log error response body for debugging
                    if (isErrorResponse && bytes.length > 0) {
                        String errorBody = new String(bytes, StandardCharsets.UTF_8);
                        log.warn("❌ HTTP error response captured: status={} url={} body={}", 
                                callRecord.getResponseStatus(), 
                                callRecord.getTargetUrl(), 
                                errorBody.length() > 500 ? errorBody.substring(0, 500) + "..." : errorBody);
                    }
                    
                    httpOutboundLogger.logAsync(callRecord);
                } catch (Exception t) {
                    if (log.isDebugEnabled()) {
                        log.debug("Telemetry side-effect failed: {}", t.toString());
                    }
                }

                return Mono.just(safe);
            });
    }

    // Request body capture for generic cases intentionally disabled to keep telemetry transparent
}
