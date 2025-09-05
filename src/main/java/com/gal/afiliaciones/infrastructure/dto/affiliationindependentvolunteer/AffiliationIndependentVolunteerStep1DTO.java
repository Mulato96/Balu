package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationIndependentVolunteerStep1DTO {

    private Long idAffiliation;
    private IndependentVolunteerDTO independentVolunteerData;
    private AddressDTO independentVolunteerAddress;
    private String phone1;
    private String phone2;
    private String email;
    private FamilyMemberDTO familyMember;
    private AddressDTO occupationAddress;
    private String occupation;
    private String occupationPhone1;
    private String occupationPhone2;
    private BigDecimal contractIbcValue;

}
