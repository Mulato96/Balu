package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
 
 

@Component
@RequiredArgsConstructor
public class InsertLegalRepresentativeClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    

    public Object insertLegalRepresentative(LegalRepresentativeRequest request) {
        String url = properties.getLegalRepresentativeUrl();
        return busTokenService
                .exchange(HttpMethod.POST, url, request, Object.class)
                .block();
    }

    

}
