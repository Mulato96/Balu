package com.gal.afiliaciones.infrastructure.client.generic.person;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonlClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<PersonResponse>> consult(String idTipoDocPersona, String idPersona) {
        String url = properties.getBusUrlPerson()
                + "?idTipoDoc=" + idTipoDocPersona
                + "&idPersona=" + idPersona;
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}
