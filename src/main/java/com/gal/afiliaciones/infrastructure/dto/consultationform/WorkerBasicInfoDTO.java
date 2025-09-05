package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerBasicInfoDTO implements InfoConsultDTO {

    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String otherGender;
    private Long departmentOfResidence;
    private Long cityOfResidence;
    private String fullAddress;
    private Long healthProvider;
    private Long pensionFund;
    private String phoneNumber1;
    private String email;
    private Boolean isActive;
    private String typeInfo;

}