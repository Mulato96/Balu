package com.gal.afiliaciones.application.service.affiliatecompany.data.impl;

import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.domain.model.*;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.*;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// removed unused imports after simplifying main office lookup
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateDataServiceImpl implements AffiliateDataService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final HealthPromotingEntityRepository healthRepository;
    private final FundPensionRepository fundPensionRepository;
    private final MunicipalityRepository municipalityRepository;
    private final DepartmentRepository departmentRepository;
    private final OccupationRepository occupationRepository;
    private final ArlRepository arlRepository;
    private final MainOfficeRepository mainOfficeRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final WorkCenterRepository workCenterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    @Override
    public List<AffiliationDependent> findDependentsByDocument(String documentType, String documentNumber) {
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][DATA] findDependentsByDocument start tDoc={} id={}", documentType, documentNumber);
        try {
            return affiliationDependentRepository.findByDocumentTypeAndNumber(documentType, documentNumber);
        } finally {
            log.debug("[BALU][DATA] findDependentsByDocument done in {}ms", (System.currentTimeMillis() - t0));
        }
    }

    @Override
    public List<Affiliation> findIndependentsByDocument(String documentType, String documentNumber) {
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][DATA] findIndependentsByDocument start tDoc={} id={}", documentType, documentNumber);
        try {
            return affiliationDetailRepository.findByDocumentTypeAndNumber(documentType, documentNumber);
        } finally {
            log.debug("[BALU][DATA] findIndependentsByDocument done in {}ms", (System.currentTimeMillis() - t0));
        }
    }

    @Override
    public Optional<Affiliate> findEmployerAffiliate(Long idAffiliateEmployer) {
        if (idAffiliateEmployer == null) return Optional.empty();
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][DATA] findEmployerAffiliate start idEmployer={}", idAffiliateEmployer);
        try {
            return affiliateRepository.findByIdAffiliate(idAffiliateEmployer);
        } finally {
            log.debug("[BALU][DATA] findEmployerAffiliate done in {}ms", (System.currentTimeMillis() - t0));
        }
    }

    @Override
    public Optional<Affiliate> findAffiliateByFiledNumber(String filedNumber) {
        if (filedNumber == null) return Optional.empty();
        try {
            long t0 = System.currentTimeMillis();
            log.debug("[BALU][DATA] findAffiliateByFiledNumber start filed={} ", filedNumber);
            try {
                return affiliateRepository.findByFiledNumber(filedNumber);
            } finally {
                log.debug("[BALU][DATA] findAffiliateByFiledNumber done in {}ms", (System.currentTimeMillis() - t0));
            }
        } catch (Exception e) {
            log.debug("Error finding affiliate by filed number: {}", filedNumber, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Department> findDepartment(Long departmentId) {
        if (departmentId == null) return Optional.empty();
        return departmentRepository.findById(departmentId);
    }

    @Override
    public Optional<Municipality> findMunicipality(Long municipalityId) {
        if (municipalityId == null) return Optional.empty();
        return municipalityRepository.findById(municipalityId);
    }

    @Override
    public Optional<Occupation> findOccupation(Long occupationId) {
        if (occupationId == null) return Optional.empty();
        return occupationRepository.findById(occupationId);
    }

    @Override
    public Optional<Occupation> findOccupationByName(String occupationName) {
        if (occupationName == null) return Optional.empty();
        return occupationRepository.findAll().stream()
            .filter(occ -> occupationName.equalsIgnoreCase(occ.getNameOccupation()))
            .findFirst();
    }

    @Override
    public Optional<FundPension> findPensionFund(Long pensionFundId) {
        if (pensionFundId == null) return Optional.empty();
        return fundPensionRepository.findById(pensionFundId);
    }

    @Override
    public Optional<Health> findHealthEntity(Long healthEntityId) {
        if (healthEntityId == null) return Optional.empty();
        return healthRepository.findById(healthEntityId);
    }

    @Override
    public Optional<Arl> findArlByCode(String arlCode) {
        if (arlCode == null) return Optional.empty();
        return arlRepository.findByCodeARL(arlCode);
    }

    @Override
    public Optional<EconomicActivity> findEconomicActivity(String economicActivityCode) {
        if (economicActivityCode == null) return Optional.empty();
        return economicActivityRepository.findByEconomicActivityCode(economicActivityCode)
            .stream().findFirst();
    }

    @Override
    public Optional<AffiliateMercantile> findAffiliateMercantile(String documentType, String documentNumber) {
        if (documentType == null || documentNumber == null) return Optional.empty();
        return affiliateMercantileRepository
            .findByTypeDocumentIdentificationAndNumberIdentification(documentType, documentNumber);
    }

    @Override
    public Optional<MainOffice> findMainOfficeByAffiliate(Long affiliateId) {
        if (affiliateId == null) return Optional.empty();
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][DATA] findMainOfficeByAffiliate start affiliateId={}", affiliateId);
        try {
            return mainOfficeRepository.findFirstByIdAffiliateAndMainTrueOrderByIdAsc(affiliateId);
        } finally {
            log.debug("[BALU][DATA] findMainOfficeByAffiliate done in {}ms", (System.currentTimeMillis() - t0));
        }
    }

    @Override
    public List<WorkCenter> findWorkCentersByManager(Long managerId) {
        if (managerId == null) return List.of();
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][DATA] findWorkCentersByManager start managerId={}", managerId);
        try {
            return workCenterRepository.findFirstByWorkCenterManager_IdOrderByIdAsc(managerId)
                .map(List::of)
                .orElseGet(List::of);
        } finally {
            log.debug("[BALU][DATA] findWorkCentersByManager done in {}ms", (System.currentTimeMillis() - t0));
        }
    }
}
