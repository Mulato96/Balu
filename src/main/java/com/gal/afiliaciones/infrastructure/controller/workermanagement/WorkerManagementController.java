package com.gal.afiliaciones.infrastructure.controller.workermanagement;

import com.gal.afiliaciones.application.service.workermanagement.WorkerManagementService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.workermanagement.NotFoundWorkersException;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/workersmanagement")
@Tag(name = "Workers-Management-Controller", description = "GESTIÓN DE TRABAJADORES")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class WorkerManagementController {

    private final WorkerManagementService service;

    @PostMapping("/getworkersbyfilters")
    @Operation(summary = "Obtener los trabajadores dependientes de un empleador por filtros")
    public ResponseEntity<List<WorkerManagementDTO>> getWorkersByEmployer(@RequestBody FiltersWorkerManagementDTO filters) {
        try {
            List<WorkerManagementDTO> workers = service.findWorkersByEmployer(filters);
            return ResponseEntity.ok(workers);
        } catch (NotFoundWorkersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        } catch (AffiliateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/getAffiliation/{filedNumber}")
    @Operation(summary = "Obtener los datos de la afiliacion de un dependiente por numero de radicado")
    public ResponseEntity<BodyResponseConfig<AffiliationDependent>> getAffiliationDependent(
            @PathVariable("filedNumber") String filedNumber) {
        return ResponseEntity.ok(service.findDataDependentById(filedNumber));
    }

    @PostMapping(value = "/loading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseServiceDTO> loading(@RequestParam(name = "files") MultipartFile file,
                                                      @RequestParam(name = "documentType") String documentType,
                                                      @RequestParam(name = "documentNumber") String documentNumber){
        return ResponseEntity.ok().body(service.massiveUpdateWorkers(file, documentType, documentNumber));
    }

    @GetMapping("downloadTemplateMassiveUpdate")
    public ResponseEntity<String> downloadTemplateMassiveUpdate() {
        return ResponseEntity.ok(service.downloadTemplateMassiveUpdate());
    }

    @GetMapping("downloadGuideMassiveUpdate")
    public ResponseEntity<String> downloadGuideMassiveUpdate() {
        return ResponseEntity.ok(service.downloadTemplateGuide());
    }

    @GetMapping("historicalMassiveUpdateWorkers/{idUser}")
    public ResponseEntity<List<RecordMassiveUpdateWorker>> historicalMassiveUpdateWorkers(@PathVariable Long idUser){
        return ResponseEntity.ok().body(service.findAllByIdUser(idUser));
    }

    @GetMapping("documentDetail/{idRecordMassive}")
    public ResponseEntity<ExportDocumentsDTO> documentDetailMassive(@PathVariable Long idRecordMassive){
        return ResponseEntity.ok().body(service.createDocument(idRecordMassive));
    }

    @PostMapping("/generateEmployerCerticate")
    @Operation(summary = "Obtener el certificado del empleador")
    public ResponseEntity<String> generateEmloyerCertificate(@RequestBody EmployerCertificateRequestDTO dto) {
        return ResponseEntity.ok(service.generateEmloyerCertificate(dto));
    }

}
