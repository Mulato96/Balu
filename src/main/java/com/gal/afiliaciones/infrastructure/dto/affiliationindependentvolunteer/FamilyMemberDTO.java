package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberDTO {

    private String idDocumentTypeFamilyMember;
    private String idDocumentNumberFamilyMember;
    private String firstNameFamilyMember;
    private String secondNameFamilyMember;
    private String surnameFamilyMember;
    private String secondSurnameFamilyMember;
    private AddressDTO addressFamilyMember;
    private String phone1FamilyMember;
    private String phone2FamilyMember;
    private String emailFamilyMember;

}
