package com.gal.afiliaciones.infrastructure.controller.certificate;

import com.gal.afiliaciones.application.service.ConsultCertificateByUserService;
import com.gal.afiliaciones.config.ex.otp.OtpCodeInvalid;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/consultcartificated")
@CrossOrigin(origins = "*")
@Tag(name = "Consult-certificate-affiliation", description = "Consular cartificado API")
@AllArgsConstructor
public class ConsultCertificateController {

    private ConsultCertificateByUserService consultCertificateService;

    @GetMapping("/consultuser/{documentType}/{documentNumber}")
    public ResponseEntity<ValidCodeCertificateDTO> consultUser(@Validated @PathVariable String documentType, @PathVariable String documentNumber) throws MessagingException, IOException, IllegalAccessException {
            return ResponseEntity.ok().body(consultCertificateService.consultUser(documentType, documentNumber));
    }

    @PostMapping("/consultcertificated")
    public ResponseEntity<List<UserAffiliateDTO>> consultCodeValidate(@Validated @RequestBody ValidCodeCertificateDTO validateCodeDTO) throws MessagingException, IOException, IllegalAccessException, OtpCodeInvalid {
            return ResponseEntity.ok().body(consultCertificateService.validateCodeOTPCertificate(validateCodeDTO));
    }

}
