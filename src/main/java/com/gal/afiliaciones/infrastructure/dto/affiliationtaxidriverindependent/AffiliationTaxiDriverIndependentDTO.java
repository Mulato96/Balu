package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliationTaxiDriverIndependentDTO {

    private String identificationType;

    private String identification;

}
