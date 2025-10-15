package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "http_outbound_call")
public class HttpOutboundCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "service_name", length = 150)
    private String serviceName;

    @Column(name = "environment", length = 50)
    private String environment;

    @Column(name = "app_version", length = 100)
    private String appVersion;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "span_id", length = 128)
    private String spanId;

    @Column(name = "origin_method", length = 10)
    private String originMethod;

    @Column(name = "origin_path", columnDefinition = "TEXT")
    private String originPath;

    @Column(name = "origin_ip")
    private String originIp;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "target_method", length = 10, nullable = false)
    private String targetMethod;

    @Column(name = "target_url", columnDefinition = "TEXT", nullable = false)
    private String targetUrl;

    @Column(name = "target_host", columnDefinition = "TEXT")
    private String targetHost;

    @Column(name = "target_path", columnDefinition = "TEXT")
    private String targetPath;

    @Column(name = "target_query", columnDefinition = "TEXT")
    private String targetQuery;

    @Column(name = "request_headers", columnDefinition = "jsonb")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "request_body_size")
    private Integer requestBodySize;

    @Column(name = "request_truncated")
    private Boolean requestTruncated;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_headers", columnDefinition = "jsonb")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_body_size")
    private Integer responseBodySize;

    @Column(name = "response_truncated")
    private Boolean responseTruncated;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_stacktrace", columnDefinition = "TEXT")
    private String errorStacktrace;

    @Column(name = "meta", columnDefinition = "jsonb")
    private String meta;
}

