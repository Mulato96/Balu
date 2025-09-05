package com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidRelationShipResponse {
    private String messageResponse;
    private String firstNameContributor;
    private String secondNameContributor;
    private String firstSurNameContributor;
    private String secondSurNameContributor;

}
