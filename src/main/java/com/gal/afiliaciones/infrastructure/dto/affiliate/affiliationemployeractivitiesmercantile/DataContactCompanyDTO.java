package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataContactCompanyDTO {

    private String phoneOneContactCompany;
    private String phoneTwoContactCompany;
    private String emailContactCompany;
    private Long departmentContactCompany;
    private Long cityMunicipalityContactCompany;
    private AddressDTO addressDTO;
}
