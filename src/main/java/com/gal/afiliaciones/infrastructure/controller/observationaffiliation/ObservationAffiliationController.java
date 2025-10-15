package com.gal.afiliaciones.infrastructure.controller.observationaffiliation;

import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.infrastructure.dto.observationsaffiliation.ObservationAffiliationDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/observations_affiliations")
public class ObservationAffiliationController {

    private final ObservationsAffiliationService observationsAffiliationService;

    @GetMapping("/findObservation/{filedNumber}")
    public ResponseEntity<List<ObservationAffiliationDTO>> findObservations(@PathVariable String filedNumber){
        return ResponseEntity.ok(observationsAffiliationService.findByFiledNumber(filedNumber));
    }
}
