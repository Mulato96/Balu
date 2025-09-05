package com.gal.afiliaciones.infrastructure.dto.employer;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateDataBasicDTO {

    private String affiliationSubType;
    private String documentTypeEmployer;
    private String documentNumberEmployer;
    private AddressDTO addressEmployer;
    private String phone1Employer;
    private String phone2Employer;
    private String emailEmployer;
    private Boolean acceptDataProcessingPolicies;

}
