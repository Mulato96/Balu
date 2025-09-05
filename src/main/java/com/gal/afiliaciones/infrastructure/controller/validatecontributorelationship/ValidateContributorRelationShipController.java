package com.gal.afiliaciones.infrastructure.controller.validatecontributorelationship;


import com.gal.afiliaciones.application.service.validatecontributorelationship.impl.ValidateContributorRelationShipServiceImpl;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidRelationShipResponse;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validateContributorRelationship")
@RequiredArgsConstructor
@Tag(name = "validateContributorRelationShip", description = "validar la realacion laboral entre empleador y empleado")
@CrossOrigin(origins = "*")
public class ValidateContributorRelationShipController {

    private final ValidateContributorRelationShipServiceImpl service;

    @PostMapping("validate")
    public ResponseEntity<BodyResponseConfig<ValidRelationShipResponse>> validateContributorRelationShip(@RequestBody ValidateContributorRequest request) {
        return ResponseEntity.ok(new BodyResponseConfig<>(service.validateRelationShip(request), "Validate contributor relationship"));
    }
}
