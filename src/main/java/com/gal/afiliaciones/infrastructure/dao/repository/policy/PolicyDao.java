package com.gal.afiliaciones.infrastructure.dao.repository.policy;

import com.gal.afiliaciones.domain.model.Policy;

import java.time.LocalDate;

public interface PolicyDao {


    Policy createPolicy(String idType, String idNumber, LocalDate effectiveDateFrom, Long idPolicyType,
                        Long idAffiliate, Long decentralizedConsecutive, String nameCompany);
    Policy createPolicyDependent(String idType, String idNumber, LocalDate effectiveDateFrom, Long idAffiliate,
                                 String code, String nameCompany);

}
