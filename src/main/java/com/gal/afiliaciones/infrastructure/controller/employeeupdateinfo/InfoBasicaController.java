package com.gal.afiliaciones.infrastructure.controller.employeeupdateinfo;

import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.infrastructure.dto.InfoBasicaDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/infobasica")
public class InfoBasicaController {

    private static final String ROL_FUNCIONARIO = "funcionario";
    private final InfoBasicaService service;

    public InfoBasicaController(InfoBasicaService service) {
        this.service = service;
    }

    @GetMapping("/{documento}")
    @Operation(summary = "Consultar información básica (requiere rol funcionario via query param o header)")
    public ResponseEntity<InfoBasicaDTO> consultar(
            @PathVariable String documento,
            @Parameter(description = "Rol del usuario; debe incluir 'funcionario'")
            @RequestParam(value = "role", required = false) String roleParam,
            @RequestHeader(name = "X-User-Role", required = false) String roleHeader
    ) {
        if (!hasFuncionario(roleParam, roleHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requiere rol '" + ROL_FUNCIONARIO + "'.");
        }
        return ResponseEntity.ok(service.consultarInfoBasica(documento));
    }

    @PutMapping("/{documento}")
    @Operation(summary = "Actualizar información básica (requiere rol funcionario via query param o header)")
    public ResponseEntity<Void> actualizar(
            @PathVariable String documento,
            @RequestBody UpdateInfoBasicaRequest body,
            @Parameter(description = "Rol del usuario; debe incluir 'funcionario'")
            @RequestParam(value = "role", required = false) String roleParam,
            @RequestHeader(name = "X-User-Role", required = false) String roleHeader,
            @Parameter(description = "Documento del usuario logueado (opcional) para bloquear auto-actualización")
            @RequestParam(value = "userDoc", required = false) String userDoc
    ) {
        if (!hasFuncionario(roleParam, roleHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requiere rol '" + ROL_FUNCIONARIO + "'.");
        }
        service.actualizarInfoBasica(documento, body, userDoc);
        return ResponseEntity.noContent().build();
    }

    private boolean hasFuncionario(String qp, String hdr) {
        String roles = normalizeRoles(qp);
        if (containsFuncionario(roles)) return true;
        roles = normalizeRoles(hdr);
        return containsFuncionario(roles);
    }

    private String normalizeRoles(String s) {
        if (s == null) return null;
        String t = s.replace('\u0000',' ')
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