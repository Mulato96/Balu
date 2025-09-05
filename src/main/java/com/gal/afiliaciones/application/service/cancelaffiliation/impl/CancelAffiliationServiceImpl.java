package com.gal.afiliaciones.application.service.cancelaffiliation.impl;

import com.gal.afiliaciones.application.service.cancelaffiliation.ICancelAffiliationService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.cancelaffiliation.CancelAffiliationNotFoundException;
import com.gal.afiliaciones.config.ex.cancelaffiliation.DateCancelAffiliationException;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CancelAffiliationServiceImpl implements ICancelAffiliationService {

    private final AffiliateRepository affiliateRepository;
    private final IUserPreRegisterRepository userMainRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;

    @Override
    public CancelAffiliateDTO consultAffiliation(String documentType, String documentNumber, Long idUser,
            String subType) {

        Specification<AffiliationDependent> specDependent = AffiliationDependentSpecification
                .findByTypeAndNumberDocument(documentType, documentNumber);
        AffiliationDependent affiliationDependent = affiliationDependentRepository.findOne(specDependent)
                .orElseThrow(() -> new CancelAffiliationNotFoundException(Constant.CANCEL_AFFILIATION_NOT_FOUND));

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

        validWorkedCompany(idUser, affiliate.getNitCompany(), subType);

        CancelAffiliateDTO cancelAffiliateDTO = new CancelAffiliateDTO();
        BeanUtils.copyProperties(affiliationDependent, cancelAffiliateDTO);
        cancelAffiliateDTO.setContractType(affiliate.getAffiliationType());

        return cancelAffiliateDTO;
    }

    @Transactional
    @Override
    public void updateStatusCanceledAffiliate(String identification, String observation) {

        Page<Affiliate> page = affiliateRepository.findAll(UserSpecifications.hasIdentification(identification),
                PageRequest.of(0, 1));
        Optional<Affiliate> entity = page.stream().findFirst();
        entity.ifPresent(affiliate -> {
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
        });
    }

    private void validWorkedCompany(Long idUser, String nitCompanyWorker, String subType) {

        UserMain user = userMainRepository.findById(idUser)
                .orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE));

        Specification<Affiliate> specAffiliate = AffiliateSpecification
                .findByEmployerByNumberDocumentAndSubType(user.getIdentification(), subType);
        Affiliate affiliate = affiliateRepository.findOne(specAffiliate)
                .orElseThrow(() -> new CancelAffiliationNotFoundException(Constant.AFFILIATE_NOT_FOUND));

        if (!Objects.equals(nitCompanyWorker, affiliate.getNitCompany())) {
            throw new DateCancelAffiliationException(Constant.WORKER_UNCONNECTED);
        }

    }
}