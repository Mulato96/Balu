package com.gal.afiliaciones.infrastructure.controller.billing;

import com.gal.afiliaciones.application.service.billing.BillingService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService service;

    @GetMapping("/create-manually")
    public ResponseEntity<BodyResponseConfig<Void>> generateBillingManually() {
        log.info("Generate billing manually");
        service.generateBilling();
        return ResponseEntity.ok(new BodyResponseConfig<>("Generate successful"));
    }

}
