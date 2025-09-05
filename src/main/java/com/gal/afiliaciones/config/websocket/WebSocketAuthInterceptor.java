package com.gal.afiliaciones.config.websocket;

import com.gal.afiliaciones.application.service.login.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final AuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if(request instanceof ServletServerHttpRequest servletRequest){

            String token  = servletRequest.getServletRequest().getParameter("token");

            if(token != null && token.startsWith("Bearer ")){

                try {

                    token = token.substring(7);

                    return authService.isValidToken(token);
                } catch (Exception e) {
                    return false;
                }
            }

        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No se necesita implementación aquí
    }
}
