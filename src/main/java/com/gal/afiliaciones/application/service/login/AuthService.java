package com.gal.afiliaciones.application.service.login;

import com.gal.afiliaciones.infrastructure.enums.TypeUser;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String documentType, String username, String password, TypeUser userType);
    String logout(String refreshToken);
    Map<String, Object> refreshToken(String refreshToken);
    boolean isValidToken(String token);
}