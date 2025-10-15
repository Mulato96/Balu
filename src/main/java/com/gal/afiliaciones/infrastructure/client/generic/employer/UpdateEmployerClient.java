package com.gal.afiliaciones.infrastructure.client.generic.employer;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateEmployerClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object update(EmployerRequest request) {
        String url = properties.getBusUrlEmployer();
        return busTokenService
                .exchange(HttpMethod.PUT, url, request, Object.class)
                .block();
    }
}

