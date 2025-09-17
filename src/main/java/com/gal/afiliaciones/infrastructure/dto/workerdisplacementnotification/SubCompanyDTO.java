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
@Schema(description = "Sub-company information for employer 899999061")
public class SubCompanyDTO {

    @Schema(description = "Sub-company unique identifier", example = "sub-001")
    private String subCompanyId;

    @Schema(description = "Sub-company name", example = "SECRETARÍA DE EDUCACIÓN")
    private String subCompanyName;

    @Schema(description = "Sub-company code", example = "SEDU")
    private String subCompanyCode;

    @Schema(description = "Sub-company status", example = "ACTIVO")
    private String status;
}
