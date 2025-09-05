package com.gal.afiliaciones.config.usernotaffiliation;

import com.gal.afiliaciones.application.service.impl.ConsultCertificateByUserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ConfigDeleteUsersTimeExpired implements CommandLineRunner {

    private final ConsultCertificateByUserServiceImpl consultCertificateService;

    @Override
    public void run(String... args) throws Exception {
        consultCertificateService.executeTask();
    }
}
