package com.gal.afiliaciones.infrastructure.client.generic.employer;


import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeResponse;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ConsultEmployerClient {
    private final BusTokenService genericWebClient;
    private final AffiliationProperties properties;

    public Mono<List<EmployerResponse>> consult(String tipoDoc, String idEmpresa, Integer idSubEmpresa) {
        String url = properties.getBusUrlEmployer()
                + "?idTipoDoc=" + tipoDoc
                + "&idEmpresa=" + idEmpresa
                + "&idSubEmpresa=" + idSubEmpresa;

        return genericWebClient.getList(url, new ParameterizedTypeReference<>() {});
    }
}
