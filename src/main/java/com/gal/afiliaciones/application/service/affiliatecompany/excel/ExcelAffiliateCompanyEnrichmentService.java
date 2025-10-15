package com.gal.afiliaciones.application.service.affiliatecompany.excel;

import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for enriching Excel affiliate company data with descriptive names.
 * Similar to EmployerEmployeeQueryServiceImpl.enrichExcelDescriptions() but comprehensive.
 * 
 * Excel tables contain codes, this service looks up the descriptive names from
 * the same repositories used by BALU mapping.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelAffiliateCompanyEnrichmentService {

    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final FundPensionRepository fundPensionRepository;
    private final HealthPromotingEntityRepository healthRepository;
    private final ArlRepository arlRepository;
    private final OccupationRepository occupationRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    /**
     * Enriches Excel affiliate company records with descriptive names.
     * This is similar to EmployerEmployeeQueryServiceImpl.enrichExcelDescriptions()
     * but handles all the fields needed for AffiliateCompanyDbApproxResponseDTO.
     * 
     * @param records List of records to enrich (modified in place)
     */
    public void enrichDescriptions(List<AffiliateCompanyDbApproxResponseDTO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        
        log.debug("[ExcelEnrichment] Enriching {} Excel records with descriptive names", records.size());
        
        for (AffiliateCompanyDbApproxResponseDTO dto : records) {
            try {
                enrichLocationData(dto);
                enrichFinancialEntityData(dto);
                enrichOccupationData(dto);
                enrichEconomicActivityData(dto);
                enrichCompanyData(dto);
            } catch (Exception ex) {
                log.debug("[ExcelEnrichment] Enrichment skipped for record {}: {}", dto.getIdPersona(), ex.getMessage());
            }
        }
        
        log.debug("[ExcelEnrichment] Completed enrichment for {} records", records.size());
    }

    /**
     * Enriches location data (department/municipality names).
     */
    private void enrichLocationData(AffiliateCompanyDbApproxResponseDTO dto) {
        // Personal location (residence)
        if (dto.getIdDepartamento() != null && dto.getDepartamento() == null) {
            departmentRepository.findById(dto.getIdDepartamento().longValue())
                .ifPresent(dept -> dto.setDepartamento(dept.getDepartmentName()));
        }
        
        if (dto.getIdMunicipio() != null && dto.getMunicipio() == null) {
            municipalityRepository.findById(dto.getIdMunicipio().longValue())
                .ifPresent(mun -> dto.setMunicipio(mun.getMunicipalityName()));
        }
        
        // Company location (work)
        if (dto.getIdDepartamentoEmp() != null && dto.getDepartamentoEmp() == null) {
            departmentRepository.findById(dto.getIdDepartamentoEmp().longValue())
                .ifPresent(dept -> dto.setDepartamentoEmp(dept.getDepartmentName()));
        }
        
        if (dto.getIdMunicipioEmp() != null && dto.getMunicipioEmp() == null) {
            municipalityRepository.findById(dto.getIdMunicipioEmp().longValue())
                .ifPresent(mun -> dto.setMunicipioEmp(mun.getMunicipalityName()));
        }
    }

    /**
     * Enriches financial entity data (AFP, EPS, ARL names).
     */
    private void enrichFinancialEntityData(AffiliateCompanyDbApproxResponseDTO dto) {
        // AFP (Pension Fund)
        if (dto.getAfp() != null && dto.getNombreAfp() == null) {
            fundPensionRepository.findById(dto.getAfp().longValue())
                .ifPresent(afp -> dto.setNombreAfp(afp.getNameAfp()));
        }
        
        // EPS (Health Entity)
        if (dto.getEps() != null && !dto.getEps().isBlank() && dto.getNombreEps() == null) {
            healthRepository.findByCodeEPS(dto.getEps())
                .ifPresent(eps -> dto.setNombreEps(eps.getNameEPS()));
        }
        
        // ARL (Risk Administrator) - Excel stores ARL code as string
        if (dto.getIdArl() != null && !dto.getIdArl().isBlank() && dto.getNombreArl() == null) {
            arlRepository.findByCodeARL(dto.getIdArl())
                .ifPresent(arl -> dto.setNombreArl(arl.getAdministrator()));
        }
    }

    /**
     * Enriches occupation data.
     * Excel sometimes has occupation name, sometimes code - handle both cases.
     */
    private void enrichOccupationData(AffiliateCompanyDbApproxResponseDTO dto) {
        // Case 1: Has occupation ID, needs occupation name
        if (dto.getIdOcupacion() != null && dto.getOcupacion() == null) {
            occupationRepository.findById(dto.getIdOcupacion().longValue())
                .ifPresent(occ -> dto.setOcupacion(occ.getNameOccupation()));
        }
        
        // Case 2: Has occupation name, needs occupation ID (reverse lookup)
        if (dto.getOcupacion() != null && dto.getIdOcupacion() == null) {
            // Try to parse as numeric code first (Excel sometimes stores codes as strings)
            try {
                Integer occupationId = Integer.valueOf(dto.getOcupacion());
                dto.setIdOcupacion(occupationId);
                // Also get the actual name
                occupationRepository.findById(occupationId.longValue())
                    .ifPresent(occ -> dto.setOcupacion(occ.getNameOccupation()));
            } catch (NumberFormatException e) {
                // It's actually a name, do reverse lookup for ID
                occupationRepository.findAll().stream()
                    .filter(occ -> dto.getOcupacion().equalsIgnoreCase(occ.getNameOccupation()))
                    .findFirst()
                    .ifPresent(occ -> dto.setIdOcupacion(occ.getIdOccupation().intValue()));
            }
        }
    }

    /**
     * Enriches economic activity data (name and sector).
     */
    private void enrichEconomicActivityData(AffiliateCompanyDbApproxResponseDTO dto) {
        if (dto.getIdActEconomica() != null && dto.getNomActEco() == null) {
            String activityCode = String.valueOf(dto.getIdActEconomica());
            economicActivityRepository.findByEconomicActivityCode(activityCode)
                .stream().findFirst()
                .ifPresent(activity -> {
                    dto.setNomActEco(activity.getDescription());
                    
                    // Also set economic sector from class risk
                    if (activity.getClassRisk() != null && dto.getIdSectorEconomico() == null) {
                        try {
                            dto.setIdSectorEconomico(Integer.valueOf(activity.getClassRisk()));
                        } catch (NumberFormatException e) {
                            log.debug("[ExcelEnrichment] Invalid class risk format: {}", activity.getClassRisk());
                        }
                    }
                });
        }
    }

    /**
     * Enriches company data using AffiliateMercantile lookup.
     * This handles the complex case where Excel has contractor document but no company name.
     */
    private void enrichCompanyData(AffiliateCompanyDbApproxResponseDTO dto) {
        if (dto.getIdEmpresa() != null && !dto.getIdEmpresa().isBlank() 
            && dto.getTpDocEmpresa() != null && dto.getRazonSocial() == null) {
            
            // Look up company info by contractor document type and number
            affiliateMercantileRepository.findByTypeDocumentIdentificationAndNumberIdentification(
                dto.getTpDocEmpresa(), dto.getIdEmpresa())
                .ifPresent(mercantile -> {
                    dto.setRazonSocial(mercantile.getBusinessName());
                    
                    // Set company status if available
                    if (dto.getEstadoEmpresa() == null) {
                        dto.setEstadoEmpresa(mapAffiliateStatusToRaw(mercantile.getAffiliationStatus()));
                    }
                    if (dto.getEstadoRl() == null) {
                        dto.setEstadoRl(mapAffiliateStatusToRaw(mercantile.getAffiliationStatus()));
                    }
                });
        }
    }

    /**
     * Maps affiliate status to raw format (same logic as common mapper).
     */
    private String mapAffiliateStatusToRaw(String status) {
        if (status == null) return null;
        return switch (status) {
            case "Activa" -> "Activo";
            case "Inactiva" -> "Inactivo";
            default -> status;
        };
    }
}
