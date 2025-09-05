package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatoryDataIndependentDTO {

    private String identificationDocumentTypeSignatory;
    private String identificationDocumentNumberSignatory;
    private String firstNameSignatory;
    private String secondNameSignatory;
    private String surnameSignatory;
    private String secondSurnameSignatory;
    private String occupationSignatory;

}
