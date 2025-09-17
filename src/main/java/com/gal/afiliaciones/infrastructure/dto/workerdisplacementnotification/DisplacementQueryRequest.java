package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplacementQueryRequest {
    // Employer is inferred from current logged-in user; no employer fields required

    @Schema(description = "Worker identification number", example = "15959336")
    @NotBlank(message = "El número de identificación del trabajador es requerido")
    private String workerIdentificationNumber;

    @Schema(description = "Worker identification type", example = "CC")
    @NotBlank(message = "El tipo de identificación del trabajador es requerido")
    private String workerIdentificationType;

    // ===== RELATIONSHIP SELECTION (Optional for query) =====
    
    @Schema(description = "Employment relationship ID (required when worker has multiple active relationships)", example = "rel-12345")
    private String employmentRelationshipId;

    // ===== PAGINATION AND SORTING =====
    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page;
    @Schema(description = "Page size", example = "10")
    private Integer size;
    @Schema(description = "Sort by field", example = "filedNumber")
    private String sortBy;
    @Schema(description = "Sort order (asc|desc)", example = "asc")
    private String sortOrder;
}
