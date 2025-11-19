package com.gal.afiliaciones.infrastructure.controller.valuecontract;

import com.gal.afiliaciones.application.service.IValueContratService;
import com.gal.afiliaciones.infrastructure.dto.ValueContractDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueContractRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueUserContractDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/value-contract")
@Tag(
        name = "Value Contract Controller",
        description = "Controlador para obtener y registrar información del valor de contratos"
)
@AllArgsConstructor
public class ValueContractController {

    private final IValueContratService contratService;

    /**
     * Retrieves contract information associated with a user based on document type and number.
     *
     * @param typeDocument Type of document (e.g., CC, TI, CE). Will be converted to uppercase.
     * @param documentNumber User's identification number.
     * @return ValueUserContractDTO containing contract details.
     */
    @Operation(
            summary = "Get Contract Value by Document Type and Number",
            description = "Obtiene el valor del contrato y la información asociada mediante el tipo y número de documento."
    )
    @GetMapping("/{typeDocument}/{documentNumber}")
    public ResponseEntity<?> getValueContract(
            @Parameter(description = "Tipo de documento del usuario (CC, TI, CE, etc.)", example = "CC")
            @PathVariable String typeDocument,
            @Parameter(description = "Número de documento del usuario", example = "1022334455")
            @PathVariable String documentNumber
    ) {
        // Convertir el tipo de documento a mayúsculas (CC siempre en mayúscula)
        String typeDocumentUpper = typeDocument.toUpperCase();

        ValueUserContractDTO dto = contratService.getUserContractInfo(typeDocumentUpper, documentNumber);

        // Si el DTO es null o tiene campos vacíos y no viene del registro, el usuario no se encontró
        if (dto == null || dto.getFirstName().isEmpty()) {
            return ResponseEntity.status(404).body(new ArrayList<>());
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * Saves a new contract value record.
     *
     * @param dto Contract information to persist.
     * @return ValueUserContractDTO saved information with user data.
     */
    @Operation(
            summary = "Save Contract Value",
            description = "Registra o actualiza la información del valor de un contrato usando el número de contrato (filedNumber)."
    )
    @PostMapping
    public ResponseEntity<ValueUserContractDTO> saveValueContract(
            @Parameter(description = "Información del contrato a guardar (debe incluir numContract/filedNumber)")
            @RequestBody ValueContractRequestDTO dto
    ) {
        ValueUserContractDTO saved = contratService.saveContract(dto);
        return ResponseEntity.ok(saved);
    }

}
