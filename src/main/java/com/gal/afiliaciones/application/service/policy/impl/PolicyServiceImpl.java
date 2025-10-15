package com.gal.afiliaciones.application.service.policy.impl;

import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyDao policyDao;

    @Override
    public Policy createPolicy(String idType, String idNumber, LocalDate effectiveDateFrom, Long idPolicyType,
                               Long idAffiliate, Long decentralizedConsecutive, String nameCompany) {
        return policyDao.createPolicy(idType, idNumber, effectiveDateFrom, idPolicyType, idAffiliate, decentralizedConsecutive, nameCompany);
    }

    @Override
    public Policy createPolicyDependent(String idType, String idNumber, LocalDate effectiveDateFrom, Long idAffiliate,
                                        String code, String nameCompany){
        return policyDao.createPolicyDependent(idType, idNumber, effectiveDateFrom, idAffiliate, code, nameCompany);
    }

}
