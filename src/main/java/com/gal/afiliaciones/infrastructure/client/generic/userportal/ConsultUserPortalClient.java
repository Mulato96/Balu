package com.gal.afiliaciones.infrastructure.client.generic.userportal;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsultUserPortalClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<UserPortalResponse>> consult(String idTipoDocPersona, String idPersona) {
        String url = properties.getUserPortalUrl()
                + "?idTipoDocPersona=" + idTipoDocPersona
                + "&idPersona=" + idPersona;
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}
