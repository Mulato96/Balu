package com.gal.afiliaciones.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistryOfficeDTO {

    private String birthDate;
    private String firstLastName;
    private String issuingMunicipality;
    private String issuingDepartment;
    private String expeditionDate;
    private String resolutionNumber;
    private String firstName;
    private String secondName;
    private String idStatus;
    private int errorCode;
    private int identificationNumber;
    private String gender;
    private String secondLastName;
    private String resolutionYear;
    private String informate;
    private String serialNumber;
    private String deathDate;
    private String referenceDate;
    private String affectingDate;

}
