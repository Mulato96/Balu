package com.gal.afiliaciones.infrastructure.controller.employer;

import com.gal.afiliaciones.application.service.employer.UpdateEmployerService;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateEmployerDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateLegalRepresentativeDataDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updateemployer")
@RequiredArgsConstructor
@Tag(name = "ACTUALIZACION_EMPLEADOR", description = "Actualiza los datos del empleador")
@CrossOrigin("*")
public class UpdateEmployerController {

    private final UpdateEmployerService service;

    @GetMapping("/searchEmployer/{identificationType}/{identification}/{affiliationSubtype}")
    public ResponseEntity<UpdateEmployerDataBasicDTO> consultEmployer(
            @PathVariable String identificationType, @PathVariable String identification,
            @PathVariable String affiliationSubtype) {
        UpdateEmployerDataBasicDTO employer = service.searchEmployerDataBasic(identificationType, identification, affiliationSubtype);
        return ResponseEntity.ok(employer);
    }

    @PostMapping("updateDataBasicEmployer")
    public ResponseEntity<Boolean> updateDataBasicEmployer(@RequestBody RequestUpdateDataBasicDTO dto){
        return ResponseEntity.ok(service.updateEmployerDataBasic(dto));
    }

    @GetMapping("/searchLegalRepresentative/{identificationType}/{identification}/{affiliationSubtype}")
    public ResponseEntity<UpdateLegalRepresentativeDataDTO> consultLegalRepresentative(
            @PathVariable String identificationType, @PathVariable String identification,
            @PathVariable String affiliationSubtype) {
        UpdateLegalRepresentativeDataDTO employer = service.searchLegalRepresentativeData(identificationType, identification, affiliationSubtype);
        return ResponseEntity.ok(employer);
    }

    @PostMapping("updateLegalRepresentativeData")
    public ResponseEntity<Boolean> updateLegalRepresentativeData(@RequestBody RequestUpdateLegalRepresentativeDTO dto){
        return ResponseEntity.ok(service.updateLegalRepresentativeData(dto));
    }

    @GetMapping("/searchEmployerById/{idAffiliate}")
    public ResponseEntity<UpdateEmployerDataBasicDTO> consultEmployerById(@PathVariable Long idAffiliate) {
        UpdateEmployerDataBasicDTO employer = service.searchEmployerDataBasicById(idAffiliate);
        return ResponseEntity.ok(employer);
    }

}
