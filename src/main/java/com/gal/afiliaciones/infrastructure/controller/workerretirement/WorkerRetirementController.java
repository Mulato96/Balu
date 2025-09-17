package com.gal.afiliaciones.infrastructure.controller.workerretirement;

import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.CreateRequestRetirementWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workerretirement")
@RequiredArgsConstructor
@Tag(name = "Worker Retirement", description = "Retiro trabajadores")
@CrossOrigin(origins = "*")
public class WorkerRetirementController {

    private final RetirementService service;

    @GetMapping("/findWorkerByEmployer")
    @Operation(summary = "find worker data by identification")
    public ResponseEntity<BodyResponseConfig<DataWorkerRetirementDTO>> findWorker(@RequestParam String documentType,
                                                                                 @RequestParam String documentNumber,
                                                                                 @RequestParam Long idAffiliateEmployer) {
        return ResponseEntity.ok(service.consultWorker(documentType, documentNumber, idAffiliateEmployer));
    }

    @PostMapping()
    public ResponseEntity<String> retirementWorker(@RequestBody DataWorkerRetirementDTO dataWorkerRetirementDTO){
        return ResponseEntity.ok(service.retirementWorker(dataWorkerRetirementDTO));
    }

    @PostMapping("/createRequestRetirementWork")
    public ResponseEntity<String> createRequestRetirementWork(@RequestBody CreateRequestRetirementWorkerDTO dto){
        return ResponseEntity.ok().body(service.createRequestRetirementWork(dto.getIdAffiliation(), dto.getDateRetirement(), dto.getName()));
    }

    @PostMapping("/cancelRetirementWorker/{idAffiliation}")
    public ResponseEntity<Boolean> cancelRetirementWorker(@PathVariable Long idAffiliation){
        return ResponseEntity.ok(service.cancelRetirementWorker(idAffiliation));
    }

}
