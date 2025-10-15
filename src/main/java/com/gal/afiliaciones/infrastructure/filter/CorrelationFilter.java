package com.gal.afiliaciones.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet filter that establishes correlation context for request tracing.
 * Part of integrations v2 architecture.
 * 
 * Sets up MDC (Mapped Diagnostic Context) with correlation ID and request details
 * that can be used by outbound HTTP call interceptors.
 */
@Component
@Order(1) // Execute early in filter chain
@Slf4j
public class CorrelationFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    
    // MDC keys
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_ORIGIN_METHOD = "originMethod";
    public static final String MDC_ORIGIN_PATH = "originPath";
    public static final String MDC_ORIGIN_IP = "originIp";
    public static final String MDC_USER_ID = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            setupCorrelationContext(httpRequest);
            chain.doFilter(request, response);
        } finally {
            // Always clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    private void setupCorrelationContext(HttpServletRequest request) {
        // Correlation ID - use existing or generate new
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                .filter(id -> !id.trim().isEmpty())
                .orElse(UUID.randomUUID().toString());
        
        // Request details
        String method = request.getMethod();
        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        // User ID from JWT or session (if available)
        String userId = extractUserId(request);
        
        // Set in MDC for use by interceptors
        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_ORIGIN_METHOD, method);
        MDC.put(MDC_ORIGIN_PATH, path);
        MDC.put(MDC_ORIGIN_IP, clientIp);
        
        if (userId != null) {
            MDC.put(MDC_USER_ID, userId);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Correlation context established: correlationId={}, method={}, path={}, ip={}", 
                correlationId, method, path, clientIp);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header first (for load balancers/proxies)
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            // Take first IP in case of multiple proxies
            return forwardedFor.split(",")[0].trim();
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
    
    private String extractUserId(HttpServletRequest request) {
        // Try to extract user ID from JWT token or session
        // This is a placeholder - implement based on your authentication mechanism
        
        // Example: Extract from JWT subject claim
        try {
            // If using Spring Security with JWT
            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // if (auth instanceof JwtAuthenticationToken jwtToken) {
            //     return jwtToken.getToken().getSubject();
            // }
            
            // For now, return null - can be enhanced based on auth setup
            return null;
        } catch (Exception e) {
            log.debug("Failed to extract user ID from request", e);
            return null;
        }
    }
}
