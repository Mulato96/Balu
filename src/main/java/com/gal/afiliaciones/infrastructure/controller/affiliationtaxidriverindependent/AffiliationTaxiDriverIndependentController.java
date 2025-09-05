package com.gal.afiliaciones.infrastructure.controller.affiliationtaxidriverindependent;

import com.gal.afiliaciones.application.service.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationIndependentTaxiDriverStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentCreateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentPreLoadDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.ContractDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/affiliation-taxi-driver-independent")
@RequiredArgsConstructor
@Tag(name = "Afiliación Independientes Taxista", description = "Operaciones de Afiliación de Independientes para Taxistas")
@CrossOrigin("*")
public class AffiliationTaxiDriverIndependentController {

    private final AffiliationTaxiDriverIndependentService service;

    @GetMapping("/preload")
    public ResponseEntity<BodyResponseConfig<AffiliationTaxiDriverIndependentPreLoadDTO>> preloadAffiliationData(
            @RequestParam String identificationType,
            @RequestParam String identification,
            @RequestParam String independentType,
            @RequestParam String identificationTypeIndependent,
            @RequestParam String identificationIndependent,
            @RequestParam(required = false) Long decentralizedConsecutive) {

        try {
            AffiliationTaxiDriverIndependentPreLoadDTO response = service.preloadAffiliationData(identificationType,
                    identification, independentType, identificationTypeIndependent, identificationIndependent, decentralizedConsecutive);
            return ResponseEntity.ok(new BodyResponseConfig<>(response, "Get data mercantile"));
        }catch (ResponseStatusException ex){
            BodyResponseConfig<AffiliationTaxiDriverIndependentPreLoadDTO> responseMercantileNotFound = new BodyResponseConfig<>();
            responseMercantileNotFound.setData(service.preloadMercantileNotExists(identificationType, identification,
                    ex.getReason()));
            responseMercantileNotFound.setMessage(ex.getReason());
            return ResponseEntity.status(HttpStatus.OK).body(responseMercantileNotFound);
        }

    }

    @PostMapping("/create")
    public ResponseEntity<Long> createAffiliation(@RequestBody AffiliationTaxiDriverIndependentCreateDTO dto) {
        Long idAffiliation = service.createAffiliation(dto);
        return ResponseEntity.ok(idAffiliation);
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateAffiliation(@RequestBody AffiliationTaxiDriverIndependentUpdateDTO dto) {
        service.updateAffiliation(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(value = "/upload-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Affiliation> uploadDocuments(AffiliationIndependentTaxiDriverStep3DTO dto,
                                                       @RequestParam(name = "documents") List<MultipartFile> documents) {
        try {
            // Pasar la lista completa de documentos al servicio
            Affiliation response = service.uploadDocuments(dto, documents);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AffiliationError ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al subir los documentos para la afiliación", ex);
        }
    }

    // Método para el cálculo del IBC con base en los parámetros del contrato
    @GetMapping("/calculate-ibc")
    public ResponseEntity<ContractDTO> calculateIbcForContract(
            @RequestParam String contractType,
            @RequestParam String contractDuration,
            @RequestParam String contractStartDate,
            @RequestParam String contractEndDate,
            @RequestParam String contractMonthlyValue) {
        try {
            return service.findContractForIbcCalculation(contractType, contractDuration, contractStartDate, contractEndDate, contractMonthlyValue)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (ResponseStatusException ex) {
            throw ex; // Rethrow para manejar las excepciones con los códigos de estado adecuados
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el cálculo del IBC", ex);
        }
    }

    @GetMapping("/get-work-center-address")
    public ResponseEntity<WorkCenterAddressIndependentDTO> getWorkCenterAddress(@RequestParam Long idAffiliateMercantile) {
        try {
            // Llamar al servicio para obtener los datos de la dirección del contratante
            WorkCenterAddressIndependentDTO addressDTO = service.getWorkCenterAddress(idAffiliateMercantile);
            return ResponseEntity.ok(addressDTO);
        } catch (ResponseStatusException e) {
            // Manejo de excepciones si los datos no son encontrados
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al obtener la dirección", e);
        }
    }

    @PostMapping(value = "/createaffiliation/step3pila")
    public ResponseEntity<Affiliation> createAffiliationStep3FromPila(AffiliationIndependentTaxiDriverStep3DTO dto) {
        try{
            Affiliation response = service.createAffiliationStep3FromPila(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (AffiliationAlreadyExistsError ex){
            throw new AffiliationAlreadyExistsError(Type.ERROR_AFFILIATION_ALREADY_EXISTS);
        }
    }

}
