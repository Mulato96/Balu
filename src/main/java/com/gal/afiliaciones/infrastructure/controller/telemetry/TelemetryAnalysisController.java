package com.gal.afiliaciones.infrastructure.controller.telemetry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for analyzing HTTP outbound call telemetry data.
 * Provides endpoints to query and analyze integration performance.
 */
@RestController
@RequestMapping("/api/v1/telemetry/analysis")
@Tag(name = "Telemetry Analysis", description = "Analyze HTTP outbound call telemetry data")
@RequiredArgsConstructor
@Slf4j
public class TelemetryAnalysisController {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String HOURS_PARAM = "hours";
    private static final String ERROR_TXT = "error";

    @GetMapping("/summary")
    @Operation(summary = "Get telemetry summary", description = "Returns summary statistics of HTTP outbound calls")
    public ResponseEntity<Map<String, Object>> getTelemetrySummary(
            @RequestParam(defaultValue = "24") int hours) {
        
        String sql = """
            SELECT 
                COUNT(*) as total_calls,
                COUNT(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 END) as success_calls,
                COUNT(CASE WHEN response_status >= 400 THEN 1 END) as error_calls,
                COUNT(CASE WHEN error_message IS NOT NULL THEN 1 END) as exception_calls,
                AVG(duration_ms) as avg_duration_ms,
                MAX(duration_ms) as max_duration_ms,
                MIN(duration_ms) as min_duration_ms
            FROM http_outbound_call 
            WHERE created_at >= NOW() - INTERVAL ':hours hours'
            """;
        
        Map<String, Object> params = Map.of(HOURS_PARAM, hours);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
            Map<String, Object> summary = results.isEmpty() ? new HashMap<>() : results.get(0);
            summary.put("analysis_period_hours", hours);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting telemetry summary", e);
            return ResponseEntity.status(500).body(Map.of(ERROR_TXT, e.getMessage()));
        }
    }

    @GetMapping("/by-service")
    @Operation(summary = "Get telemetry by service", description = "Returns telemetry statistics grouped by service")
    public ResponseEntity<List<Map<String, Object>>> getTelemetryByService(
            @RequestParam(defaultValue = "24") int hours) {
        
        String sql = """
            SELECT 
                service_name,
                target_host,
                COUNT(*) as total_calls,
                COUNT(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 END) as success_calls,
                COUNT(CASE WHEN response_status >= 400 THEN 1 END) as error_calls,
                AVG(duration_ms) as avg_duration_ms,
                MAX(duration_ms) as max_duration_ms
            FROM http_outbound_call 
            WHERE created_at >= NOW() - INTERVAL ':hours hours'
            GROUP BY service_name, target_host
            ORDER BY total_calls DESC
            """;
        
        Map<String, Object> params = Map.of(HOURS_PARAM, hours);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error getting telemetry by service", e);
            return ResponseEntity.status(500).body(List.of(Map.of(ERROR_TXT, e.getMessage())));
        }
    }

    @GetMapping("/errors")
    @Operation(summary = "Get recent errors", description = "Returns recent HTTP call errors")
    public ResponseEntity<List<Map<String, Object>>> getRecentErrors(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "50") int limit) {
        
        String sql = """
            SELECT 
                created_at,
                service_name,
                target_method,
                target_url,
                response_status,
                error_message,
                correlation_id,
                duration_ms
            FROM http_outbound_call 
            WHERE created_at >= NOW() - INTERVAL ':hours hours'
            AND (response_status >= 400 OR error_message IS NOT NULL)
            ORDER BY created_at DESC
            LIMIT :limit
            """;
        
        Map<String, Object> params = Map.of(HOURS_PARAM, hours, "limit", limit);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error getting recent errors", e);
            return ResponseEntity.status(500).body(List.of(Map.of(ERROR_TXT, e.getMessage())));
        }
    }

    @GetMapping("/by-correlation/{correlationId}")
    @Operation(summary = "Get calls by correlation ID", description = "Returns all HTTP calls for a specific correlation ID")
    public ResponseEntity<List<Map<String, Object>>> getCallsByCorrelation(
            @PathVariable String correlationId) {
        
        String sql = """
            SELECT 
                created_at,
                finished_at,
                duration_ms,
                service_name,
                target_method,
                target_url,
                response_status,
                error_message,
                request_body_size,
                response_body_size
            FROM http_outbound_call 
            WHERE correlation_id = :correlationId
            ORDER BY created_at ASC
            """;
        
        Map<String, Object> params = Map.of("correlationId", correlationId);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error getting calls by correlation ID", e);
            return ResponseEntity.status(500).body(List.of(Map.of(ERROR_TXT, e.getMessage())));
        }
    }

    @GetMapping("/performance")
    @Operation(summary = "Get performance metrics", description = "Returns performance metrics for HTTP calls")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(defaultValue = "24") int hours) {
        
        String sql = """
            SELECT 
                target_host,
                COUNT(*) as total_calls,
                AVG(duration_ms) as avg_duration_ms,
                PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_ms) as p50_duration_ms,
                PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_ms) as p95_duration_ms,
                PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY duration_ms) as p99_duration_ms,
                MAX(duration_ms) as max_duration_ms,
                COUNT(CASE WHEN response_status >= 400 THEN 1 END) * 100.0 / COUNT(*) as error_rate_percent
            FROM http_outbound_call 
            WHERE created_at >= NOW() - INTERVAL ':hours hours'
            AND duration_ms IS NOT NULL
            GROUP BY target_host
            HAVING COUNT(*) >= 10
            ORDER BY total_calls DESC
            """;
        
        Map<String, Object> params = Map.of(HOURS_PARAM, hours);
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysis_period_hours", hours);
            response.put("performance_by_host", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting performance metrics", e);
            return ResponseEntity.status(500).body(Map.of(ERROR_TXT, e.getMessage()));
        }
    }
}
