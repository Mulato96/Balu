package com.gal.afiliaciones.infrastructure.controller.workermanagement;

import com.gal.afiliaciones.application.service.workermanagement.WorkerManagementService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.workermanagement.NotFoundWorkersException;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementPaginatedResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchFilterDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @PostMapping("/getworkersbyfilters/paginated")
    @Operation(summary = "Obtener los trabajadores dependientes de un empleador por filtros con paginación")
    public ResponseEntity<WorkerManagementPaginatedResponseDTO> getWorkersByEmployerPaginated(@RequestBody FiltersWorkerManagementDTO filters) {
        try {
            WorkerManagementPaginatedResponseDTO workers = service.findWorkersByEmployerPaginated(filters);
            return ResponseEntity.ok(workers);
        } catch (NotFoundWorkersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new WorkerManagementPaginatedResponseDTO());
        } catch (AffiliateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WorkerManagementPaginatedResponseDTO());
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
                                                      @RequestParam(name = "idUser") Long idUser,
                                                      @RequestParam(name = "idAffiliateEmployer") Long idAffiliateEmployer){
        return ResponseEntity.ok().body(service.massiveUpdateWorkers(file, idUser, idAffiliateEmployer));
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

    @PostMapping("/getworker")
    @Operation(summary = "Obtener trabajador por tipo y número de documento (dependientes e independientes)")
    public ResponseEntity<List<WorkerSearchResponseDTO>> getWorker(@RequestBody WorkerSearchFilterDTO filter) {
        try {
            List<WorkerSearchResponseDTO> workers = service.getWorkersByDocument(filter);
            return ResponseEntity.ok(workers);
        } catch (NotFoundWorkersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/updatecoveragedate")
    @Operation(summary = "Actualizar fecha de cobertura de un trabajador (dependiente, independiente)")
    public ResponseEntity<UpdateWorkerCoverageDateResponseDTO> updateCoverageDate(
            @Valid @RequestBody UpdateWorkerCoverageDateDTO dto) {
        try {
            UpdateWorkerCoverageDateResponseDTO response = service.updateWorkerCoverageDate(dto);
            return ResponseEntity.ok(response);
        } catch (AffiliateNotFound | NotFoundWorkersException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(UpdateWorkerCoverageDateResponseDTO.builder()
                            .success(false)
                            .message(ex.getError().getMessage())
                            .build());
        } catch (AffiliationError ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UpdateWorkerCoverageDateResponseDTO.builder()
                            .success(false)
                            .message(ex.getError().getMessage())
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UpdateWorkerCoverageDateResponseDTO.builder()
                            .success(false)
                            .message("Error al actualizar la fecha de cobertura: " + ex.getMessage())
                            .build());
        }
    }

    @GetMapping("/worker/{idAffiliate}")
    @Operation(summary = "Obtener detalle completo de un trabajador (dependiente o independiente) por ID")
    public ResponseEntity<WorkerDetailDTO> getWorkerDetail(
            @PathVariable Long idAffiliate) {
        try {
            WorkerDetailDTO detail = service.getWorkerDetail(idAffiliate);
            return ResponseEntity.ok(detail);
        } catch (AffiliateNotFound ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AffiliationError ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/updatecontract")
    @Operation(summary = "Actualizar contrato de un trabajador independiente")
    public ResponseEntity<UpdateContractResponseDTO> updateContract(
            @Valid @RequestBody UpdateContractDTO dto) {
        try {
            UpdateContractResponseDTO response = service.updateContract(dto);
            return ResponseEntity.ok(response);
        } catch (AffiliateNotFound ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(UpdateContractResponseDTO.builder()
                            .success(false)
                            .message(ex.getMessage())
                            .build());
        } catch (AffiliationError ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UpdateContractResponseDTO.builder()
                            .success(false)
                            .message(ex.getError().getMessage())
                            .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UpdateContractResponseDTO.builder()
                            .success(false)
                            .message("Error al actualizar el contrato: " + ex.getMessage())
                            .build());
        }
    }
}
