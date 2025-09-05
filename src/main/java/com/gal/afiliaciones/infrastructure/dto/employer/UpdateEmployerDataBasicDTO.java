package com.gal.afiliaciones.infrastructure.dto.employer;

import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployerDataBasicDTO {

    private String documentTypeEmployer;
    private String documentNumberEmployer;
    private Integer digitVerificationEmployer;
    private String businessNameEmployer;
    private List<RegisteredAffiliationsDTO> economicActivityListEmployer;
    private AddressDTO addressEmployer;
    private String phone1Employer;
    private String phone2Employer;
    private String emailEmployer;
    private Boolean acceptDataProcessingPolicies;

}
