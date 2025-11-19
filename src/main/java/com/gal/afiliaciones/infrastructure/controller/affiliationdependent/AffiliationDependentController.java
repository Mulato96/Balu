package com.gal.afiliaciones.infrastructure.controller.affiliationdependent;

import com.gal.afiliaciones.application.service.affiliationdependent.AffiliationDependentService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.HeadquarterDataDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.RequestSearchEconomicActivitiesDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.UpdateEconomicActivityRequest;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.UpdateEconomicActivityResponse;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityHeadquarterDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/affiliationdependent")
@RequiredArgsConstructor
@Tag(name = "AFILIACION_DEPENDIENTES", description = "Servicios para afiliacion de trabajadores dependientes")
@CrossOrigin("*")
public class AffiliationDependentController {

    private final AffiliationDependentService service;

    @GetMapping("bondingTypes")
    public ResponseEntity<List<BondingTypeDependent>> getAll() {
        List<BondingTypeDependent> bondingTypes = service.findAll();
        return ResponseEntity.ok(bondingTypes);
    }

    @PostMapping("/search")
    public ResponseEntity<BodyResponseConfig<DependentWorkerDTO>> consultUser(@RequestBody ValidateContributorRequest request) {
        try {
            DependentWorkerDTO dependentWorkerDTO = service.consultUser(request);
            return ResponseEntity.ok(new BodyResponseConfig<>(dependentWorkerDTO, "Get data user"));
        } catch (ResponseStatusException ex){
            BodyResponseConfig<DependentWorkerDTO> responseUserNotFound = new BodyResponseConfig<>();
            responseUserNotFound.setData(service.preloadUserNotExists(request));
            responseUserNotFound.setMessage(ex.getReason());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseUserNotFound);
        }
    }

    @GetMapping("/searchHeadquarter/{idAffiliate}")
    public ResponseEntity<HeadquarterDataDTO> consultHeadquarters(@PathVariable Long idAffiliate) {
        HeadquarterDataDTO employerHeadquarters = service.consultHeadquarters(idAffiliate);
        return ResponseEntity.ok(employerHeadquarters);
    }

    @PostMapping("/createaffiliation")
    public ResponseEntity<AffiliationDependent> createAffiliation(@RequestBody AffiliationDependentDTO dto) {
        AffiliationDependent response = service.createAffiliation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("workmodalities")
    public ResponseEntity<List<WorkModality>> getAllWorkModalities() {
        List<WorkModality> workModalities = service.findAlllWorkModalities();
        return ResponseEntity.ok(workModalities);
    }

    @PostMapping("/createAffiliationIndependentStep1")
    public ResponseEntity<AffiliationDependent> createAffiliationIndependentStep1(@RequestBody AffiliationIndependentStep1DTO dto) {
        AffiliationDependent response = service.createAffiliationIndependentStep1(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/createAffiliationIndependentStep2")
    public ResponseEntity<AffiliationDependent> createAffiliationIndependentStep2(@RequestBody AffiliationIndependentStep2DTO dto) {
        AffiliationDependent response = service.createAffiliationIndependentStep2(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("searchEconomicActivitiesByEmployer/{idAffiliateEmployer}")
    public ResponseEntity<List<EconomicActivityDTO>> searchEconomicActivitiesByEmployer(@PathVariable Long idAffiliateEmployer) {
        List<EconomicActivityDTO> economicActivities = service.findEconomicActivitiesByEmployer(idAffiliateEmployer);
        return ResponseEntity.ok(economicActivities);
    }

    @GetMapping("/searchEconomicActivities/{idHeadquarter}")
    public ResponseEntity<List<EconomicActivityHeadquarterDTO>> consultEconomicActivities(@PathVariable Long idHeadquarter) {
        List<EconomicActivityHeadquarterDTO> economicActivities = service.findEconomicActivitiesByHeadquarter(idHeadquarter);
        return ResponseEntity.ok(economicActivities);
    }

    @PutMapping("/updateEconomicActivity")
    public ResponseEntity<UpdateEconomicActivityResponse> updateEconomicActivity(
            @RequestBody @Valid UpdateEconomicActivityRequest request) {
        service.updateEconomicActivityCode(request);
        return ResponseEntity.ok(new UpdateEconomicActivityResponse("Actividad económica actualizada exitosamente"));
    }

}
