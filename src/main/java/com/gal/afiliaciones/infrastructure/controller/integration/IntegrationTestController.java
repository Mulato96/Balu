package com.gal.afiliaciones.infrastructure.controller.integration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerDisplacementNotificationClient;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerDisplacementNotificationRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.security.ExternalServiceException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/integration-test")
@Tag(name = "Integration Test", description = "Endpoints for testing external service integration plumbing")
@RequiredArgsConstructor
public class IntegrationTestController {

    private final SatConsultTransferableEmployerClient satClient;
    private final WorkerDisplacementNotificationClient workerDisplacementClient;

    @PostMapping("/echo")
    @Operation(summary = "Echo payload", description = "Returns the same payload received. Placeholder to wire an external integration call.")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
        log.info("Echo payload received: {}", payload);

        // Integration hook: call the desired external integration service here using the payload

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/sat/transferable-employer")
    @Operation(summary = "Consulta Empleador Trasladable (SAT)", description = "Calls SAT endpoint to check if an employer is transferable.")
    public ResponseEntity<String> transferableEmployer(@RequestBody TransferableEmployerRequest request) {
        try {
            // External call using common practices (BusTokenService inside client)
            String raw = satClient.consultRaw(request);
            return ResponseEntity.ok(raw);
        } catch (ExternalServiceException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBody());
        }
    }

    @PostMapping("/positiva/novelty/worker-displacement")
    @Operation(summary = "Notificación Desplazamiento Trabajador (Positiva)", description = "Invoca el endpoint de Positiva para notificar desplazamiento del trabajador. Devuelve el cuerpo crudo hasta conocer el esquema definitivo.")
    public ResponseEntity<String> workerDisplacement(@RequestBody WorkerDisplacementNotificationRequest request) {
        String raw = workerDisplacementClient.sendRaw(request);
        return ResponseEntity.ok(raw);
    }
}


