package com.gal.afiliaciones.infrastructure.controller.policy;

import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.infrastructure.dto.policy.PolicyRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/poliza")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/crear")
    public ResponseEntity<Policy> createPolicy(@RequestBody PolicyRequestDTO request) {
        Policy policy = policyService.createPolicy(request.getIdType(), request.getIdNumber(),
                request.getEffectiveDateFrom(), request.getIdPolicyType(), request.getIdAffiliate(), 0L, request.getNameCompany());
        return new ResponseEntity<>(policy, HttpStatus.CREATED);
    }
}
