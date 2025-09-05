package com.gal.afiliaciones.infrastructure.client.generic.person;

import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalClient;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {
    private final PersonlClient client;

    @GetMapping
    public Mono<List<PersonResponse>> get(
            @RequestParam String tipoDoc,
            @RequestParam String idEmpresa) {

        return client.consult(tipoDoc, idEmpresa);
    }

}
