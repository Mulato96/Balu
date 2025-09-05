package com.gal.afiliaciones.infrastructure.controller.noveltyruaf;

import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.infrastructure.utils.ByteArrayToMultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/noveltyruaf")
@Tag(name = "Novelty-RUAF-Controller", description = "Novedades RUAF")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class NoveltyRuafController {

    private final NoveltyRuafService service;

    @PostMapping("/createNoveltyWorkerRetirement")
    @Operation(summary = "Crear novedad retiro trabajador")
    public ResponseEntity<Boolean> createNoveltyWorkerRetirement() {
        Boolean response = service.executeWorkerRetirement();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generateFileRNRA")
    @Operation(summary = "Generar archivo RUAF RNRA")
    public ResponseEntity<String> generateFileRNRA() {
        String response = service.generateFileRNRA();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prueba")
    public ResponseEntity<ByteArrayToMultipartFile> prueba(){
        return ResponseEntity.ok().body(service.retryGeneratingFileRNRE());
    }

}
