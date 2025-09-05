package com.gal.afiliaciones.infrastructure.client.generic.employer;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsertEmployerClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insertEmployer(EmployerRequest request) {
        String url = properties.getBusUrlEmployer();
        return busTokenService
                .exchange(HttpMethod.POST, url, request, Object.class).block();
    }
}
