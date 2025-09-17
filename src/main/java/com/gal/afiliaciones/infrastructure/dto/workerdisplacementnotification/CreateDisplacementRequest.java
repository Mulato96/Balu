package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new displacement notification")
public class CreateDisplacementRequest {

    // ===== WORKER IDENTIFICATION =====
    
    @NotBlank(message = "Tipo de documento del trabajador es requerido")
    @Schema(description = "Worker document type", example = "CC")
    private String workerDocumentType;

    @NotBlank(message = "Número de documento del trabajador es requerido")
    @Schema(description = "Worker document number", example = "15959336")
    private String workerDocumentNumber;

    // ===== RELATIONSHIP SELECTION (Conditional) =====
    
    @Schema(description = "Employment relationship ID (required when worker has multiple active relationships)", example = "rel-12345")
    private String employmentRelationshipId;

    @Schema(description = "Sub-company ID (required only when employer NIT is 899999061)", example = "sub-001")
    private String subCompanyId;

    // ===== DISPLACEMENT INFORMATION =====
    
    @NotNull(message = "Fecha de inicio de desplazamiento es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement start date", example = "2025-12-22")
    private LocalDate displacementStartDate;

    @NotNull(message = "Fecha de fin de desplazamiento es requerida")
    @Future(message = "Fecha de fin debe ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement end date", example = "2025-12-27")
    private LocalDate displacementEndDate;

    @NotNull(message = "Id de departamento es requerido")
    @Schema(description = "Displacement department id", example = "11")
    private Integer displacementDepartmentId;

    @NotNull(message = "Id de municipio es requerido")
    @Schema(description = "Displacement municipality id", example = "11001")
    private Long displacementMunicipalityId;

    @NotBlank(message = "Motivo de desplazamiento es requerido")
    @Schema(description = "Displacement reason", example = "DESPLAZAMIENTO POR PROYECTO")
    private String displacementReason;

    // ===== VALIDATION METHODS =====

    /**
     * Validate that end date is after start date
     */
    public boolean isDateRangeValid() {
        if (displacementStartDate == null || displacementEndDate == null) {
            return false;
        }
        return displacementEndDate.isAfter(displacementStartDate);
    }

    // External integration payload is no longer constructed here; employer is inferred server-side
}
