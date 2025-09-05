package com.gal.afiliaciones.infrastructure.controller.login;

import com.gal.afiliaciones.application.service.login.AuthService;
import com.gal.afiliaciones.infrastructure.dto.login.LogOutCredentials;
import com.gal.afiliaciones.infrastructure.dto.login.UserCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserCredentials credentials) {
        return ResponseEntity.ok(authService.login(credentials.getTypeDocument(),credentials.getUsername(), credentials.getPassword(), credentials.getTypeUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogOutCredentials logOutCredentials) {
        return ResponseEntity.ok(authService.logout(logOutCredentials.getRefreshToken()));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody LogOutCredentials logOutCredentials) {
        return ResponseEntity.ok(authService.refreshToken(logOutCredentials.getRefreshToken()));
    }

}