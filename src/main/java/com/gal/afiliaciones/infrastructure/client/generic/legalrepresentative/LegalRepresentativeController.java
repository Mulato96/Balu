package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/legal-representative")
@RequiredArgsConstructor
public class LegalRepresentativeController {
    private final ConsultLegalRepresentativeClient client;

    @GetMapping
    public Mono<List<LegalRepresentativeResponse>> get(
            @RequestParam String tipoDoc,
            @RequestParam String idEmpresa,
            @RequestParam Integer idSubEmpresa) {

        return client.consult(tipoDoc, idEmpresa, idSubEmpresa);
    }

}
