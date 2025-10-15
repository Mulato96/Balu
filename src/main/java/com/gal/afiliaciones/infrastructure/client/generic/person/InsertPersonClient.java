package com.gal.afiliaciones.infrastructure.client.generic.person;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
 
 

@Component
@RequiredArgsConstructor
public class InsertPersonClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    

    public Object insertPerson(PersonRequest request) {
        String url = properties.getBusUrlPerson();
        return busTokenService
                .exchange(HttpMethod.POST, url, request, Object.class)
                .block();
    }

    
    
}
