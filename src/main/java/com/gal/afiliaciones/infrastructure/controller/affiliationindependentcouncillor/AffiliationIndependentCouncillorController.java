package com.gal.afiliaciones.infrastructure.controller.affiliationindependentcouncillor;

import com.gal.afiliaciones.application.service.affiliationindependentcouncillor.AffiliationIndependentCouncillorService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep3Response;
import com.gal.afiliaciones.infrastructure.dto.mayoraltydependence.MayoraltyDependenceDTO;
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
@RequestMapping("/affiliationindependentcouncillorcontroller")
@Tag(name = "Affiliation-Independent-Councillor-Controller", description = "AFILIACIONES TRABAJADORES INDEPENDIENTES " +
        "CONCEJAL/EDIL")
@AllArgsConstructor
public class AffiliationIndependentCouncillorController {

    private final AffiliationIndependentCouncillorService service;

    @PostMapping("/createaffiliation/step1")
    public ResponseEntity<AffiliationCouncillorStep1DTO> createAffiliationStep1(
            @RequestBody AffiliationCouncillorStep1DTO dto) {
        try{
            AffiliationCouncillorStep1DTO response = service.createAffiliationStep1(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("/createaffiliation/step2")
    public ResponseEntity<AffiliationCouncillorStep2DTO> createAffiliationStep2(
            @RequestBody AffiliationCouncillorStep2DTO dto) {
        try{
            AffiliationCouncillorStep2DTO response = service.createAffiliationStep2(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping(value = "/createaffiliation/step3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AffiliationCouncillorStep3Response> createAffiliationStep3(
            AffiliationCouncillorStep3DTO dto, @RequestParam(name = "files") List<MultipartFile> files) {
        try{
            AffiliationCouncillorStep3Response response = service.createAffiliationStep3(dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @GetMapping("/findAllMayoraltyDependencies/{nit}")
    public ResponseEntity<List<MayoraltyDependenceDTO>> findAllMayoraltyDependencies(@PathVariable String nit) {
        return ResponseEntity.ok(service.findAllMayoraltyDependence(nit));
    }

    @PostMapping(value = "/createaffiliation/step3pila")
    public ResponseEntity<AffiliationCouncillorStep3DTO> createAffiliationStep3FromPila(AffiliationCouncillorStep3DTO dto) {
        try{
            AffiliationCouncillorStep3DTO response = service.createAffiliationStep3FromPila(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

}
