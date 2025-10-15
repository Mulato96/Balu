package com.gal.afiliaciones.infrastructure.controller.card;


import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.infrastructure.dto.card.ResponseGrillaCardsDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consultcard")
@Tag(name = "Consult-card-affiliation", description = "Consular carnet API")
@AllArgsConstructor
public class GenerateCardAffiliatedController {

    private final GenerateCardAffiliatedService generateCardAffiliatedService;

    @GetMapping("/cosultuserCard/{numberDocument}/{typeDocument}")
    public ResponseEntity<ValidCodeCertificateDTO> consultCard(@PathVariable String numberDocument, @PathVariable String typeDocument){
        return ResponseEntity.ok().body(generateCardAffiliatedService.consultUserCard(numberDocument, typeDocument));
    }

    @PostMapping("/generatedCard")
    public ResponseEntity<List<ResponseGrillaCardsDTO>> generatedCard(@Validated @RequestBody ValidCodeCertificateDTO validateCodeDTO) {
        return ResponseEntity.ok().body(generateCardAffiliatedService.createCardUser(validateCodeDTO));
    }

    @GetMapping("/consultCard/{id}")
    public ResponseEntity<Map<String, String>> consultCard(@Validated @PathVariable String id) {
        return ResponseEntity.ok().body(generateCardAffiliatedService.consultCard(id));
    }

    @PostMapping("/generatedCardWithoutOtp/{filedNumber}")
    public ResponseEntity<List<ResponseGrillaCardsDTO>> generatedCardWithoutOtp(@PathVariable String filedNumber) {
        return ResponseEntity.ok().body(generateCardAffiliatedService.createCardWithoutOtp(filedNumber));
    }

    @GetMapping("/consultCardByFiledNumber/{filedNumber}")
    public ResponseEntity<String> consultCardByAffiliate(@PathVariable String filedNumber) {
        String cardStr = generateCardAffiliatedService.consultCardByAffiliate(filedNumber);
        return ResponseEntity.ok().body(cardStr);
    }

}
