package com.gal.afiliaciones.application.service.contract.impl;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl.AffiliationEmployerDomesticServiceIndependentServiceImpl;
import com.gal.afiliaciones.application.service.contract.ContractService;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractFilterDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final AffiliationEmployerDomesticServiceIndependentServiceImpl affiliationService;

    @Override
    public List<ContractEmployerResponseDTO> findContractsByEmployer(ContractFilterDTO filters){
        List<ContractEmployerResponseDTO> contractsList = new ArrayList<>();

        Specification<Affiliate> spcAffiliate = AffiliateSpecification.findByIndependentEmployer(filters.getIdentificationType(),
                filters.getIdentificationNumber(), filters.getEmployerName());
        List<Affiliate> affiliateList = affiliateRepository.findAll(spcAffiliate);

        if(affiliateList.isEmpty())
            return new ArrayList<>();

        affiliateList.forEach(affiliate -> {
            ContractEmployerResponseDTO contract = new ContractEmployerResponseDTO();
            // Busca por radicado el detalle de la afiliacion
            Optional<Affiliation> affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber());
            if (affiliation.isPresent()) {
                contract.setCompany(affiliate.getCompany());
                contract.setStageManagement(affiliation.get().getStageManagement());
                contract.setStartContractDate(affiliation.get().getContractStartDate() != null ?
                        affiliation.get().getContractStartDate().toString() : "");
                contract.setEndContractDate(affiliation.get().getContractEndDate() != null ?
                        affiliation.get().getContractEndDate().toString() : "");
                contract.setStatus(affiliate.getAffiliationStatus());
                contract.setFiledNumber(affiliate.getFiledNumber());
                contract.setBondingType(affiliate.getAffiliationSubType());
                contract.setIdAffiliate(affiliate.getIdAffiliate());
                contract.setAffiliationType(affiliate.getAffiliationType());
                contractsList.add(contract);
            }
        });

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
