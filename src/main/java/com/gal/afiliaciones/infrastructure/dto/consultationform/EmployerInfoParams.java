package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerInfoParams {

    private String identificationNumber;
    private String verificationDigit;
    private String economicActivity;
    private Long department;
    private Long city;
    private String address;
    private String phone1;
    private String phone2;
    private String email;
    private String documentTypeLegalRep;
    private String identificationNumberLegalRep;
    private String legalRepresentativeName;
    private String nature;
    private String typeInfo;
}
