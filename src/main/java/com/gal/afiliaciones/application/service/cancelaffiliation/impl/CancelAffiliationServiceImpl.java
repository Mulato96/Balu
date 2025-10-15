package com.gal.afiliaciones.application.service.cancelaffiliation.impl;

import com.gal.afiliaciones.application.service.cancelaffiliation.ICancelAffiliationService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.config.ex.cancelaffiliation.CancelAffiliationNotFoundException;
import com.gal.afiliaciones.config.ex.cancelaffiliation.DateCancelAffiliationException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelAffiliationServiceImpl implements ICancelAffiliationService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;

    @Override
    public CancelAffiliateDTO consultAffiliation(String documentType, String documentNumber, Long idAffiliateEmployer) {

        Specification<AffiliationDependent> specDependent = AffiliationDependentSpecification
                .findByTypeDependentAndEmployer(documentType, documentNumber, idAffiliateEmployer);
        List<AffiliationDependent> affiliationDependentList = affiliationDependentRepository.findAll(specDependent);

        if(affiliationDependentList.isEmpty())
            throw new CancelAffiliationNotFoundException(Constant.CANCEL_AFFILIATION_NOT_FOUND);

        affiliationDependentList.sort(Comparator.comparing(AffiliationDependent::getId).reversed());
        AffiliationDependent affiliationDependent = affiliationDependentList.get(0);

        Specification<Affiliate> specAffiliate = AffiliateSpecification
                .findByField(affiliationDependent.getFiledNumber());
        Affiliate affiliate = affiliateRepository.findOne(specAffiliate)
                .orElseThrow(() -> new CancelAffiliationNotFoundException(Constant.CANCEL_AFFILIATION_NOT_FOUND));

        if (Boolean.TRUE.equals(affiliate.getAffiliationCancelled())
                && affiliate.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_INACTIVE)) {
            throw new CancelAffiliationNotFoundException(Constant.ERROR_AFFILIATION);
        }

        if (ChronoUnit.DAYS.between(affiliationDependent.getCoverageDate(), LocalDate.now()) > 1) {
            throw new DateCancelAffiliationException(Constant.DATE_CANCEL_AFFILIATION);
        }

        CancelAffiliateDTO cancelAffiliateDTO = new CancelAffiliateDTO();
        BeanUtils.copyProperties(affiliationDependent, cancelAffiliateDTO);
        cancelAffiliateDTO.setContractType(affiliate.getAffiliationType());

        return cancelAffiliateDTO;
    }

    @Transactional
    @Override
    public void updateStatusCanceledAffiliate(String filedNumber, String observation) {

        Affiliate affiliate = affiliateRepository.findByFiledNumber(filedNumber)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate dependent not found"));

        affiliate.setAffiliationCancelled(Boolean.TRUE);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);
        affiliate.setDateAffiliateSuspend(LocalDateTime.now());
        affiliate.setObservation(observation);
        affiliateRepository.save(affiliate);

        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                .idAffiliation(affiliate.getIdAffiliate())
                .filedNumber(affiliate.getFiledNumber())
                .noveltyType(Constant.CANCELLATION_SUCCESSFUL)
                .status(Constant.APPLIED)
                .observation(observation)
                .build();

        generalNoveltyServiceImpl.saveGeneralNovelty(request);
    }

}