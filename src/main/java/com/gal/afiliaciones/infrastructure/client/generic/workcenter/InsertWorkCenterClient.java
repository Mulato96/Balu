package com.gal.afiliaciones.infrastructure.client.generic.workcenter;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsertWorkCenterClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object insertWorkCenter(WorkCenterRequest request) {
        String url = properties.getInsertWorkCenterUrl();
        return busTokenService
                .exchange(HttpMethod.POST, url, request, Object.class).block();
    }

}
