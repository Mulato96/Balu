package com.gal.afiliaciones.infrastructure.controller.employer;

import com.gal.afiliaciones.application.service.employer.EmployerEmployeeQueryService;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeListResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeQueryRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Employer Employee Query", description = "API para consultar empleados usando los 4 parámetros específicos")
public class EmployerEmployeeQueryController {

    private final EmployerEmployeeQueryService employerEmployeeQueryService;
    private final com.gal.afiliaciones.application.service.siarp.SiarpAffiliateStatusGateway siarpAffiliateStatusGateway;
    private final com.gal.afiliaciones.application.service.siarp.SiarpAffiliateGateway siarpAffiliateGateway;
    private final ObjectMapper objectMapper;

    @GetMapping("/test")
    @Operation(summary = "Endpoint de prueba con datos de ejemplo")
    public ResponseEntity<EmployerEmployeeListResponseDTO> testEndpoint() {
        log.info("=== TEST ENDPOINT CALLED ===");
        
        // Crear datos de ejemplo
        EmployerEmployeeDTO employee1 = new EmployerEmployeeDTO();
        employee1.setIdTipoDocEmp("NI");
        employee1.setIdEmpresa("89999906101");
        employee1.setRazonSocial("SECRETARIA GENERAL DE LA ALCALDIA MAYOR DE BOGOTA DC SGAM");
        employee1.setSubEmpresa(1);
        employee1.setIdTipoDocPer("CC");
        employee1.setIdPersona("79543633");
        employee1.setNombre1("WILLIAM");
        employee1.setNombre2("LEONARDO");
        employee1.setApellido1("VARGAS");
        employee1.setApellido2("HERRERA");
        employee1.setSexo("MASCULINO");
        employee1.setFechaInicioVinculacion("01-06-1997");
        employee1.setFechaFinVinculacion(null);
        employee1.setFechaNacimiento("05-07-1970");
        employee1.setIdAfp(14);
        employee1.setNombreAfp("COLPENSIONES ADMINISTRADORA COLOMBIANA DE PENSIONE");
        employee1.setIdEps("EPS008");
        employee1.setNombreEps("COMPENSAR E.P.S");
        employee1.setDireccionPersona("CRA. 74  44 - 29 SUR INT.15 APTO.345");
        employee1.setIdArp(10);
        employee1.setNombreArp("INSTITUTO DE SEGUROS SOCIALES I.S.S.");
        employee1.setIdOcupacion(2869);
        employee1.setNombreOcupacion("AUXILIAR ADMINISTRATIVO");
        employee1.setSalarioMensual(3001419L);
        employee1.setIdDepartamento(11);
        employee1.setNombreDepartamento("BOGOTA D.C.");
        employee1.setIdMunicipio(1);
        employee1.setNombreMunicipio("BOGOTA D.C.");
        
        EmployerEmployeeListResponseDTO response = new EmployerEmployeeListResponseDTO();
        response.setEmployees(List.of(employee1));
        response.setTotalCount(1);
        response.setSuccess(true);
        response.setMessage("Datos de prueba generados correctamente");
        
        log.info("Test response generated with {} employees", response.getEmployees().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/query")
    @Operation(summary = "Consultar empleado con 4 parámetros específicos",
               description = "Consulta empleados usando tDocEmp, idEmp, tDocAfi, idAfi en ambas tablas")
    public ResponseEntity<List<EmployerEmployeeDTO>> queryEmployee(
            @RequestBody EmployerEmployeeQueryRequestDTO request) {
        
        log.info("=== EMPLOYER EMPLOYEE QUERY START ===");
        log.info("Request received: {}", request);
        log.info("Querying employee with parameters: tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}", 
                request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
        
        try {
            EmployerEmployeeListResponseDTO response = employerEmployeeQueryService.queryEmployeeByParameters2(request);
            log.info("Number of employees in response: {}",
                    response.getEmployees() != null ? response.getEmployees().size() : "null");
            
            if (response.getEmployees() != null && !response.getEmployees().isEmpty()) {
                log.info("First employee sample: ID_TIPO_DOC_EMP={}, ID_EMPRESA={}, NOMBRE1={}", 
                        response.getEmployees().get(0).getIdTipoDocEmp(),
                        response.getEmployees().get(0).getIdEmpresa(),
                        response.getEmployees().get(0).getNombre1());
            }
            
            log.info("=== EMPLOYER EMPLOYEE QUERY END ===");
            return ResponseEntity.ok(response.getEmployees() != null ? response.getEmployees() : List.of());
        } catch (Exception e) {
            log.error("Error in employer employee query: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/WSAlissta2/consultaAfiliado2")
    @Operation(summary = "Consulta afiliado estilo externo (GET)")
    public ResponseEntity<String> consultaAfiliado2(
            @RequestParam("tDoc") String tDoc,
            @RequestParam("idAfiliado") String idAfiliado,
            @RequestParam(value = "appSource", required = false) String appSource,
            @RequestHeader(value = "originator", required = false) String originator,
            @RequestHeader(value = "sender", required = false) String sender,
            @RequestHeader(value = "messageId", required = false) String messageId
    ) {
        log.info("[GET v2] consultaAfiliado2 tDoc={}, idAfiliado={}, appSource={}, originator={}, sender={}, messageId={}",
                tDoc, idAfiliado, appSource, originator, sender, messageId);

        // Default: return RAW JSON from SIARP if non-empty array; fallback to DTO list
        String raw = null;
        try {
            var opt = siarpAffiliateGateway.getAffiliate(tDoc, idAfiliado).blockOptional();
            if (opt.isPresent() && opt.get().rawJson().isPresent()) {
                raw = opt.get().rawJson().get();
            }
        } catch (Exception ex) {
            log.warn("[GET v2] SIARP RAW call failed, falling back to DTO: {}", ex.getMessage());
        }

        if (raw != null) {
            String trimmed = raw.trim();
            if (!("[]".equals(trimmed) || "[ ]".equals(trimmed))) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            }
        }

        // Fallback to DTO path
        EmployerEmployeeQueryRequestDTO req = new EmployerEmployeeQueryRequestDTO();
        req.setTDocAfi(tDoc);
        req.setIdAfi(idAfiliado);
        EmployerEmployeeListResponseDTO response = employerEmployeeQueryService.queryEmployeeByParameters2(req, appSource);
        try {
            String dtoJson = objectMapper.writeValueAsString(response.getEmployees() != null ? response.getEmployees() : List.of());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[GET v2] Failed to serialize DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
    }

    @GetMapping("/WSAlissta2/consultaEstadoAfiliado")
    @Operation(summary = "Consulta estado afiliado (GET)")
    public ResponseEntity<String> consultaEstadoAfiliado(
            @RequestParam("tDocEmp") String tDocEmp,
            @RequestParam("idEmp") String idEmp,
            @RequestParam("tDocAfi") String tDocAfi,
            @RequestParam("idAfi") String idAfi,
            @RequestParam(value = "appSource", required = false) String appSource,
            @RequestHeader(value = "originator", required = false) String originator,
            @RequestHeader(value = "sender", required = false) String sender,
            @RequestHeader(value = "messageId", required = false) String messageId
    ) {
        log.info("[GET v1] consultaEstadoAfiliado tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}, appSource={}, originator={}, sender={}, messageId={}",
                tDocEmp, idEmp, tDocAfi, idAfi, appSource, originator, sender, messageId);

        // Default: return RAW JSON if SIARP responded with a non-empty JSON array.
        // Fallback to DTO when SIARP returns empty array or a non-200 error.
        String raw = null;
        try {
            var opt = siarpAffiliateStatusGateway.getStatus(tDocEmp, idEmp, tDocAfi, idAfi).blockOptional();
            if (opt.isPresent() && opt.get().rawJson().isPresent()) {
                raw = opt.get().rawJson().get();
            }
        } catch (Exception ex) {
            log.warn("[GET v1] SIARP RAW call failed, falling back to DTO: {}", ex.getMessage());
        }

        if (raw != null) {
            String trimmed = raw.trim();
            if (!("[]".equals(trimmed) || "[ ]".equals(trimmed))) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            }
        }

        // Fallback to DTO path
        EmployerEmployeeQueryRequestDTO req = new EmployerEmployeeQueryRequestDTO();
        req.setTDocEmp(tDocEmp);
        req.setIdEmp(idEmp);
        req.setTDocAfi(tDocAfi);
        req.setIdAfi(idAfi);

        java.util.List<com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO> dtoList =
                employerEmployeeQueryService.queryEmployeeByParameters1(req, appSource);
        try {
            String dtoJson = objectMapper.writeValueAsString(dtoList);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[GET v1] Failed to serialize DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
    }
}
