package com.gal.afiliaciones.application.service.contract.impl;

import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl.AffiliationEmployerDomesticServiceIndependentServiceImpl;
import com.gal.afiliaciones.application.service.contract.ContractService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractFilterDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final AffiliationEmployerDomesticServiceIndependentServiceImpl affiliationService;

    @Override
    public List<ContractEmployerResponseDTO> findContractsByEmployer(ContractFilterDTO filters){
        List<ContractEmployerResponseDTO> contractsList = affiliateRepository.findContractsIndependent(filters.getIdentificationType(),
                        filters.getIdentificationNumber(), filters.getEmployerName())
                .stream()
                .map(p -> new ContractEmployerResponseDTO(
                        p.getCompany(),
                        p.getStartContractDate(),
                        p.getEndContractDate(),
                        p.getStageManagement(),
                        p.getStatus(),
                        p.getFiledNumber(),
                        p.getBondingType(),
                        p.getIdAffiliate(),
                        p.getAffiliationType()
                ))
                .toList();

        if(filters.getUpdateRequired()!=null){
            if(Boolean.TRUE.equals(filters.getUpdateRequired())){
                return contractsList.stream().filter(affiliation ->
                        affiliation.getStageManagement().equals(Constant.PENDING_COMPLETE_FORM)).toList();
            }else{
                return contractsList.stream().filter(affiliation ->
                        !affiliation.getStageManagement().equals(Constant.PENDING_COMPLETE_FORM)).toList();
            }
        }

        return contractsList;
    }

    @Override
    public Object getStep1Pila(String filedNumber){
        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate not found"));

        Specification<Affiliation> spc = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(filedNumber);
        Optional<Affiliation> optionalAffiliation = repositoryAffiliation.findOne(spc);
        if(optionalAffiliation.isPresent()){
            Affiliation affiliation = optionalAffiliation.get();
            return affiliationService.getAffiliationDataByType(affiliate.getAffiliationSubType(), affiliation);
        }
        return new Affiliation();
    }

}
