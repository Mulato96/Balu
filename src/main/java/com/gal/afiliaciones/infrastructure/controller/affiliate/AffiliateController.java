package com.gal.afiliaciones.infrastructure.controller.affiliate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.EmployerAffiliationHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationHistoryView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.RequestSignatureDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/affiliates")
@Tag(name = "Affiliate", description = "Affiliate Management API")
@RequiredArgsConstructor
public class AffiliateController {
    @Value("${enviroment:dev}")
    private String environment;
    private final AffiliateService affiliateService;
    private final AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;

    @GetMapping("/by-type-and-number")
    @Operation(summary = "Obtener afiliaciones por tipo y número de documento")
    public ResponseEntity<List<UserAffiliateDTO>> getAffiliationsByTypeAndNumber(
            @Parameter(description  = "Tipo de documento", example = "CC", required = true)
            @RequestParam("documentType") String documentType,
            @Parameter(description = "Número de documento", example = "123456789", required = true)
            @RequestParam("documentNumber") String documentNumber) {
        try {
            List<UserAffiliateDTO> affiliations = affiliateService.findAffiliationsByTypeAndNumber(documentType, documentNumber);
            return ResponseEntity.ok(affiliations);
        } catch (AffiliateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }
    }

    @GetMapping("/dataStatusAffiliation/{numberDocument}/{typeDocument}")
    public List<DataStatusAffiliationDTO> getDataStatusAffiliations(@PathVariable String numberDocument,
                                                                    @PathVariable String typeDocument){
        return affiliateService.getDataStatusAffiliations(numberDocument, typeDocument);
    }

    @PostMapping("signature")
    public void signature(@RequestBody RequestSignatureDTO request){
        affiliateService.sing(request.getFiledNumber());
    }

    @PutMapping(value = "/regularization/{filedNumber}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegularizationDTO> regularization(@PathVariable("filedNumber") String filedNumber,
                                                            @RequestParam(name = "files") List<MultipartFile> files) {
        try{
            RegularizationDTO response = affiliateService.regularizationDocuments(filedNumber, files);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

    @PostMapping("getAffiliationByIdentification")
    public ResponseEntity<Object> getAffiliationByIdentification(@RequestBody ConsultIndependentWorkerDTO consultIndependentWorkerDTO) {
        return ResponseEntity.ok(affiliateService.responseFoundAffiliate(consultIndependentWorkerDTO.getWorkerDocumentType(), consultIndependentWorkerDTO.getWorkerDocumentNumber()));
    }

    @PutMapping("/regularizationMercantile/{filedNumber}/{idTypeEmployer}/{idSubTypeEmployer}")
    public ResponseEntity<List<DataDocumentAffiliate>> regularizationMercantile(@PathVariable String filedNumber, @PathVariable Long idTypeEmployer , @PathVariable Long idSubTypeEmployer , @RequestBody List<DocumentRequestDTO> files){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.regularizationDocuments(filedNumber, idTypeEmployer, idSubTypeEmployer, files));
    }

    @GetMapping("/isForeignPension/{filedNumber}")
    public ResponseEntity<Boolean> getForeignPension(@PathVariable String filedNumber){
        return  ResponseEntity.ok().body(affiliateService.getForeignPension(filedNumber));
    }

    @Operation(summary = "Servicio que permite la generación y asignación de la contraseña temporal")
    @PutMapping("assignTemporalPass")
    public ResponseEntity<BodyResponseConfig<String>> assignTemporalPass(@RequestParam String email) {
        return ResponseEntity.ok(new BodyResponseConfig<>(affiliateService.assignTemporalPass(email), "Temporal password generated"));
    }

    @GetMapping("findAllRequestChannel")
    public ResponseEntity<List<RequestChannel>> findAllRequestChannel(){
        return  ResponseEntity.ok().body(affiliateService.findAllRequestChannel());
    }

    @GetMapping("findAffiliation")
    public ResponseEntity<BodyResponseConfig<AffiliationResponseDTO>> findAffiliation(
            @Parameter(description = "Tipo de documento del aportante")
            @RequestParam String documentType,
            @Parameter(description = "Número de documento del aportante")
            @RequestParam String documentNumber,
            @Parameter(description = "Dígito de verificación")
            @RequestParam(required = false) Integer verificationDigit) {
        return ResponseEntity.ok(new BodyResponseConfig<>(affiliateService.findUserAffiliate(documentType, documentNumber, verificationDigit), ""));
    }
    /**
     * GET /api/v1/affiliates/latest?documentType=CC&documentNumber=123456
     *
     * @param documentType   tipo de documento (CC, NIT, PAS, etc.)
     * @param documentNumber número del documento
     * @return el Affiliate más reciente
     */
    @GetMapping("/latest")
    public ResponseEntity<Long> getLatestAffiliate(
            @RequestParam String documentType,
            @RequestParam String documentNumber) {

        // evita NPE devolviendo 404 si el env no coincide
        if (!List.of("dev", "uat", "qa").contains(environment.toLowerCase())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(
                affiliateService.findAffiliate(documentType, documentNumber));
    }

    @GetMapping("calculateIbcAmount")
    public ResponseEntity<BigDecimal> calculateIbcAmount(BigDecimal monthlyContractValue, BigDecimal ibcPercentage){
        return  ResponseEntity.ok().body(affiliateService.calculateIbcAmount(monthlyContractValue, ibcPercentage));
    }

    @PostMapping("/affiliate-bus")
    public ResponseEntity<String> affiliateBUs(
            @RequestParam String idTipoDoc,
            @RequestParam String idAfiliado) throws MessagingException, IOException, IllegalAccessException {

        Boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        if (Boolean.TRUE.equals(result)) {
            return ResponseEntity.ok("Afiliaciones iniciadas correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al iniciar el proceso de afiliación.");
        }
    }

    @GetMapping("/employer-affiliation-history/{nitCompany}/{documentType}/{documentNumber}")
    public List<EmployerAffiliationHistoryDTO> employerAffiliationHistory(@PathVariable String nitCompany, 
                                               @PathVariable String documentType, 
                                               @PathVariable String documentNumber) {
        return affiliateService.getEmployerAffiliationHistory(nitCompany, documentType, documentNumber);
    }

    @GetMapping("/internal-staff-employer-affiliation-history/{nitCompany}")
    public List<EmployerAffiliationHistoryDTO> employerAffiliationHistory(@PathVariable String nitCompany,
                                               @QueryParam(value = "decentralizedNumber") Integer decentralizedNumber) {
        return affiliateService.getEmployerAffiliationHistory(nitCompany, decentralizedNumber);
    }

    @GetMapping("/individual-worker-affiliation/{nitCompany}/{documentType}/{documentNumber}")
    public IndividualWorkerAffiliationView individualWorkerAffiliation(@PathVariable String nitCompany, 
                                               @PathVariable String documentType, 
                                               @PathVariable String documentNumber) {
        return affiliateService.getIndividualWorkerAffiliation(nitCompany, documentType, documentNumber);
    }

    @GetMapping("/individual-worker-affiliation-history/{documentType}/{documentNumber}")
    public List<IndividualWorkerAffiliationHistoryView> individualWorkerAffiliationHistory(
                                                    @PathVariable String documentType, 
                                                    @PathVariable String documentNumber) {
        return affiliateService.getIndividualWorkerAffiliationHistory(documentType, documentNumber);
    }

    @GetMapping("/affiliate-company/{documentType}/{documentNumber}")
    public Affiliate affiliateCompany(@PathVariable String documentType, 
                                                    @PathVariable String documentNumber) {
        return affiliateService.getAffiliateCompany(documentType, documentNumber);
    }
    
}
