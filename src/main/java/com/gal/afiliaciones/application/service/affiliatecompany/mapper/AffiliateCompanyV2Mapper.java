package com.gal.afiliaciones.application.service.affiliatecompany.mapper;

import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO.AffiliateCompanyV2ResponseDTOBuilder;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Common V2 mapper for affiliate company data.
 * Contains shared mapping logic used by both dependent and independent affiliate V2 mappers.
 * Follows the same pattern as AffiliateCompanyMapper but maps to V2 DTO structure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AffiliateCompanyV2Mapper {

    private final AffiliateDataService dataService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATUS_ACTIVE = "Activo";
    private static final String STATUS_INACTIVE = "Inactivo";
    
    // Domestic economic activities for idTipoVinculado conversion
    private static final List<String> ECONOMIC_ACTIVITIES_DOMESTIC = Arrays.asList(
        Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC,  // "1970001"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_2,          // "3869201"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_3,          // "1970002"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_4           // "3970001"
    );

    /**
     * Maps location data (department and municipality) to the V2 builder.
     */
    public void mapLocationData(AffiliateCompanyV2ResponseDTOBuilder builder, Long departmentId, Long cityId) {
        mapDepartment(builder, departmentId);
        mapMunicipality(builder, cityId);
    }

    /**
     * Maps occupation data to the V2 builder (idCargo/cargo instead of idOcupacion/ocupacion).
     */
    public void mapOccupationData(AffiliateCompanyV2ResponseDTOBuilder builder, Long occupationId, String occupationName) {
        if (occupationId != null) {
            builder.idCargo(occupationId.intValue()); // V2 uses idCargo instead of idOcupacion
            dataService.findOccupation(occupationId)
                .ifPresent(occupation -> builder.cargo(occupation.getNameOccupation())); // V2 uses cargo instead of ocupacion
        } else if (occupationName != null) {
            builder.cargo(occupationName);
            dataService.findOccupationByName(occupationName)
                .ifPresent(occupation -> builder.idCargo(occupation.getIdOccupation().intValue()));
        }
    }

    /**
     * Maps financial entities (pension fund, health entity, ARL) to the V2 builder.
     */
    public void mapFinancialEntities(AffiliateCompanyV2ResponseDTOBuilder builder, 
                                   Long pensionFundId, Long healthEntityId, String arlCode) {
        mapPensionFund(builder, pensionFundId);
        mapHealthEntity(builder, healthEntityId);
        mapArl(builder, arlCode);
    }

    /**
     * Maps economic activity data to the V2 builder.
     */
    public void mapEconomicActivity(AffiliateCompanyV2ResponseDTOBuilder builder, String economicActivityCode) {
        if (economicActivityCode == null) return;

        try {
            builder.idActEconomica(Long.valueOf(economicActivityCode));
            dataService.findEconomicActivity(economicActivityCode)
                .ifPresent(activity -> {
                    builder.nomActEco(activity.getDescription());
                    mapEconomicSector(builder, activity.getClassRisk());
                });
        } catch (NumberFormatException e) {
            log.debug("Invalid economic activity code: {}", economicActivityCode);
        }
    }

    /**
     * Maps affiliate basic data (dates, company info) to the V2 builder.
     */
    public void mapAffiliateBasicData(AffiliateCompanyV2ResponseDTOBuilder builder, Affiliate affiliate) {
        mapAffiliateDates(builder, affiliate);
        mapCompanyData(builder, affiliate);
    }

    /**
     * Maps main office data to the V2 builder.
     */
    public void mapMainOfficeData(AffiliateCompanyV2ResponseDTOBuilder builder, Long affiliateId) {
        if (affiliateId == null) return;

        dataService.findMainOfficeByAffiliate(affiliateId)
            .ifPresent(mainOffice -> {
                builder.direccionEmpresa(mainOffice.getAddress())
                       .telefonoEmpresa(mainOffice.getMainOfficePhoneNumber())
                       .emailEmpresa(mainOffice.getMainOfficeEmail());

                mapZone(builder, mainOffice.getMainOfficeZone());
                mapWorkCenter(builder, mainOffice.getOfficeManager() != null ? 
                    mainOffice.getOfficeManager().getId() : null);
                mapCompanyLocation(builder, mainOffice.getIdDepartment(), mainOffice.getIdCity());
            });
    }

    /**
     * Converts bonding type to idTipoVinculado using integration logic.
     */
    public Integer convertTipoVinculadoDependent(Long idBondingType, String economicActivity) {
        if (idBondingType == null) return null;
        
        if (idBondingType.equals(1L) && ECONOMIC_ACTIVITIES_DOMESTIC.contains(economicActivity)) {
            return 1; // Domestic service worker
        }
        
        return switch (idBondingType.intValue()) {
            case 2 -> 34; // Student in practice
            case 3 -> 35; // SENA apprentice
            case 4 -> 0;  // Other
            default -> 3; // Regular dependent worker
        };
    }

    /**
     * Maps affiliate status to raw format.
     */
    public String mapAffiliateStatusToRaw(String status) {
        if (status == null) return null;
        return switch (status) {
            case "Activa" -> STATUS_ACTIVE;
            case "Inactiva" -> STATUS_INACTIVE;
            default -> status;
        };
    }

    /**
     * Maps dependent status for V2 (estado field).
     */
    public String mapDependentStatus(AffiliationDependent dependent) {
        // V2 uses "estado" instead of "estadoRl"
        // Logic can be based on dependent status or other fields
        // AffiliationDependent doesn't have getAffiliationStatus(), use other logic
        // Could check dependent dates or other criteria if needed
        log.debug("Mapping dependent status for person: {}", dependent.getIdentificationDocumentNumber());
        return STATUS_ACTIVE; // Default value
    }

    /**
     * Maps independent status for V2 (estado field).
     */
    public String mapIndependentStatus(Affiliation independent, Affiliate affiliate) {
        // V2 uses "estado" instead of "estadoRl"
        if (affiliate != null && affiliate.getAffiliationStatus() != null) {
            return mapAffiliateStatusToRaw(affiliate.getAffiliationStatus());
        }
        // Logic based on contract dates
        if (independent.getContractEndDate() != null && 
            independent.getContractEndDate().isBefore(LocalDate.now())) {
            return STATUS_INACTIVE;
        }
        return STATUS_ACTIVE; // Default value
    }

    /**
     * Maps contract status for V2 specific field.
     */
    public String mapContractStatus(Affiliation independent) {
        // V2 specific field - may be null for most cases
        if (independent.getContractEndDate() != null && 
            independent.getContractEndDate().isBefore(LocalDate.now())) {
            return "Vencido";
        } else if (independent.getContractStartDate() != null && 
                   independent.getContractStartDate().isAfter(LocalDate.now())) {
            return "Futuro";
        }
        return null; // Most common case
    }

    /**
     * Maps occupation code for V2 specific field.
     */
    public Integer mapOccupationCode(String occupationName) {
        // V2 specific field - may be null if not available
        if (occupationName != null) {
            return dataService.findOccupationByName(occupationName)
                .map(occupation -> occupation.getIdOccupation().intValue())
                .orElse(null);
        }
        return null;
    }

    /**
     * Safely converts salary to double.
     */
    public Double mapSalary(Object salary) {
        if (salary == null) return null;
        try {
            if (salary instanceof Number number) {
                return number.doubleValue();
            }
            return Double.valueOf(salary.toString());
        } catch (NumberFormatException e) {
            log.debug("Error converting salary: {}", salary, e);
            return null;
        }
    }

    /**
     * Safely parses integer with fallback.
     */
    public Integer parseIntSafely(String value, Integer fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Formats date to string.
     */
    public String formatDate(LocalDate date) {
        return date != null ? date.atStartOfDay().format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Formats datetime to string.
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    // Private helper methods
    private void mapDepartment(AffiliateCompanyV2ResponseDTOBuilder builder, Long departmentId) {
        Optional.ofNullable(departmentId)
            .ifPresent(id -> {
                builder.idDepartamento(id.intValue());
                dataService.findDepartment(id)
                    .ifPresent(department -> builder.departamento(department.getDepartmentName()));
            });
    }

    private void mapMunicipality(AffiliateCompanyV2ResponseDTOBuilder builder, Long cityId) {
        Optional.ofNullable(cityId)
            .ifPresent(id -> dataService.findMunicipality(id)
                .ifPresent(municipality -> {
                    builder.idMunicipio(parseIntSafely(municipality.getMunicipalityCode(), id.intValue()));
                    builder.municipio(municipality.getMunicipalityName());
                }));
    }

    private void mapPensionFund(AffiliateCompanyV2ResponseDTOBuilder builder, Long pensionFundId) {
        Optional.ofNullable(pensionFundId)
            .ifPresent(id -> {
                builder.afp(id.intValue());
                dataService.findPensionFund(id)
                    .ifPresent(fund -> builder.nombreAfp(fund.getNameAfp()));
            });
    }

    private void mapHealthEntity(AffiliateCompanyV2ResponseDTOBuilder builder, Long healthEntityId) {
        Optional.ofNullable(healthEntityId)
            .ifPresent(id -> dataService.findHealthEntity(id)
                .ifPresent(health -> {
                    builder.eps(health.getCodeEPS());
                    builder.nombreEps(health.getNameEPS());
                }));
    }

    private void mapArl(AffiliateCompanyV2ResponseDTOBuilder builder, String arlCode) {
        Optional.ofNullable(arlCode)
            .ifPresent(code -> {
                builder.idArl(code);
                dataService.findArlByCode(code)
                    .ifPresent(arl -> builder.nombreArl(arl.getAdministrator()));
            });
    }

    private void mapEconomicSector(AffiliateCompanyV2ResponseDTOBuilder builder, String classRisk) {
        if (classRisk != null) {
            try {
                builder.idSectorEconomico(Integer.valueOf(classRisk));
            } catch (NumberFormatException e) {
                log.debug("Invalid class risk: {}", classRisk);
            }
        }
    }

    private void mapAffiliateDates(AffiliateCompanyV2ResponseDTOBuilder builder, Affiliate affiliate) {
        builder.fechaInicioVinculacion(formatDate(affiliate.getCoverageStartDate()))
               .fechaFinVinculacion(formatDate(affiliate.getRetirementDate()))
               .fechaAfiliacionEfectiva(formatDate(affiliate.getCoverageStartDate()));
    }

    private void mapCompanyData(AffiliateCompanyV2ResponseDTOBuilder builder, Affiliate affiliate) {
        builder.tpDocEmpresa(affiliate.getDocumenTypeCompany())
               .idEmpresa(affiliate.getNitCompany())
               .razonSocial(affiliate.getCompany());
    }

    private void mapZone(AffiliateCompanyV2ResponseDTOBuilder builder, String zone) {
        Optional.ofNullable(zone).ifPresent(builder::indZona);
    }

    private void mapWorkCenter(AffiliateCompanyV2ResponseDTOBuilder builder, Long managerId) {
        if (managerId != null) {
            dataService.findWorkCentersByManager(managerId).stream()
                .findFirst()
                .ifPresent(workCenter -> builder.idCentroTrabajo(workCenter.getId().intValue()));
        }
    }

    private void mapCompanyLocation(AffiliateCompanyV2ResponseDTOBuilder builder, Long departmentId, Long cityId) {
        Optional.ofNullable(departmentId)
            .ifPresent(id -> {
                builder.idDepartamentoEmp(id.intValue());
                dataService.findDepartment(id)
                    .ifPresent(department -> builder.departamentoEmp(department.getDepartmentName()));
            });

        Optional.ofNullable(cityId)
            .ifPresent(id -> dataService.findMunicipality(id)
                .ifPresent(municipality -> {
                    builder.idMunicipioEmp(parseIntSafely(municipality.getMunicipalityCode(), id.intValue()));
                    builder.municipioEmp(municipality.getMunicipalityName());
                }));
    }
}
