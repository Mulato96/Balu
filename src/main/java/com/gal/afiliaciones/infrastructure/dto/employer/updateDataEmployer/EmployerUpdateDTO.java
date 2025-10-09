package com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer;

import lombok.Data;

@Data
public class EmployerUpdateDTO {

    private String employerType;
    private Section section;
    private String docType;
    private String docNumber;
    private String docTypeNew;
    private String businessName;
    private String departmentId;
    private String cityId;
    private String addressFull;
    private String phone1;
    private String phone2;
    private String email;

    public enum Section { BASIC, LEGAL_REP }
}
