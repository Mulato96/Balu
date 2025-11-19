package com.gal.afiliaciones.infrastructure.dao.telemetry;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Data access object for HTTP outbound call tracking.
 * Part of integrations v2 architecture.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class HttpOutboundCallDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
        INSERT INTO public.http_outbound_call(
          created_at, finished_at, duration_ms,
          service_name, environment, app_version,
          correlation_id, trace_id, span_id,
          origin_method, origin_path, origin_ip, user_id,
          target_method, target_url, target_host, target_path, target_query,
          request_headers, request_body, request_body_size, request_truncated,
          response_status, response_headers, response_body, response_body_size, response_truncated,
          error_message, error_stacktrace, meta
        ) VALUES (
          :created_at, :finished_at, :duration_ms,
          :service_name, :environment, :app_version,
          :correlation_id, :trace_id, :span_id,
          :origin_method, :origin_path, CAST(:origin_ip AS inet), :user_id,
          :target_method, :target_url, :target_host, :target_path, :target_query,
          CAST(:request_headers AS jsonb), :request_body, :request_body_size, :request_truncated,
          :response_status, CAST(:response_headers AS jsonb), :response_body, :response_body_size, :response_truncated,
          :error_message, :error_stacktrace, CAST(:meta AS jsonb)
        )
        """;

    /**
     * Inserts HTTP outbound call record.
     * This method should only be called from the telemetry thread pool.
     * 
     * @param params SQL parameters map
     */
    public void insert(Map<String, Object> params) {
        try {
            // Debug log: Show exactly what data is being saved
            log.debug("=== HTTP OUTBOUND CALL - SAVING TO DATABASE ===");
            log.debug("Target: {} {}", params.get("target_method"), params.get("target_url"));
            log.debug("Host: {}", params.get("target_host"));
            log.debug("Status: {}", params.get("response_status"));
            log.debug("Duration: {}ms", params.get("duration_ms"));
            log.debug("Correlation ID: {}", params.get("correlation_id"));
            log.debug("Service: {}", params.get("service_name"));
            log.debug("Environment: {}", params.get("environment"));
            log.debug("Request Body Size: {} bytes", params.get("request_body_size"));
            log.debug("Response Body Size: {} bytes", params.get("response_body_size"));
            log.debug("Request Truncated: {}", params.get("request_truncated"));
            log.debug("Response Truncated: {}", params.get("response_truncated"));
            log.debug("Error Message: {}", params.get("error_message"));
            log.debug("Origin: {} {} from {}", params.get("origin_method"), params.get("origin_path"), params.get("origin_ip"));
            
            // Show request/response bodies (truncated for logging)
            String requestBody = (String) params.get("request_body");
            String responseBody = (String) params.get("response_body");
            if (requestBody != null) {
                String truncatedRequest = requestBody.length() > 500 ? requestBody.substring(0, 500) + "..." : requestBody;
                log.debug("Request Body: {}", truncatedRequest);
            }
            if (responseBody != null) {
                String truncatedResponse = responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
                log.debug("Response Body: {}", truncatedResponse);
            }
            
            log.debug("Headers - Request: {}", params.get("request_headers"));
            log.debug("Headers - Response: {}", params.get("response_headers"));
            log.debug("=== END HTTP OUTBOUND CALL DATA ===");
            
            int rowsAffected = jdbcTemplate.update(INSERT_SQL, params);
            log.debug("✅ Successfully inserted HTTP outbound call record: {} rows affected", rowsAffected);
            
        } catch (Exception e) {
            // Log but don't throw - telemetry must never break the application
            log.warn("❌ Failed to insert HTTP outbound call record: {}", e.getMessage());
            Throwable root = (e.getCause() != null) ? e.getCause() : e;
            log.warn("   Exception: {} | Root cause: {}", e.getClass().getName(), root.getClass().getName());
            log.warn("   Root message: {}", root.getMessage());
            // Print compact SQL and parameter overview
            log.debug("   SQL template: {}", INSERT_SQL.replace("\n", " "));
            log.debug("   Param keys: {}", params.keySet());
            log.debug("   Null params: {}", params.entrySet().stream().filter(en -> en.getValue() == null).map(java.util.Map.Entry::getKey).toList());
            if (log.isDebugEnabled()) log.debug("Insert failure details", e);
        }
    }
}
