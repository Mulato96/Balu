package com.gal.afiliaciones.infrastructure.controller.affiliationemployerdomesticserviceindependent;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.*;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DocumentBase64;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/AffiliationEmployerDomesticServiceIndependentController")
@CrossOrigin(origins = "*")
@Tag(name = "Affiliation-Employer-Domestic-Service-Independent-Controller", description = "GESTIÓN DE AFILIACIONES PROCESO EMPLEADOR SERVICIO DOMÉSTICO E INDEPENDIENTES")
@AllArgsConstructor
public class AffiliationEmployerDomesticServiceIndependentController {

    private final AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;

    private final AlfrescoService alfrescoService;

    @GetMapping("/pending")
    public ResponseEntity<VisualizationPendingPerformDTO> visualizationPendingPerform(){
        return ResponseEntity.status(HttpStatus.OK).body(affiliationEmployerDomesticServiceIndependentService.visualizationPendingPerform());
    }

    @PostMapping("/managementaffiliation")
    public ResponseEntity<ResponseManagementDTO> managementAffiliation(@RequestBody(required = false) AffiliationsFilterDTO filter, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(affiliationEmployerDomesticServiceIndependentService.managementAffiliation(page, size, filter));
    }

    @GetMapping("/requestdocuments/{field}")
    public ResponseEntity<ManagementDTO> management(@Valid @PathVariable String field){
        return ResponseEntity.status(HttpStatus.OK).body(affiliationEmployerDomesticServiceIndependentService.management(field));
    }

    @GetMapping("/consultdocument/{id}")
    public ResponseEntity<List<DocumentBase64>> consultDocument(String id){
        return ResponseEntity.ok().body(affiliationEmployerDomesticServiceIndependentService.consultDocument(id));
    }

    @PostMapping("/stateaffiliation")
    public void stateAffiliation(@RequestBody StateAffiliation stateAffiliation){

        affiliationEmployerDomesticServiceIndependentService.stateAffiliation(stateAffiliation);
    }

    @PutMapping("/statedocuments/{idAffiliate}")
    public void stateDocuments(@RequestBody List<DocumentsDTO> listDocumentsDTOS,  @PathVariable Long idAffiliate){

        affiliationEmployerDomesticServiceIndependentService.stateDocuments(listDocumentsDTOS, idAffiliate);
    }

    @PostMapping("/createaffiliation/step1")
    public ResponseEntity<Affiliation> createAffiliationStep1(@RequestBody DomesticServiceAffiliationStep1DTO dto) {
        try{
            Affiliation response = affiliationEmployerDomesticServiceIndependentService.createAffiliationStep1(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("/createaffiliation/step2")
    public ResponseEntity<Affiliation> createAffiliationStep2(@RequestBody DomesticServiceAffiliationStep2DTO dto) {
        try{
            Affiliation response = affiliationEmployerDomesticServiceIndependentService.createAffiliationStep2(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PutMapping(value = "/createaffiliation/step3/{idAffiliation}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Affiliation> createAffiliationStep3(@PathVariable("idAffiliation") Long idAffiliation,
                                                         @RequestParam(name = "files") List<MultipartFile> files) {
        try{
            MultipartFile document = null;
            if(!files.isEmpty())
                document = files.get(0);

            Affiliation response = affiliationEmployerDomesticServiceIndependentService.createAffiliationStep3
                    (idAffiliation, document);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @GetMapping("/createfile/{idParent}/{nameFile}")
    public ResponseEntity<AlfrescoUploadResponse> createFile(@PathVariable String idParent, @PathVariable String nameFile){

        return ResponseEntity.status(HttpStatus.CREATED).body(alfrescoService.createFolder(idParent, nameFile));
    }

    @PostMapping("/managementaffiliation/download")
    public ResponseEntity<String> downloadExcel(@RequestBody(required = false) AffiliationsFilterDTO filter) {
        String excel = affiliationEmployerDomesticServiceIndependentService.generateExcel(filter);

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(excel);
    }

    @PostMapping("managementaffiliation/{filedNumber}/asignar/{usuarioId}")
    public ResponseEntity<String> assignTo(
            @PathVariable String filedNumber,
            @PathVariable Long usuarioId
    ) {
        affiliationEmployerDomesticServiceIndependentService.assignTo(filedNumber, usuarioId);
        return ResponseEntity.ok("Afiliacion asignada correctamente");
    }
}
