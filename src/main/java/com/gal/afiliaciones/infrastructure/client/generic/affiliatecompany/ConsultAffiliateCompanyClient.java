package com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsultAffiliateCompanyClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<AffiliateCompanyResponse>> consultAffiliate(String tipoDoc, String idAfiliado) {
        String url = properties.getBusUrlAffiliate()
                + "?idTipoDoc=" + tipoDoc + "&idAfiliado=" + idAfiliado;
        return busTokenService.getList(url,
                new ParameterizedTypeReference<>() {
                });
    }
}
