package com.gal.afiliaciones.config.interceptor;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LogTransactionInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm:ss");
    private static final String DETAIL_LABEL = "detalle";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extrae el token del header Authorization
        String authHeader = request.getHeader("Authorization");
        String username = null;
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
        // Si no hay token o usuario, se asume "Sistema" (acción automática)
        if (username == null || username.isEmpty()) {
            username = "Sistema";
        }
        MDC.put("usuario", username);
        // Se establecen otros campos
        MDC.put("fecha_ejecucion", LocalDateTime.now().format(FORMATTER));
        // Por ejemplo, el evento puede ser la URL solicitada
        MDC.put("evento", request.getRequestURI());
        // Inicialmente, se pueden dejar en blanco la respuesta y detalle
        MDC.put("respuesta", "");
        MDC.put(DETAIL_LABEL, "");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Actualiza la respuesta con el código HTTP
        MDC.put("respuesta", String.valueOf(response.getStatus()));
        // Actualiza el detalle según si hubo excepción o no
        if (ex != null) {
            MDC.put(DETAIL_LABEL, ex.getMessage());
        } else {
            MDC.put(DETAIL_LABEL, "Operación exitosa");
        }
        // Limpia el MDC para evitar contaminación entre peticiones
        MDC.clear();
    }
}