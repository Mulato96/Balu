package com.gal.afiliaciones.infrastructure.controller.affiliationemployerprovisionserviceindependent;

import com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent.AffiliationEmployerProvisionServiceIndependentService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep3DTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/AffiliationEmployerProvisionServiceIndependentController")
@CrossOrigin(origins = "*")
@Tag(name = "Affiliation-Employer-Provision-Service-Independent-Controller", description = "GESTIÓN DE AFILIACIONES PROCESO EMPLEADOR PRESTACION DE SERVICIO E INDEPENDIENTES")
@AllArgsConstructor
public class AffiliationEmployerProvisionServiceIndependentController {

    private final AffiliationEmployerProvisionServiceIndependentService affiliationEmployerProvisionServiceIndependentService;

    @PostMapping("/affiliation-provision-service/step1")
    public ResponseEntity<ProvisionServiceAffiliationStep1DTO> affiliationProvisionServiceStep1(@RequestBody ProvisionServiceAffiliationStep1DTO dto) {
        try{
            ProvisionServiceAffiliationStep1DTO response = affiliationEmployerProvisionServiceIndependentService.createAffiliationProvisionServiceStep1(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("/affiliation-provision-service/step2")
    public ResponseEntity<ProvisionServiceAffiliationStep2DTO> createAffiliationProvisionServiceStep2(@RequestBody ProvisionServiceAffiliationStep2DTO dto) {
        try{
            ProvisionServiceAffiliationStep2DTO response = affiliationEmployerProvisionServiceIndependentService.createAffiliationProvisionServiceStep2(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping(value = "/affiliation-provision-service/step3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProvisionServiceAffiliationStep3DTO> createAffiliationStep3(ProvisionServiceAffiliationStep3DTO dto,
                                                              @RequestParam(name = "files") List<MultipartFile> files) {
        try{
            ProvisionServiceAffiliationStep3DTO response = affiliationEmployerProvisionServiceIndependentService
                    .createAffiliationProvisionServiceStep3(dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping(value = "/affiliation-provision-service/step3pila")
    public ResponseEntity<ProvisionServiceAffiliationStep3DTO> createAffiliationStep3FromPila(
            ProvisionServiceAffiliationStep3DTO dto) {
        try{
            ProvisionServiceAffiliationStep3DTO response = affiliationEmployerProvisionServiceIndependentService
                    .createProvisionServiceStep3FromPila(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

}
