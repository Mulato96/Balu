package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employment relationship information for selection")
public class EmploymentRelationshipDTO {

    @Schema(description = "Unique relationship identifier", example = "rel-12345")
    private String relationshipId;

    @Schema(description = "Affiliation type", example = "DEPENDIENTE")
    private String affiliationType;

    @Schema(description = "Job position/role", example = "DESARROLLADOR")
    private String jobPosition;

    @Schema(description = "Contract start date", example = "2024-01-01")
    private String contractStartDate;

    @Schema(description = "Contract end date (if applicable)", example = "2024-12-31")
    private String contractEndDate;

    @Schema(description = "Relationship status", example = "ACTIVO")
    private String status;

    @Schema(description = "Additional description for identification", example = "Contrato principal - Desarrollador Senior")
    private String description;
}
