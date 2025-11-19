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
    private String departmentName;
    private String cityName;

    public EmployerBasicDTO(EmployerBasicProjection projection) {
        this.employerId = projection.getEmployerId();
        this.docType = projection.getDocType();
        this.docNumber = projection.getDocNumber();
        this.dv = projection.getDv();
        this.businessName = projection.getBusinessName();
        this.departmentId = projection.getDepartmentId();
        this.cityId = projection.getCityId();
        this.addressFull = projection.getAddressFull();
        this.phone1 = projection.getPhone1();
        this.phone2 = projection.getPhone2();
        this.email = projection.getEmail();
        this.employerType = projection.getEmployerType();
        this.departmentName = projection.getDepartmentName();
        this.cityName = projection.getCityName();
    }
}
