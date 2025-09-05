package com.gal.afiliaciones.config.interceptor;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Slf4j
public class TransactionLogFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm:ss");
    private static final String RESPUESTA = "respuesta";
    private static final String DETALLE = "detalle";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String uri = request.getRequestURI();
        // Si la URI corresponde a endpoints de Prometheus o Swagger, se omite el logging transaccional
        if (uri.contains("actuator/prometheus") || uri.contains("swagger") || uri.contains("api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Inicializa los valores en el MDC
        MDC.put("fecha_ejecucion", LocalDateTime.now().format(FORMATTER));
        MDC.put("evento", request.getRequestURI());

        // Aquí podrías extraer el token y obtener el usuario, similar a tu interceptor
        // Por simplicidad, si no hay token se asume "Sistema"
        String authHeader = request.getHeader("Authorization");
        String username = "Sistema";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                // Intenta obtener "preferred_username", sino usa "name"
                username = claims.getStringClaim("email");
                if (username == null || username.isEmpty()) {
                    username = claims.getStringClaim("preferred_username");
                }
            } catch (ParseException e) {
                // Si falla la extracción, se puede asignar un valor por defecto
                username = "desconocido";
            }
        }
        MDC.put("usuario", username);
        // Inicializa campos de respuesta y detalle (se actualizarán después)
        MDC.put(RESPUESTA, "");
        MDC.put(DETALLE, "");

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (response.getStatus() >= 400) {
                String responseBody = "";
                byte[] content = wrappedResponse.getContentAsByteArray();
                responseBody = new String(content, wrappedResponse.getCharacterEncoding());

                // Actualiza el MDC con la respuesta
                if(content.length > 0) {
                    MDC.put(DETALLE, "Error en la transacción->> " + responseBody);
                }else{
                    MDC.put(DETALLE, "Error en la transacción");
                }
            }else{
                MDC.put(DETALLE, "Transacción exitosa");
            }
            // Actualiza el MDC con la respuesta
            MDC.put(RESPUESTA, String.valueOf(response.getStatus()));
            log.info("Transaction log details: usuario={}, fecha_ejecucion={}, evento={}, respuesta={}, detalle={}",
                    MDC.get("usuario"), MDC.get("fecha_ejecucion"), MDC.get("evento"),
                    MDC.get(RESPUESTA), MDC.get(DETALLE));
            // Podrías actualizar "detalle" en base a algún error si se detecta
            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }
}