package com.gal.afiliaciones.infrastructure.dto.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Record representing an HTTP outbound call for telemetry tracking.
 * Part of integrations v2 architecture.
 */
@Data
@Slf4j
public class HttpCallRecord {
    
    // Timing
    private Instant started;
    private Instant finished;
    
    // Service context
    private String serviceName;
    private String environment;
    private String appVersion;
    
    // Tracing
    private String correlationId;
    private String traceId;
    private String spanId;
    
    // Origin (inbound request context)
    private String originMethod;
    private String originPath;
    private String originIp;
    private String userId;
    
    // Target (outbound request)
    private String targetMethod;
    private String targetUrl;
    private String targetHost;
    private String targetPath;
    private String targetQuery;
    
    // Request/Response data
    private Map<String, String> requestHeaders;
    private Map<String, String> responseHeaders;
    private byte[] requestBodyRaw;
    private byte[] responseBodyRaw;
    private Integer responseStatus;
    
    // Error information
    private String errorMessage;
    private String errorStacktrace;
    
    // Additional metadata
    private String metaJson;
    
    /**
     * Converts this record to SQL parameters for database insertion.
     * Handles truncation, sanitization, and format conversion.
     */
    public Map<String, Object> toSqlParams(int maxBodySize) {
        Map<String, Object> params = new HashMap<>();
        
        // Timing
        params.put("created_at", started != null ? Timestamp.from(started) : null);
        params.put("finished_at", finished != null ? Timestamp.from(finished) : null);
        params.put("duration_ms", calculateDurationMs());
        
        // Service context
        params.put("service_name", serviceName);
        params.put("environment", environment);
        params.put("app_version", appVersion);
        
        // Tracing
        params.put("correlation_id", correlationId);
        params.put("trace_id", traceId);
        params.put("span_id", spanId);
        
        // Origin
        params.put("origin_method", originMethod);
        params.put("origin_path", originPath);
        params.put("origin_ip", originIp);
        params.put("user_id", userId);
        
        // Target - parse URL components
        parseTargetUrl();
        params.put("target_method", targetMethod);
        params.put("target_url", targetUrl);
        params.put("target_host", targetHost);
        params.put("target_path", targetPath);
        params.put("target_query", targetQuery);
        
        // Headers (sanitized)
        Map<String, String> safeRequestHeaders = sanitizeHeaders(requestHeaders);
        Map<String, String> safeResponseHeaders = sanitizeHeaders(responseHeaders);
        params.put("request_headers", toJsonString(safeRequestHeaders));
        params.put("response_headers", toJsonString(safeResponseHeaders));
        
        // Body handling (Content-Type–aware)
        String reqCt = headerValueIgnoreCase(safeRequestHeaders, "Content-Type");
        String resCt = headerValueIgnoreCase(safeResponseHeaders, "Content-Type");
        String requestBody = convertBytesToText(requestBodyRaw, reqCt);
        String responseBody = convertBytesToText(responseBodyRaw, resCt);
        
        params.put("request_body_size", calculateBodySize(requestBody));
        params.put("response_body_size", calculateBodySize(responseBody));
        params.put("request_truncated", requestBody != null && requestBody.length() > maxBodySize);
        params.put("response_truncated", responseBody != null && responseBody.length() > maxBodySize);
        params.put("request_body", truncateString(requestBody, maxBodySize));
        params.put("response_body", truncateString(responseBody, maxBodySize));
        
        // Response status
        params.put("response_status", responseStatus);
        
        // Error information
        params.put("error_message", errorMessage);
        params.put("error_stacktrace", errorStacktrace);
        
        // Metadata
        params.put("meta", metaJson != null ? metaJson : "{}");
        
        return params;
    }
    
    /**
     * Sets error information from an exception.
     */
    public void setError(Throwable throwable) {
        if (throwable != null) {
            this.errorMessage = throwable.getMessage();
            this.errorStacktrace = getStackTrace(throwable);
        }
    }
    
    private Long calculateDurationMs() {
        if (started == null || finished == null) {
            return null;
        }
        return Duration.between(started, finished).toMillis();
    }
    
    private void parseTargetUrl() {
        if (targetUrl != null) {
            try {
                URI uri = URI.create(targetUrl);
                if (targetHost == null) {
                    targetHost = uri.getHost();
                }
                if (targetPath == null) {
                    targetPath = uri.getPath();
                }
                if (targetQuery == null) {
                    targetQuery = uri.getQuery();
                }
            } catch (Exception e) {
                log.debug("Failed to parse target URL: {}", targetUrl, e);
            }
        }
    }
    
    private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if (headers == null) {
            return Collections.emptyMap();
        }
        
        Map<String, String> sanitized = new HashMap<>(headers);
        
        // Remove sensitive headers
        List<String> sensitiveHeaders = Arrays.asList(
            "authorization", "Authorization",
            "cookie", "Cookie",
            "x-api-key", "X-API-Key",
            "x-auth-token", "X-Auth-Token"
        );
        
        sensitiveHeaders.forEach(sanitized::remove);
        
        return sanitized;
    }
    
    private String convertBytesToText(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            if (isJsonOrText(contentType)) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            if (isClearlyBinary(contentType)) {
                return Base64.getEncoder().encodeToString(bytes);
            }
            // Unknown/missing content-type: small heuristic fallback
            return isLikelyBinary(bytes) ? Base64.getEncoder().encodeToString(bytes)
                                         : new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Failed to convert bytes to text, using Base64", e);
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    private boolean isLikelyBinary(byte[] bytes) {
        int controlCount = 0;
        for (byte b0 : bytes) {
            int b = b0 & 0xFF;
            // Null byte is a strong signal of binary content
            if (b == 0x00) {
                return true;
            }
            // Count non-whitespace control characters
            if ((b < 0x09) || (b >= 0x0E && b < 0x20)) {
                controlCount++;
                // If more than ~5% (or more than 8) are control chars, treat as binary
                if (controlCount > Math.max(8, bytes.length / 20)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isJsonOrText(String contentType) {
        if (contentType == null) {
            return false;
        }
        String c = contentType.toLowerCase();
        return c.startsWith("application/json")
            || c.contains("+json")
            || c.startsWith("text/")
            || c.startsWith("application/xml")
            || c.contains("+xml");
    }

    private boolean isClearlyBinary(String contentType) {
        if (contentType == null) {
            return false;
        }
        String c = contentType.toLowerCase();
        return c.startsWith("application/pdf")
            || c.startsWith("application/octet-stream")
            || c.startsWith("image/")
            || c.startsWith("application/zip");
    }

    private String headerValueIgnoreCase(Map<String, String> headers, String name) {
        if (headers == null || name == null) {
            return null;
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (name.equalsIgnoreCase(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }
    
    private Integer calculateBodySize(String body) {
        return body != null ? body.getBytes(StandardCharsets.UTF_8).length : null;
    }
    
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
    
    private String toJsonString(Map<String, String> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map != null ? map : Collections.emptyMap());
        } catch (Exception e) {
            log.debug("Failed to serialize map to JSON", e);
            return "{}";
        }
    }
    
    private String getStackTrace(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Failed to capture stack trace: " + e.getMessage();
        }
    }
}
