package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsultLegalRepresentativeClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<LegalRepresentativeResponse>> consult(String tipoDoc, String idEmpresa, Integer idSubEmpresa) {
        String url = properties.getLegalRepresentativeUrl()
                + "?idTipoDoc=" + tipoDoc
                + "&idEmpresa=" + idEmpresa
                + "&idSubEmpresa=" + idSubEmpresa;

        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}
