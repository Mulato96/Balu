package com.gal.afiliaciones.application.service.affiliatecompany.excel;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * V2 Service for enriching Excel affiliate company data with descriptive names.
 * Similar to ExcelAffiliateCompanyEnrichmentService but for V2 DTOs with JSON2 structure.
 * 
 * Excel tables contain codes, this service looks up the descriptive names from
 * the same repositories used by BALU mapping.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelAffiliateCompanyV2EnrichmentService {

    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final FundPensionRepository fundPensionRepository;
    private final HealthPromotingEntityRepository healthRepository;
    private final ArlRepository arlRepository;
    private final OccupationRepository occupationRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    /**
     * Enriches Excel affiliate company V2 records with descriptive names.
     * This follows the same pattern as ExcelAffiliateCompanyEnrichmentService
     * but handles V2 DTO fields (idCargo/cargo instead of idOcupacion/ocupacion, etc.).
     * 
     * @param records List of V2 records to enrich (modified in place)
     */
    public void enrichDescriptions(List<AffiliateCompanyV2ResponseDTO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        log.debug("[ExcelAffiliateCompanyV2Enrichment] Enriching {} records", records.size());

        for (AffiliateCompanyV2ResponseDTO record : records) {
            try {
                enrichSingleRecord(record);
            } catch (Exception e) {
                log.warn("[ExcelAffiliateCompanyV2Enrichment] Error enriching record for person {}: {}", 
                    record.getIdPersona(), e.getMessage());
            }
        }

        log.debug("[ExcelAffiliateCompanyV2Enrichment] Enrichment completed");
    }

    private void enrichSingleRecord(AffiliateCompanyV2ResponseDTO record) {
        // Enrich person location
        enrichPersonLocation(record);
        
        // Enrich company location
        enrichCompanyLocation(record);
        
        // Enrich occupation (V2 uses idCargo/cargo)
        enrichOccupation(record);
        
        // Enrich financial entities
        enrichFinancialEntities(record);
        
        // Enrich economic activity
        enrichEconomicActivity(record);
    }

    private void enrichPersonLocation(AffiliateCompanyV2ResponseDTO record) {
        // Enrich person department
        if (record.getIdDepartamento() != null && record.getDepartamento() == null) {
            departmentRepository.findById(record.getIdDepartamento().longValue())
                .ifPresent(dept -> record.setDepartamento(dept.getDepartmentName()));
        }

        // Enrich person municipality
        if (record.getIdMunicipio() != null && record.getMunicipio() == null) {
            municipalityRepository.findById(record.getIdMunicipio().longValue())
                .ifPresent(muni -> record.setMunicipio(muni.getMunicipalityName()));
        }
    }

    private void enrichCompanyLocation(AffiliateCompanyV2ResponseDTO record) {
        // Enrich company department
        if (record.getIdDepartamentoEmp() != null && record.getDepartamentoEmp() == null) {
            departmentRepository.findById(record.getIdDepartamentoEmp().longValue())
                .ifPresent(dept -> record.setDepartamentoEmp(dept.getDepartmentName()));
        }

        // Enrich company municipality
        if (record.getIdMunicipioEmp() != null && record.getMunicipioEmp() == null) {
            municipalityRepository.findById(record.getIdMunicipioEmp().longValue())
                .ifPresent(muni -> record.setMunicipioEmp(muni.getMunicipalityName()));
        }
    }

    private void enrichOccupation(AffiliateCompanyV2ResponseDTO record) {
        // V2 uses idCargo/cargo instead of idOcupacion/ocupacion
        if (record.getIdCargo() != null && record.getCargo() == null) {
            occupationRepository.findById(record.getIdCargo().longValue())
                .ifPresent(occupation -> record.setCargo(occupation.getNameOccupation()));
        } else if (record.getCargo() != null && record.getIdCargo() == null) {
            occupationRepository.findByNameOccupationIgnoreCase(record.getCargo())
                .ifPresent(occupation -> record.setIdCargo(occupation.getIdOccupation().intValue()));
        }
    }

    private void enrichFinancialEntities(AffiliateCompanyV2ResponseDTO record) {
        // Enrich pension fund
        if (record.getAfp() != null && record.getNombreAfp() == null) {
            fundPensionRepository.findById(record.getAfp().longValue())
                .ifPresent(fund -> record.setNombreAfp(fund.getNameAfp()));
        }

        // Enrich health entity
        if (record.getEps() != null && record.getNombreEps() == null) {
            healthRepository.findByCodeEPS(record.getEps())
                .ifPresent(health -> record.setNombreEps(health.getNameEPS()));
        }

        // Enrich ARL
        if (record.getIdArl() != null && record.getNombreArl() == null) {
            arlRepository.findByCodeARL(record.getIdArl())
                .ifPresent(arl -> record.setNombreArl(arl.getAdministrator()));
        }
    }

    private void enrichEconomicActivity(AffiliateCompanyV2ResponseDTO record) {
        if (record.getIdActEconomica() != null && record.getNomActEco() == null) {
            economicActivityRepository.findByEconomicActivityCode(record.getIdActEconomica().toString())
                .stream()
                .findFirst()
                .ifPresent(activity -> {
                    record.setNomActEco(activity.getDescription());
                    
                    // Also set economic sector if available
                    if (activity.getClassRisk() != null && record.getIdSectorEconomico() == null) {
                        try {
                            record.setIdSectorEconomico(Integer.valueOf(activity.getClassRisk()));
                        } catch (NumberFormatException e) {
                            log.debug("Invalid class risk for economic activity {}: {}", 
                                record.getIdActEconomica(), activity.getClassRisk());
                        }
                    }
                });
        }
    }
}
