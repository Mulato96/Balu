package com.gal.afiliaciones.infrastructure.controller.inactiveusers;


import com.gal.afiliaciones.application.service.inactiveusers.IInactiveUsersService;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inactive-pre-registred-users")
@RequiredArgsConstructor
public class InactiveUsersController {

    private final IInactiveUsersService service;

    @PostMapping("/active-account")
    public ResponseEntity<String> validOtpAndActiveAccount(@RequestBody OTPRequestDTO request) {
        return ResponseEntity.ok(service.validOtpAndActiveAccount(request));
    }
}
