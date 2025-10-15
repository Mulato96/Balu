package com.gal.afiliaciones.infrastructure.controller.occupation;

import com.gal.afiliaciones.application.service.occupation.OccupationService;
import com.gal.afiliaciones.domain.model.Occupation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/occupations")
@RequiredArgsConstructor
@Tag(name = "Occupations-Employer-Provision-Service-Independent", description = "CONSULTA OCUPACIONES PARA INDEPENDIENTE PRESTACION DE SERVICIOS")
public class OccupationController {

    private final OccupationService occupationService;

    @GetMapping("/findAllOccupationProvisionService")
    public ResponseEntity<List<Occupation>> findOccupationsProvisionService() {
        return ResponseEntity.ok(occupationService.findOccupationsProvisionService());
    }

}
