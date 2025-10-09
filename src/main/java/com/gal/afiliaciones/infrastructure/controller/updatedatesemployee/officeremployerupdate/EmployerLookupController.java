package com.gal.afiliaciones.infrastructure.controller.updatedatesemployee.officeremployerupdate;

import com.gal.afiliaciones.application.service.officeremployerupdate.EmployerLookupService;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerBasicProjection;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepViewDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepUpdateRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/officer/employers")
@CrossOrigin(origins = "*")
public class EmployerLookupController {

    private static final String MSG_ACCESS_DENIED = "Acceso restringido: se requiere rol Funcionario";
    private static final String MSG_NOT_FOUND = "Registro no encontrado, revisa la información e intenta nuevamente";
    private static final String MSG_SUCCESS = "Actualización realizada de forma exitosa";
    private static final String MSG_NO_RECORDS = "No se encontraron registros para actualizar con el documento especificado";

    private final EmployerLookupService service;

    public EmployerLookupController(EmployerLookupService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findBasic(@RequestParam String docType,
                                            @RequestParam String docNumber) {

        if (!hasFuncionarioRole()) {
            return ResponseEntity.status(403).body(jsonMsg(MSG_ACCESS_DENIED));
        }

        Optional<EmployerBasicProjection> res = service.findBasic(docType, docNumber);
        return res.<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(jsonMsg(MSG_NOT_FOUND)));
    }

    @PutMapping("/basic")
    public ResponseEntity<Object> updateBasic(@RequestBody EmployerUpdateDTO dto) {

        if (!hasFuncionarioRole()) {
            return ResponseEntity.status(403).body(jsonMsg(MSG_ACCESS_DENIED));
        }

        int updated = service.updateBasic(dto);
        if (updated == 0) {
            return ResponseEntity.status(404).body(jsonMsg(MSG_NO_RECORDS));
        }
        return ResponseEntity.ok(jsonMsg(MSG_SUCCESS));
    }

    @GetMapping("/legal-rep/search")
    public ResponseEntity<Object> findLegalRep(@RequestParam String docType,
                                               @RequestParam String docNumber) {

        if (!hasFuncionarioRole()) {
            return ResponseEntity.status(403).body(jsonMsg(MSG_ACCESS_DENIED));
        }

        Optional<LegalRepViewDTO> res = service.findLegalRep(docType, docNumber);
        return res.<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(jsonMsg(MSG_NOT_FOUND)));
    }

    @PutMapping("/legal-rep/basic")
    public ResponseEntity<Object> updateLegalRep(@RequestBody LegalRepUpdateRequestDTO dto) {

        if (!hasFuncionarioRole()) {
            return ResponseEntity.status(403).body(jsonMsg(MSG_ACCESS_DENIED));
        }

        int updated = service.updateLegalRep(dto);
        if (updated == 0) {
            return ResponseEntity.status(404).body(jsonMsg(MSG_NO_RECORDS));
        }
        return ResponseEntity.ok(jsonMsg(MSG_SUCCESS));
    }

    private static Map<String, String> jsonMsg(String m) {
        return Map.of("message", m);
    }

    private boolean hasFuncionarioRole() {
        Jwt jwt = currentJwt();
        if (jwt == null) {
            return false;
        }
        Object groupsObj = jwt.getClaim("groups");
        if (groupsObj instanceof Collection<?> c) {
            for (Object g : c) {
                if (g != null && "/Funcionario".equalsIgnoreCase(String.valueOf(g))) {
                    return true;
                }
            }
        }
        return false;
    }

    private Jwt currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return (principal instanceof Jwt jwt) ? jwt : null;
    }
}