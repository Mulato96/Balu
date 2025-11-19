package com.gal.afiliaciones.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for test-only endpoints.
 * Ensures certain endpoints are only accessible from allowed hosts (localhost, dev, QA).
 * This is an infrastructure/security concern, not business logic.
 */
@Component
@Slf4j
public class TestEnvironmentValidator {

    private static final List<String> ALLOWED_HOSTS = Arrays.asList(
        "localhost",
        "127.0.0.1",
        "localhost:8080",
        "127.0.0.1:8080",
        "localhost:8081",
        "127.0.0.1:8081",
        "gal-back-dev.linktic.com",
        "gal-back-qa.linktic.com"
    );

    /**
     * Validates that the request comes from an allowed test environment.
     * Checks both Host header and X-Forwarded-Host (for proxied requests).
     * 
     * @param request The HTTP request to validate
     * @return true if request is from allowed host, false otherwise
     */
    public boolean isAllowedTestEnvironment(HttpServletRequest request) {
        String host = extractHost(request);
        boolean isAllowed = ALLOWED_HOSTS.stream()
                .anyMatch(host::equalsIgnoreCase);
        
        if (!isAllowed) {
            log.warn("Test endpoint access denied from host: {}", host);
        }
        
        return isAllowed;
    }

    /**
     * Extracts host from request, checking X-Forwarded-Host first (for load balancers).
     * 
     * @param request The HTTP request
     * @return The host string
     */
    public String extractHost(HttpServletRequest request) {
        // Check X-Forwarded-Host first (when behind a proxy/load balancer)
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.trim().isEmpty()) {
            return forwardedHost.split(",")[0].trim();
        }
        
        // Fallback to Host header
        String host = request.getHeader("Host");
        return host != null ? host.trim() : "";
    }
}

