package com.gal.afiliaciones.infrastructure.client.generic.independentrelationship;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
 
 

@Component
@RequiredArgsConstructor
public class IndependentContractRelationshipClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insert(IndependentContractRelationshipRequest request) {
        return busTokenService.exchange(
                HttpMethod.POST,
                properties.getIndependentContractRelationshipUrl(),
                request,
                Object.class
        ).block();
    }

    
}
