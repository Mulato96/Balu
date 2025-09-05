package com.gal.afiliaciones.infrastructure.client.generic.businessgroup;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsultBusinessGroupClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<List<BusinessGroupResponse>> getBusinessGroups() {
        String url = properties.getBusinessGroupUrl();
        return busTokenService.getList(url, new ParameterizedTypeReference<>() {});
    }
}
