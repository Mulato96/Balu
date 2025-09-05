package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationIndependentTaxiDriverStep3DTO {

    private Long idAffiliation;
    private String occupation;
    private String risk;
    private BigDecimal price;
    private BigDecimal contractIbcValue;

}
