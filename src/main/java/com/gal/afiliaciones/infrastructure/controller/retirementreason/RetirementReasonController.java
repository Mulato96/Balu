package com.gal.afiliaciones.infrastructure.controller.retirementreason;

import com.gal.afiliaciones.application.service.retirementreason.RetirementReasonService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.CompanyInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RetirementEmployerDTO;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/retirement-reason")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RetirementReasonController {

    public static final String COMPANY_INFO = "company info";
    private final RetirementReasonService retirementReasonService;

    @GetMapping("/findall")
    public ResponseEntity<List<RetirementReason>> findAll(){
        return ResponseEntity.ok().body(retirementReasonService.findAll());
    }


   @GetMapping("get-info-empresa")
    public ResponseEntity<BodyResponseConfig<CompanyInfoDTO>> getCompanyInfo(@Param("id") String identification,
                                                                             @Param("id") String identificationType) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(retirementReasonService.getCompanyInfo(identificationType, identification), COMPANY_INFO), HttpStatus.OK);
    }


    @PostMapping("retirement-employer")
            public ResponseEntity<BodyResponseConfig<String>> getCompanyInfo(@RequestBody RetirementEmployerDTO retirementEmployerDTO) throws MessagingException, IOException {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(retirementReasonService.retirementEmployer(retirementEmployerDTO)), HttpStatus.OK);
    }

    @GetMapping("/findAllRetirementReasonWorker")
    public ResponseEntity<List<RetirementReasonWorker>> findAllRetirementReasonWorker(){
        return ResponseEntity.ok().body(retirementReasonService.findAllRetirementReasonWorker());
    }

}
