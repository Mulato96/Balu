package com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DependentRelationshipClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insert(DependentRelationshipRequest request) {
        return busTokenService.exchange(
                HttpMethod.POST,
                properties.getDependentRelationshipUrl(),
                request,
                Object.class
        ).block();
    }
}
