package com.gal.afiliaciones.infrastructure.controller.updatedatesemployee;

import com.gal.afiliaciones.application.service.updatedatesemployee.DateUpdateService;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/update-dates")
public class DateUpdateController {

    private static final String ROL_FUNCIONARIO = "funcionario";
    private static final String MESSAGE_REQUIRED_ROL = "Requiere rol";
    private final DateUpdateService service;

    public DateUpdateController(DateUpdateService service) {
        this.service = service;
    }

    @PostMapping("/consult-links")
    @Operation(summary = "Consultar información básica (requiere rol funcionario via query param)")
    public ResponseEntity<ApiResponse<List<VinculacionDTO>>> consultLinks(
            @RequestBody VinculacionQueryDTO query,
            @RequestParam(value = "role", required = false) String roleParam
    ) {
        if (!hasFuncionario(roleParam)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MESSAGE_REQUIRED_ROL + ROL_FUNCIONARIO + "'.");
        }

        List<VinculacionDTO> result = service.consultLinks(query);
        return ResponseEntity.ok(new ApiResponse<>("Consulta exitosa", result));
    }

    @PostMapping("/update-coverage-date")
    @Operation(summary = "Consultar información básica (requiere rol funcionario via query param)")
    public ResponseEntity<ApiResponse<String>> updateDateCoverage(
            @RequestBody UpdateCoverageDateDTO updateDto,
            @RequestParam(value = "role", required = false) String roleParam
    ) {
        if (!hasFuncionario(roleParam)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MESSAGE_REQUIRED_ROL + ROL_FUNCIONARIO + "'.");
        }

        service.updateDateCoverage(updateDto);
        return ResponseEntity.ok(new ApiResponse<>("Fecha de cobertura actualizada exitosamente.", null));
    }


    @GetMapping("/detail/{tipo}/{id}")
    @Operation(summary = "Consultar detalle de registro (requiere tipo Vinculacion y Id vinculacion y rol funcionario via query param)")
    public ResponseEntity<ApiResponse<VinculacionDetalleDTO>> getLinksDetail(
            @RequestParam(value = "role", required = false) String roleParam,
            @PathVariable String tipo,
            @PathVariable Long id) {
        if (!hasFuncionario(roleParam)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MESSAGE_REQUIRED_ROL + ROL_FUNCIONARIO + "'.");
        }
        VinculacionDetalleDTO detalle = service.getLinksDetail(tipo, id);
        return ResponseEntity.ok(new ApiResponse<>("Detalle de vinculación obtenido exitosamente.", detalle));
    }

    private boolean hasFuncionario(String qp) {
        String roles = normalizeRoles(qp);
        if (containsFuncionario(roles)) return true;
        return containsFuncionario(roles);
    }

    private String normalizeRoles(String s) {
        if (s == null) return null;
        String t = s.replace('\u0000', ' ')
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("%0A", " ")
                .replace("%0a", " ")
                .trim()
                .toLowerCase();
        return t.replaceAll("[,;\\s]+", " ");
    }

    private boolean containsFuncionario(String rolesNorm) {
        if (rolesNorm == null || rolesNorm.isBlank()) return false;
        if (ROL_FUNCIONARIO.equals(rolesNorm)) return true;
        for (String token : rolesNorm.split(" ")) {
            if (ROL_FUNCIONARIO.equals(token)) return true;
        }
        return rolesNorm.contains(ROL_FUNCIONARIO);
    }
}