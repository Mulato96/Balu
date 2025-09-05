package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionServiceAffiliationStep3DTO {

    private Long id;
    private String identificationDocumentTypeSignatory;
    private String identificationDocumentNumberSignatory;
    private String firstNameSignatory;
    private String secondNameSignatory;
    private String surnameSignatory;
    private String secondSurnameSignatory;
    private String occupationSignatory;
    private String risk;
    private BigDecimal price;
    private BigDecimal contractIbcValue;
    private String filedNumber;

}
