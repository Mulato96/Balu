package com.gal.afiliaciones.infrastructure.client.generic.employer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/employer")
@RequiredArgsConstructor
public class EmployerController {

    private final ConsultEmployerClient consultEmployerClient;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<EmployerResponse>> getEmployer(
            @RequestParam String tipoDoc,
            @RequestParam String idEmpresa,
            @RequestParam Integer idSubEmpresa) {
        return consultEmployerClient.consult(tipoDoc, idEmpresa, idSubEmpresa);
    }
}
