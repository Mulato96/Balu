package com.gal.afiliaciones.infrastructure.dto.policy;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PolicyRequestDTO {

    private String idType;
    private String idNumber;
    private LocalDate effectiveDateFrom;
    private Long idPolicyType;
    private Long idAffiliate;
    private String nameCompany;

}
