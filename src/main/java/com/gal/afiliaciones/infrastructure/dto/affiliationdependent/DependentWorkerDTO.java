package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependentWorkerDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String otherGender;
    private Long nationality;
    private Long healthPromotingEntity;
    private Long pensionFundAdministrator;
    private String occupationalRiskManager;
    private AddressDTO address;
    private String phone1;
    private String phone2;
    private String email;
    private Long idWorkModality;
    private BigDecimal salary;
    private Long idOccupation;
    private Boolean userFromRegistry;

}
