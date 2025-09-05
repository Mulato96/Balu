package com.gal.afiliaciones.infrastructure.controller.affiliationindependentvolunteer;

import com.gal.afiliaciones.application.service.affiliationindependentvolunteer.AffiliationIndependentVolunteerService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliate.MandatoryDanger;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep3DTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/AffiliationIndependentVolunteerController")
@Tag(name = "Affiliation-Independent-Volunteer-Controller", description = "AFILIACIONES TRABAJADORES INDEPENDIENTES SIN CONTRATO (VOLUNTARIO)")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AffiliationIndependentVolunteerController {

    private final AffiliationIndependentVolunteerService service;

    @PostMapping("/createaffiliation/step1")
    public ResponseEntity<Affiliation> createAffiliationStep1(
            @RequestBody AffiliationIndependentVolunteerStep1DTO dto) {
        try {
            Affiliation response = service.createAffiliationStep1(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AffiliationAlreadyExistsError ex) {
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("/createaffiliation/step2")
    public ResponseEntity<Danger> createAffiliationStep2(@RequestBody AffiliationIndependentVolunteerStep2DTO dto) {
        try {
            Danger response = service.createAffiliationStep2(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AffiliationAlreadyExistsError ex) {
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping(value = "/createaffiliation/step3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Affiliation> createAffiliationStep3(AffiliationIndependentVolunteerStep3DTO dto,
            @RequestParam(name = "files") List<MultipartFile> files) {
        try {
            Affiliation response = service.createAffiliationStep3(dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AffiliationAlreadyExistsError ex) {
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("isUserTransferable")
    public ResponseEntity<Boolean> isUserTransferable(
            @RequestBody ConsultIndependentWorkerDTO consultIndependentWorkerDTO) {
        return ResponseEntity.ok(service.isTransferableBySAT(consultIndependentWorkerDTO.getWorkerDocumentType(),
                consultIndependentWorkerDTO.getWorkerDocumentNumber()));
    }

    @PostMapping(value = "/createaffiliation/step3pila")
    public ResponseEntity<Affiliation> createAffiliationStep3FromPila(AffiliationIndependentVolunteerStep3DTO dto) {
        try {
            Affiliation response = service.createAffiliationStep3FromPila(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AffiliationAlreadyExistsError ex) {
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @GetMapping("/mandatory-danger/{fkOccupationId}")
    public ResponseEntity<MandatoryDanger> getMandatoryDangerByFkOccupationId(@PathVariable Long fkOccupationId) {
        MandatoryDanger danger = service.getMandatoryDangerByFkOccupationId(fkOccupationId);
        return ResponseEntity.ok(danger);
    }

}
