package com.gal.afiliaciones.infrastructure.controller.cancelaffiliate;

import com.gal.afiliaciones.application.service.cancelaffiliation.ICancelAffiliationService;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.ResponseStatusAffiliate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cancel/affiliate")
@RequiredArgsConstructor
@Tag(name = "CancelAffiliation", description = "Cancel affiliation")
@CrossOrigin(origins = "*")
public class CancelAffiliateController {

    private final ICancelAffiliationService iCancelAffiliationService;

    @GetMapping("/findByIdAffiliateEmployer")
    @Operation(summary = "find affiliate by document type and document number")
    public ResponseEntity<CancelAffiliateDTO> findEconomicActivityByCodeCIIU(@RequestParam String documentType, @RequestParam String documentNumber, @RequestParam Long idAffiliateEmployer) {
        return ResponseEntity.ok(iCancelAffiliationService.consultAffiliation(documentType, documentNumber, idAffiliateEmployer));
    }

    @PutMapping("/update-status/{filedNumber}")
    public ResponseEntity<ResponseStatusAffiliate> updateAffiliateStatus(@PathVariable String filedNumber, @RequestParam String newObservation) {
        iCancelAffiliationService.updateStatusCanceledAffiliate(filedNumber, newObservation);
        ResponseStatusAffiliate response = new ResponseStatusAffiliate();
        response.setStatus("Affiliate status updated successfully.");
        return ResponseEntity.ok(response);
    }
}