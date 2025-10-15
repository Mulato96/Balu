package com.gal.afiliaciones.infrastructure.dto.affiliate;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainOfficeDTO {

    private String mainOfficeName;
    private String mainOfficeZone;
    private AddressDTO addressDTO;
    private String mainOfficePhoneNumber;
    private String mainOfficeEmail;

    private Boolean main;
    private String mainPhoneNumberTwo;
    private List<Long> economicActivity;
    private String typeDocumentResponsibleHeadquarters;
    private String numberDocumentResponsibleHeadquarters;
    private String firstNameResponsibleHeadquarters;
    private String secondNameResponsibleHeadquarters;
    private String surnameResponsibleHeadquarters;
    private String secondSurnameResponsibleHeadquarters;
    private String phoneOneResponsibleHeadquarters;
    private String phoneTwoResponsibleHeadquarters;
    private String emailResponsibleHeadquarters;

    private Long officeManager;
    private Long idAffiliateEmployer;

    // Optional on creation only; ignored on update
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long idSedePositiva;
}
