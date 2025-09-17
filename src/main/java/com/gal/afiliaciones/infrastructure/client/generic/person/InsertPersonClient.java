package com.gal.afiliaciones.infrastructure.client.generic.person;

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
public class InsertPersonClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    private final PositivaLogService positivaLogService;
    private final ObjectMapper objectMapper;

    public Object insertPerson(PersonRequest request) {
        String url = properties.getInsertPersonUrl();
        try {
            Object response = busTokenService
                    .exchange(HttpMethod.POST, url, request, Object.class).block();
            positivaLogService.saveFromResponse("InsertPersonClient", "insertPerson", request.getIdTipoDoc(), request.getIdPersona(), buildFullName(request), null, null, null, response);
            return response;
        } catch (Exception ex) {
            String code = ex instanceof WebClientResponseException wcre ? String.valueOf(wcre.getStatusCode().value()) : "EX";
            String message = ex instanceof WebClientResponseException wcre ? wcre.getResponseBodyAsString() : ex.getMessage();
            positivaLogService.saveError("InsertPersonClient", "insertPerson", request.getIdTipoDoc(), request.getIdPersona(), buildFullName(request), null, null, null, code, message);
            throw ex;
        }
    }

    private String buildFullName(PersonRequest request) {
        String nombre1 = request.getNombre1() != null ? request.getNombre1() : "";
        String nombre2 = request.getNombre2() != null ? request.getNombre2() : "";
        String apellido1 = request.getApellido1() != null ? request.getApellido1() : "";
        String apellido2 = request.getApellido2() != null ? request.getApellido2() : "";
        String fullName = (nombre1 + " " + nombre2 + " " + apellido1 + " " + apellido2).trim();
        return fullName.isEmpty() ? null : fullName.replaceAll("\\s+", " ");
    }

    private String toJson(Object response) {
        try {
            return response != null ? objectMapper.writeValueAsString(response) : null;
        } catch (Exception e) {
            return String.valueOf(response);
        }
    }
}
