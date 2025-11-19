package com.gal.afiliaciones.infrastructure.controller.certificate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.gal.afiliaciones.domain.model.UserMain;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.CertificateBulkService;

import com.gal.afiliaciones.application.service.CertificateNoAffiliateService;
import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.CertificateWorkerARLIntegrationService;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.certificate.CertificateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCodeValidationExpired;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateWorkerByEmployerResponse;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ResponseBulkDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateBulkService certificateBulkService;
    private final CertificateNoAffiliateService certificateNoAffiliateService;
    private final CertificateWorkerARLIntegrationService certificateWorkerArlIntegrationService;

    @PostMapping("/generate-create")
    public ResponseEntity<String> createAndGenerateCertificate(
           @RequestBody FindAffiliateReqDTO findAffiliateReqDTO) {
        try {
            String report = certificateService.createAndGenerateCertificate(findAffiliateReqDTO);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            String message = Objects.isNull(e.getMessage()) ? ((AffiliationsExceptionBase) e).getError().getMessage() : e.getMessage();
            return ResponseEntity.status(500).body("Error generating certificate: " + message);
        }
    }


    @PostMapping("/generate-certificate")
    public ResponseEntity<String> generateReportCertificate(@RequestParam String documentNumber, @RequestParam String validatorCode) {
        try {
            String response = certificateService.generateReportCertificate(documentNumber, validatorCode);
            return ResponseEntity.ok(response);
        } catch (AffiliateNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Affiliate or Certificate not found for validatorCode : " + validatorCode + " and document number: " + documentNumber);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error occurred while generating the report: " + ex.getMessage());
        }
    }

    @GetMapping("/certificateWorkerByEmployer")
    public ResponseEntity<CertificateWorkerByEmployerResponse> certificateWorkerByEmployer(
            @RequestParam String tipoDoc,
            @RequestParam String idAfiliado) {
        return ResponseEntity.ok().body(certificateWorkerArlIntegrationService.getCertificatesWorkerArlIntegration(tipoDoc, idAfiliado));
    }

    @Operation(summary = "Generate a new certificate for an affiliate")
    @GetMapping("/validate")
    public ResponseEntity<String> validateCertificate(@RequestParam String validationCode) {
        try {
            String certificate = certificateService.getValidateCodeCerticate(validationCode);
            return ResponseEntity.ok(certificate);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        } catch (AffiliateNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Constant.CERTIFICATE_CODE_VALIDATION_MESSAGE);
        }catch (ErrorCodeValidationExpired ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error occurred while validating the certificate: " + Constant.ERROR_CODE_VALIDATION_EXPIRED);
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error occurred while validating the certificate: " + ex.getMessage());
        }
    }

    @Operation(summary = "Find certificates by type document and number document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the certificates",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Certificate.class)) }),
            @ApiResponse(responseCode = "404", description = "No certificates found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @GetMapping("/findByTypeAndNumber")
    public ResponseEntity<List<Certificate>> findByTypeDocumentAndNumberDocument(
            @Parameter(description = "Type of document (e.g., CC, NIT)", required = true)
            @RequestParam String typeDocument,
            @Parameter(description = "Number of document", required = true)
            @RequestParam String identification) {
        try {
            List<Certificate> certificates = certificateService.findByTypeDocumentAndNumberDocument(typeDocument, identification);
            return ResponseEntity.ok(certificates);
        } catch (CertificateNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ArrayList<>());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    // Nuevo método para crear certificado de no afiliado
    @Operation(summary = "Create a non-affiliate certificate")
    @PostMapping("/create-non-affiliate-certificate")
    public ResponseEntity<String> createNonAffiliateCertificate(@RequestParam String type ,@RequestParam String identification) {
        try {
            String response = certificateNoAffiliateService.validateNonAffiliateCertificate(identification, type);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error occurred while creating the certificate: " + ex.getMessage());
        }
    }

    @GetMapping("qr/{id}")
    public ResponseEntity<QrDTO> getQrCertificate(@PathVariable String id) {
        return new ResponseEntity<>(certificateService.getValidateCodeQR(id), HttpStatus.OK);
    }

    @PostMapping(value = "/loading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseBulkDTO> loading(
            @RequestParam(name = "files") MultipartFile file,
            @RequestParam(name = "numberDocument") String numberDocument,
            @RequestParam(name = "typeDocument") String typeDocument){
        return ResponseEntity.ok().body(certificateBulkService.generateMassiveWorkerCertificates(file, numberDocument, typeDocument));
    }

    @DeleteMapping("/id-document/{id}")
    public void deleteRecord(@PathVariable String id){
        certificateBulkService.deleteRecords(id);
    }

    @GetMapping("/create-certificates-masive/{idDocument}")
    public ResponseEntity<ByteArrayResource> createCertificatesMassive(@PathVariable String idDocument) throws IOException {
        MultipartFile file = certificateBulkService.createCertificatesMassive(idDocument);
        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getSize())
                .body(resource);
    }

    @PostMapping("/create-all-certificates")
    public ResponseEntity<String> createAllCertificate(@RequestParam String type, @RequestParam(required = false) LocalDate date){
        UserMain userMain = certificateBulkService.getUserPreRegister();
        certificateBulkService.createCertificatesMassive(type, date, userMain);
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/records-bulk-massive")
    public ResponseEntity<List<Map<String, Object>>> recordsBulkMassive(){
        return ResponseEntity.ok().body(certificateBulkService.recordsBulkMassive());
    }

    @GetMapping("/download-document-zip/{idDocument}")
    public ResponseEntity<ByteArrayResource> downloadDocumentZip(@PathVariable String idDocument) throws IOException {
        MultipartFile file = certificateBulkService.downloadDocumentZip(idDocument);
        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getSize())
                .body(resource);
    }

    @GetMapping("/template-bulk-certificate")
    public ResponseEntity<String> templateBulkCertificate(){
        return ResponseEntity.ok().body(certificateBulkService.getTemplate());
    }

}
