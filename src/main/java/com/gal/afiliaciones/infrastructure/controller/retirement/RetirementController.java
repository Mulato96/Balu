package com.gal.afiliaciones.infrastructure.controller.retirement;

import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.domain.model.retirement.ContractListResponseDTO;
import com.gal.afiliaciones.domain.model.retirement.RetirementRequestDTO;
import com.gal.afiliaciones.domain.model.retirement.RetirementResponseDTO;
import com.gal.afiliaciones.domain.model.retirement.WorkerSearchRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/retirement")
public class RetirementController {

    @Autowired
    private RetirementService retirementService;

    @PostMapping("/search-worker")
    public ResponseEntity<List<ContractListResponseDTO>> searchWorker(@RequestBody WorkerSearchRequestDTO request) {
        List<ContractListResponseDTO> contracts = retirementService.searchWorker(request);
        return ResponseEntity.ok(contracts);
    }

    @PostMapping("/request-retirement")
    public ResponseEntity<RetirementResponseDTO> requestRetirement(@RequestBody RetirementRequestDTO request) {
        RetirementResponseDTO response = retirementService.requestRetirement(request);
        return ResponseEntity.ok(response);
    }
}