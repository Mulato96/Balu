package com.gal.afiliaciones.infrastructure.controller.ruaf;

import com.gal.afiliaciones.application.service.ruaf.RuafService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("ruaf")
@CrossOrigin
@RequiredArgsConstructor
public class RuafController {

    private final RuafService service;

    @PostMapping("generate")
    public ResponseEntity<BodyResponseConfig<Void>> generateFiles() throws IOException {
        service.generateFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("find")
    public ResponseEntity<BodyResponseConfig<Page<RuafDTO>>> findFiles(@RequestParam Integer page, @RequestParam Integer size, @RequestBody(required = false) RuafFilterDTO filter) {
        return ResponseEntity.ok(new BodyResponseConfig<>(service.findAll(PageRequest.of(page, size), filter), ""));
    }

    @GetMapping("{id}")
    public ResponseEntity<BodyResponseConfig<String>> exportFile(@PathVariable Long id) {
        return ResponseEntity.ok(new BodyResponseConfig<>(service.exportFile(id), ""));
    }

    @GetMapping("retry/{id}")
    public ResponseEntity<BodyResponseConfig<String>> retry(@PathVariable Long id) {
        return ResponseEntity.ok(new BodyResponseConfig<>(service.retryFileGeneration(id), ""));
    }

}
