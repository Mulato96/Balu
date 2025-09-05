package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndependentWorkerDTO {

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
    private String nationality;
    private Long healthPromotingEntity;
    private Long pensionFundAdministrator;
    private AddressDTO address;
    private String phone1;
    private String phone2;
    private String email;
    private Long idOccupation;

}
