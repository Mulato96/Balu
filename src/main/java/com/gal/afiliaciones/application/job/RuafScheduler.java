package com.gal.afiliaciones.application.job;

import com.gal.afiliaciones.application.service.ruaf.RuafService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

@RequiredArgsConstructor
public class RuafScheduler {

    private final RuafService service;

    @Scheduled(cron = "0 20 00 * * MON")
    public void generateRuafFiles() throws IOException {
        service.generateFiles();
    }

}
