package com.gal.afiliaciones.infrastructure.controller.economicactivity;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.response.EconomicActivityResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activity/economic")
@RequiredArgsConstructor
@Tag(name = "EconomicActivity", description = "get economic activity by code CIIU and name ")
public class EconomicActivityController {
    private final IEconomicActivityService iEconomicActivityService;

    @GetMapping("/findAll")
    @Operation(summary = "find economic activity by code CIIU or description")
    public ResponseEntity<List<EconomicActivityDTO>> findEconomicActivityByCodeCIIU(
            @RequestParam(required = false) String codeCIIUDecree,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(iEconomicActivityService.getEconomicActivityByCodeCIIU(codeCIIUDecree, description));
    }

    @GetMapping("/findByCode/{code}")
    @Operation(summary = "find economic activity by code")
    public ResponseEntity<EconomicActivityDTO> findEconomicActivityByCode(@PathVariable String code) {
        return ResponseEntity.ok(iEconomicActivityService.getEconomicActivityByCode(code));
    }

    @GetMapping("find-user-economic-activity")
    public ResponseEntity<List<EconomicActivityResponseDTO>> findUserEconomicActivity(@RequestParam String documentType, @RequestParam String documentNumber) {
        return ResponseEntity.ok(iEconomicActivityService.findUserEconomicActivity(documentType, documentNumber));
    }

    @GetMapping("find-economic-activity-exclude-current")
    public ResponseEntity<List<EconomicActivityDTO>> findExcludeEconomicActivity(@RequestParam String documentType, @RequestParam String documentNumber) {
        return ResponseEntity.ok(iEconomicActivityService.getEconomyActivityExcludeCurrent(documentType, documentNumber));
    }

    @GetMapping("/findByIdEconomicSector/{id}")
    @Operation(summary = "find economic activity by id economic sector")
    public ResponseEntity<List<EconomicActivityDTO>> findEconomicActivityByCode(@PathVariable Long id) {
        return ResponseEntity.ok(iEconomicActivityService.getEconomicActivitiesByEconomicSectorId(id));
    }

}
