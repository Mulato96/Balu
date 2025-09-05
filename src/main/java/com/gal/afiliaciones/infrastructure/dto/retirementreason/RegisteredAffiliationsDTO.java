package com.gal.afiliaciones.infrastructure.dto.retirementreason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredAffiliationsDTO {
    private String classRisk;
    private String codeCIIU;
    private String additionalCode;
    private String description;
    private String economicActivityCode;
    private Boolean typeActivity;
}
