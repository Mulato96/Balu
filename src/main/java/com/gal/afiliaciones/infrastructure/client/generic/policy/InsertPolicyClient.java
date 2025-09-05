package com.gal.afiliaciones.infrastructure.client.generic.policy;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsertPolicyClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insert(InsertPolicyRequest request) {
        return busTokenService.exchange(
                HttpMethod.POST,
                properties.getInsertPolicyUrl(),
                request,
                Object.class
        ).block();
    }
}
