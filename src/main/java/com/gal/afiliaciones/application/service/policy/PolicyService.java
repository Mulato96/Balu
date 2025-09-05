package com.gal.afiliaciones.application.service.policy;

import com.gal.afiliaciones.domain.model.Policy;

import java.time.LocalDate;

public interface PolicyService {

    Policy createPolicy(String idType, String idNumber, LocalDate effectiveDateFrom, Long idPolicyType, Long idAffiliate, Long decentralizedConsecutive, String nameCompany);
    Policy createPolicyDependent(String idType, String idNumber, LocalDate effectiveDateFrom, Long idAffiliate, String code, String nameCompany);

}
