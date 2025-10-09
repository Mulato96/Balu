package com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class EmployerBasicDTO {

    private String employerId;
    private String docType;
    private String docNumber;
    private String dv;
    private String employerType;
    private String businessName;
    private String departmentId;
    private String cityId;
    private String addressFull;
    private String phone1;
    private String phone2;
    private String email;
    private String rlDocType;
    private String rlDocNumber;
    private String rlFirstName;
    private String rlSecondName;
    private String rlSurname;
    private String rlSecondSurname;
    private String rlBirthDate;
    private String rlAge;
    private String rlSex;
    private String rlNationality;
    private String epsId;
    private String afpId;
}
