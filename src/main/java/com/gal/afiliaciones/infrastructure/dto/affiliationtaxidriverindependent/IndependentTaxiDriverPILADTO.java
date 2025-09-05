package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndependentTaxiDriverPILADTO {

    private String contractorIdentificationType;
    private String contractorIdentificationNumber;
    private String contractorDigiteVerification;
    private String companyName;
    private String contractorEmail;
    private String actualARLContract;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;
    private int age;
    private Long nationality;
    private String gender;
    private String otherGender;
    private Long pensionFundAdministrator;
    private Boolean isForeignPension;
    private Long healthPromotingEntity;
    private Long department;
    private Long cityMunicipality;
    private String address;
    private String phone1;
    private String phone2;
    private String email;
    private String occupation;
    private Boolean is723;
    private WorkCenterAddressIndependentDTO workCenter;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private BigDecimal totalContractValue;
    private BigDecimal monthlyContractValue;
    private EconomicActivityDTO economicActivity;

}
