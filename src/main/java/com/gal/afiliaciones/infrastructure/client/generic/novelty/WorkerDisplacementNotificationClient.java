package com.gal.afiliaciones.infrastructure.client.generic.novelty;

import com.gal.afiliaciones.application.service.positiva.PositivaLogService;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerDisplacementNotificationRequest;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component("noveltyWorkerDisplacementNotificationClient")
@RequiredArgsConstructor
public class WorkerDisplacementNotificationClient {

    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;
    private final PositivaLogService positivaLogService;

    private static final String SERVICE_NAME = "WorkerDisplacementNotificationClient";

    public String sendRaw(WorkerDisplacementNotificationRequest request) {
        String url = properties.getWorkerDisplacementNotificationUrl();
        try {
            String response = busTokenService
                    .exchange(HttpMethod.POST, url, request, String.class)
                    .block();
            // We do not know the success schema yet; persist raw body as http trace (200)
            positivaLogService.save(
                    SERVICE_NAME,
                    "notificacionDesplazamientoTrabajador",
                    request.getIdTipoDoc(),
                    request.getIdPersona(),
                    null,
                    null,
                    null,
                    null,
                    String.valueOf(200),
                    null,
                    response,
                    200
            );
            return response;
        } catch (WebClientResponseException ex) {
            // Persist HTTP error with raw body
            positivaLogService.saveHttp(
                    SERVICE_NAME,
                    "notificacionDesplazamientoTrabajador",
                    request.getIdTipoDoc(),
                    request.getIdPersona(),
                    null,
                    null,
                    null,
                    null,
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }
}


