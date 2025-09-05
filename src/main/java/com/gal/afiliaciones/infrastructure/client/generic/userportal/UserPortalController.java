package com.gal.afiliaciones.infrastructure.client.generic.userportal;

import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.ConsultLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/user-portal")
@RequiredArgsConstructor
public class UserPortalController {
    private final ConsultUserPortalClient client;

    @GetMapping
    public Mono<List<UserPortalResponse>> get(
            @RequestParam String tipoDoc,
            @RequestParam String idEmpresa) {

        return client.consult(tipoDoc, idEmpresa);
    }

}
