package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request DTO for updating an existing displacement notification")
public class UpdateDisplacementRequest {

    @NotNull(message = "ID del desplazamiento es requerido")
    @Schema(description = "Displacement notification ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    // ===== RELATIONSHIP SELECTION (Conditional) =====
    
    @Schema(hidden = true)
    private String employmentRelationshipId;

    @Schema(hidden = true)
    private String subCompanyId;

    // ===== DISPLACEMENT INFORMATION (Only editable fields) =====
    
    @NotNull(message = "Fecha de inicio de desplazamiento es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement start date", example = "2024-07-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate displacementStartDate;

    @NotNull(message = "Fecha de fin de desplazamiento es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement end date", example = "2024-07-05", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate displacementEndDate;

    @NotNull(message = "Id de departamento es requerido")
    @Schema(description = "Displacement department id", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displacementDepartmentId;

    @NotNull(message = "Id de municipio es requerido")
    @Schema(description = "Displacement municipality id", example = "11001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long displacementMunicipalityId;

    @NotBlank(message = "Motivo de desplazamiento es requerido")
    @Schema(description = "Displacement reason", example = "DESPLAZAMIENTO POR PROYECTO ACTUALIZADO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displacementReason;

    // ===== VALIDATION METHODS =====

    /**
     * Validate that end date is after start date
     */
    @Schema(hidden = true)
    public boolean isDateRangeValid() {
        if (displacementStartDate == null || displacementEndDate == null) {
            return false;
        }
        return displacementEndDate.isAfter(displacementStartDate);
    }

    /**
     * Check if start date is in the future (can be edited)
     */
    @Schema(hidden = true)
    public boolean isStartDateEditable() {
        return displacementStartDate != null && displacementStartDate.isAfter(LocalDate.now());
    }
}
