package com.gal.afiliaciones.infrastructure.controller.integration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient; // DEPRECATED: Use IntegrationsV2TestController with SatConsultTransferableEmployerClientV2 instead
import com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerRetirementNoveltyClient;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyResponse;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.security.ExternalServiceException;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.security.ExternalServiceException;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import com.gal.afiliaciones.infrastructure.security.SiarpTokenService;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusRawClient;
import java.util.Map;

/**
 * @deprecated Use {@link IntegrationsV2TestController} instead.
 * This controller uses deprecated clients without automatic telemetry tracking.
 * Migrate to IntegrationsV2TestController for full HTTP monitoring via http_outbound_call table.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/integration-test")
@Tag(name = "Integration Test", description = "Endpoints for testing external service integration plumbing")
@RequiredArgsConstructor
@Deprecated(since = "2.0", forRemoval = true)
public class IntegrationTestController {

    private final SatConsultTransferableEmployerClient satClient;
    private final WorkerRetirementNoveltyClient workerRetirementNoveltyClient;
    private final BusTokenService busTokenService; // kept for other tests
    private final SiarpTokenService siarpTokenService; // kept for other tests
    private final ConsultSiarpAffiliateClient consultSiarpAffiliateClient;
    private final ConsultSiarpAffiliateStatusClient consultSiarpAffiliateStatusClient;
    private final ConsultSiarpAffiliateStatusRawClient consultSiarpAffiliateStatusRawClient;

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


    @PostMapping("/positiva/novelty/worker-retirement")
    @Operation(summary = "Novedad Retiro Trabajador (Positiva)", description = "Invoca el endpoint de Positiva para registrar la novedad de retiro del trabajador.")
    public ResponseEntity<WorkerRetirementNoveltyResponse> workerRetirement(@RequestBody WorkerRetirementNoveltyRequest request) {
        WorkerRetirementNoveltyResponse response = workerRetirementNoveltyClient.send(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/siarp/consultaAfiliado2")
    @Operation(summary = "Consulta Afiliado2 (SIARP)", description = "Calls SIARP consultaAfiliado2. Body must include tDoc and idAfiliado.")
    public ResponseEntity<Object> siarpConsultaAfiliado2(@RequestBody Map<String, String> payload) {
        String tDoc = payload.get("tDoc");
        String idAfiliado = payload.get("idAfiliado");
        log.info("[INT TEST] SIARP consultaAfiliado2 tDoc={}, idAfiliado={}", tDoc, idAfiliado);
        var list = consultSiarpAffiliateClient.consult(tDoc, idAfiliado).blockOptional().orElse(java.util.List.of());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/siarp/consultaEstadoAfiliado")
    @Operation(summary = "Consulta Estado Afiliado (SIARP)", description = "Calls SIARP consultaEstadoAfiliado. Body must include tDocEmp, idEmp, tDocAfi, idAfi.")
    public ResponseEntity<Object> siarpConsultaEstadoAfiliado(@RequestBody Map<String, String> payload) {
        String tDocEmp = payload.get("tDocEmp");
        String idEmp = payload.get("idEmp");
        String tDocAfi = payload.get("tDocAfi");
        String idAfi = payload.get("idAfi");
        log.info("[INT TEST] SIARP consultaEstadoAfiliado tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}", tDocEmp, idEmp, tDocAfi, idAfi);
        var list = consultSiarpAffiliateStatusClient.consult(tDocEmp, idEmp, tDocAfi, idAfi).blockOptional().orElse(java.util.List.of());
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/siarp/consultaEstadoAfiliado/raw", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consulta Estado Afiliado RAW (SIARP)", description = "Calls SIARP consultaEstadoAfiliado and returns the exact raw JSON as received.")
    public ResponseEntity<String> siarpConsultaEstadoAfiliadoRaw(@RequestBody Map<String, String> payload) {
        String tDocEmp = payload.get("tDocEmp");
        String idEmp = payload.get("idEmp");
        String tDocAfi = payload.get("tDocAfi");
        String idAfi = payload.get("idAfi");
        log.info("[INT TEST] SIARP consultaEstadoAfiliado RAW tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}", tDocEmp, idEmp, tDocAfi, idAfi);
        String raw = consultSiarpAffiliateStatusRawClient.consultRaw(tDocEmp, idEmp, tDocAfi, idAfi)
                .blockOptional().orElse("[]");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);

    }
}


