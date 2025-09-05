package com.gal.afiliaciones.infrastructure.client.generic.volunteer;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VolunteerRelationshipClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insert(VolunteerRelationshipRequest request) {
        String url = properties.getInsertVolunteerUrl();
        return busTokenService
                .exchange(HttpMethod.POST, url, request, Object.class).block();
    }

}
