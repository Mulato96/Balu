package com.gal.afiliaciones.infrastructure.dao.repository.policy.impl;

import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyDao;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.PolicySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PolicyDaoImpl implements PolicyDao {

    private final PolicyRepository policyRepository;

    private static final String STATUS_ACTIVE = "ACTIVA";

    public Optional<Policy> findExistingPolicy(String idType, String idNumber, Long idPolicyType, Long idAffiliate) {
        Specification<Policy> spc = PolicySpecification.findPolicyByAffiliate(idType, idNumber, STATUS_ACTIVE,
                idPolicyType, idAffiliate);
        return policyRepository.findOne(spc);
    }

    @Override
    @Transactional
    public Policy createPolicy(String idType, String idNumber, LocalDate effectiveDateFrom, Long idPolicyType,
                               Long idAffiliate, Long decentralizedConsecutive, String nameCompany) {
        Optional<Policy> policyOptional = findExistingPolicy(idType, idNumber, idPolicyType, idAffiliate);
        policyOptional.ifPresent(this::deactivateExistingPolicy);

        String currentYear = getCurrentYear();

        //Genera el numero de la poliza
        String policyCode = generatePolicyCode("24", "90", currentYear, getNextConsecutive(currentYear));
        Policy policy = new Policy();

        policy.setCode(policyCode);
        policy.setIdType(idType);
        policy.setIdNumber(idNumber);
        policy.setEffectiveDateFrom(effectiveDateFrom);
        policy.setEffectiveDateTo(effectiveDateFrom.plusYears(2));
        policy.setIssueDate(LocalDate.now());
        policy.setStatus(STATUS_ACTIVE);
        policy.setIdPolicyType(idPolicyType);
        policy.setIdAffiliate(idAffiliate);
        policy.setDecentralizedConsecutive(decentralizedConsecutive);
        policy.setCompanyName(nameCompany);
        policy.setNumPolicyClient(policyRepository.nextNumPolicyCient());
        policyRepository.save(policy);
        return policy;
    }

    public String generatePolicyCode(String ramo, String sucursal, String ano, String consecutivo) {
        int remainingLength = 20 - (ramo.length() + sucursal.length() + ano.length());
        String strFormat = "%0" + remainingLength + "d";

        String formattedConsecutive = String.format(strFormat, Integer.parseInt(consecutivo));

        return ramo + sucursal + ano + formattedConsecutive;
    }

    @Transactional
    public void deactivateExistingPolicy(Policy policy) {
        policy.setStatus("INACTIVA");
        policyRepository.save(policy);
    }

    public String getCurrentYear() {
        // Retornar solo los dos últimos dígitos del año
        return String.valueOf(LocalDate.now().getYear()).substring(2);
    }

    public String getNextConsecutive(String currentYear) {
        int nextNumber = 1;

        // Obtener el código de la última póliza del año actual
        String lastCode = policyRepository.getLastPolicyCode(currentYear);

        if(lastCode!=null && !lastCode.isBlank()) {
            // Extraer el número consecutivo del código (los últimos dígitos)
            String numberStr = lastCode.substring(lastCode.length() - 14);
            int lastNumber = Integer.parseInt(numberStr);

            // Incrementar el número para el próximo consecutivo
            nextNumber = lastNumber + 1;
        }

        return String.valueOf(nextNumber);
    }

    @Override
    public Policy createPolicyDependent(String idType, String idNumber, LocalDate effectiveDateFrom, Long idAffiliate,
                                        String code, String nameCompany){
        Optional<Policy> policyOptional = findExistingPolicy(idType, idNumber, null, idAffiliate);
        policyOptional.ifPresent(this::deactivateExistingPolicy);

        Policy policy = new Policy();
        policy.setCode(code);
        policy.setIdType(idType);
        policy.setIdNumber(idNumber);
        policy.setEffectiveDateFrom(effectiveDateFrom);
        policy.setEffectiveDateTo(effectiveDateFrom.plusYears(2));
        policy.setIssueDate(LocalDate.now());
        policy.setStatus(STATUS_ACTIVE);
        policy.setIdAffiliate(idAffiliate);
        policy.setCompanyName(nameCompany);
        policy.setNumPolicyClient(policyRepository.nextNumPolicyCient());
        policyRepository.save(policy);
        return policy;
    }

}
