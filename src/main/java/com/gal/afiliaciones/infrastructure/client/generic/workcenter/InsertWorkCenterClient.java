package com.gal.afiliaciones.infrastructure.client.generic.workcenter;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.application.service.positiva.PositivaLogService;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class InsertWorkCenterClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    private final PositivaLogService positivaLogService;
    private final ObjectMapper objectMapper;

    public Object insertWorkCenter(WorkCenterRequest request) {
        String url = properties.getInsertWorkCenterUrl();
        try {
            Object response = busTokenService
                    .exchange(HttpMethod.POST, url, request, Object.class).block();
            positivaLogService.saveFromResponse("InsertWorkCenterClient", "insertWorkCenter", request.getTipoDocEmp(), request.getNumeDocEmp(), null, null, null, null, response);
            return response;
        } catch (Exception ex) {
            String code = ex instanceof WebClientResponseException wcre ? String.valueOf(wcre.getStatusCode().value()) : "EX";
            String message = ex instanceof WebClientResponseException wcre ? wcre.getResponseBodyAsString() : ex.getMessage();
            positivaLogService.saveError("InsertWorkCenterClient", "insertWorkCenter", request.getTipoDocEmp(), request.getNumeDocEmp(), null, null, null, null, code, message);
            throw ex;
        }
    }

    private String toJson(Object response) {
        try {
            return response != null ? objectMapper.writeValueAsString(response) : null;
        } catch (Exception e) {
            return String.valueOf(response);
        }
    }

}
