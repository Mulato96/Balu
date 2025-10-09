package com.gal.afiliaciones.infrastructure.controller.retirement;

import com.gal.afiliaciones.domain.model.retirement.ContractListResponseDto;
import com.gal.afiliaciones.domain.model.retirement.RetirementRequestDto;
import com.gal.afiliaciones.domain.model.retirement.RetirementResponseDto;
import com.gal.afiliaciones.domain.model.retirement.RetirementService;
import com.gal.afiliaciones.domain.model.retirement.WorkerSearchRequestDto;
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
    public ResponseEntity<List<ContractListResponseDto>> searchWorker(@RequestBody WorkerSearchRequestDto request) {
        List<ContractListResponseDto> contracts = retirementService.searchWorker(request);
        return ResponseEntity.ok(contracts);
    }

    @PostMapping("/request-retirement")
    public ResponseEntity<RetirementResponseDto> requestRetirement(@RequestBody RetirementRequestDto request) {
        RetirementResponseDto response = retirementService.requestRetirement(request);
        return ResponseEntity.ok(response);
    }
}