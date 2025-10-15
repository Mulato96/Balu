package com.gal.afiliaciones.application.service.affiliatecompany.data;

import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;

import java.util.List;
import java.util.Optional;

/**
 * Data access service for affiliate company operations.
 * Abstracts repository operations to reduce coupling and improve testability.
 */
public interface AffiliateDataService {
    
    // Document-based queries
    List<AffiliationDependent> findDependentsByDocument(String documentType, String documentNumber);
    List<Affiliation> findIndependentsByDocument(String documentType, String documentNumber);
    
    // Affiliate queries
    Optional<Affiliate> findEmployerAffiliate(Long idAffiliateEmployer);
    Optional<Affiliate> findAffiliateByFiledNumber(String filedNumber);
    
    // Location queries
    Optional<Department> findDepartment(Long departmentId);
    Optional<Municipality> findMunicipality(Long municipalityId);
    
    // Occupation queries
    Optional<Occupation> findOccupation(Long occupationId);
    Optional<Occupation> findOccupationByName(String occupationName);
    
    // Financial entity queries
    Optional<FundPension> findPensionFund(Long pensionFundId);
    Optional<Health> findHealthEntity(Long healthEntityId);
    Optional<Arl> findArlByCode(String arlCode);
    
    // Economic activity queries
    Optional<EconomicActivity> findEconomicActivity(String economicActivityCode);
    
    // Mercantile affiliate queries
    Optional<AffiliateMercantile> findAffiliateMercantile(String documentType, String documentNumber);
    
    // Main office and work center queries
    Optional<MainOffice> findMainOfficeByAffiliate(Long affiliateId);
    List<WorkCenter> findWorkCentersByManager(Long managerId);
}
