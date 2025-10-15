package com.gal.afiliaciones.application.service.affiliatecompany.mapper;

import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder;
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
 * Common mapper for affiliate company data.
 * Contains shared mapping logic used by both dependent and independent affiliate mappers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AffiliateCompanyMapper {

    private final AffiliateDataService dataService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Domestic economic activities for idTipoVinculado conversion
    private static final List<String> ECONOMIC_ACTIVITIES_DOMESTIC = Arrays.asList(
        Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC,  // "1970001"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_2,          // "3869201"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_3,          // "1970002"
        Constant.ECONOMIC_ACTIVITY_DOMESTIC_4           // "3970001"
    );

    /**
     * Maps location data (department and municipality) to the builder.
     */
    public void mapLocationData(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long departmentId, Long cityId) {
        mapDepartment(builder, departmentId);
        mapMunicipality(builder, cityId);
    }

    /**
     * Maps occupation data to the builder.
     */
    public void mapOccupationData(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long occupationId, String occupationName) {
        if (occupationId != null) {
            builder.idOcupacion(occupationId.intValue());
            dataService.findOccupation(occupationId)
                .ifPresent(occupation -> builder.ocupacion(occupation.getNameOccupation()));
        } else if (occupationName != null) {
            builder.ocupacion(occupationName);
            dataService.findOccupationByName(occupationName)
                .ifPresent(occupation -> builder.idOcupacion(occupation.getIdOccupation().intValue()));
        }
    }

    /**
     * Maps financial entities (pension fund, health entity, ARL) to the builder.
     */
    public void mapFinancialEntities(AffiliateCompanyDbApproxResponseDTOBuilder builder, 
                                   Long pensionFundId, Long healthEntityId, String arlCode) {
        mapPensionFund(builder, pensionFundId);
        mapHealthEntity(builder, healthEntityId);
        mapArl(builder, arlCode);
    }

    /**
     * Maps economic activity data to the builder.
     */
    public void mapEconomicActivity(AffiliateCompanyDbApproxResponseDTOBuilder builder, String economicActivityCode) {
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
     * Maps affiliate basic data (dates, company info) to the builder.
     */
    public void mapAffiliateBasicData(AffiliateCompanyDbApproxResponseDTOBuilder builder, Affiliate affiliate) {
        mapAffiliateDates(builder, affiliate);
        mapCompanyData(builder, affiliate);
    }

    /**
     * Maps main office data to the builder.
     */
    public void mapMainOfficeData(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long affiliateId) {
        if (affiliateId == null) return;
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][MAP] mapMainOfficeData start affiliateId={}", affiliateId);

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

        log.debug("[BALU][MAP] mapMainOfficeData done in {}ms", (System.currentTimeMillis() - t0));
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
            case "Activa" -> "Activo";
            case "Inactiva" -> "Inactivo";
            default -> status;
        };
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
    private void mapDepartment(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long departmentId) {
        Optional.ofNullable(departmentId)
            .ifPresent(id -> {
                builder.idDepartamento(id.intValue());
                dataService.findDepartment(id)
                    .ifPresent(department -> builder.departamento(department.getDepartmentName()));
            });
    }

    private void mapMunicipality(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long cityId) {
        Optional.ofNullable(cityId)
            .ifPresent(id -> dataService.findMunicipality(id)
                .ifPresent(municipality -> {
                    builder.idMunicipio(parseIntSafely(municipality.getMunicipalityCode(), id.intValue()));
                    builder.municipio(municipality.getMunicipalityName());
                }));
    }

    private void mapPensionFund(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long pensionFundId) {
        Optional.ofNullable(pensionFundId)
            .ifPresent(id -> {
                builder.afp(id.intValue());
                dataService.findPensionFund(id)
                    .ifPresent(fund -> builder.nombreAfp(fund.getNameAfp()));
            });
    }

    private void mapHealthEntity(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long healthEntityId) {
        Optional.ofNullable(healthEntityId)
            .ifPresent(id -> dataService.findHealthEntity(id)
                .ifPresent(health -> {
                    builder.eps(health.getCodeEPS());
                    builder.nombreEps(health.getNameEPS());
                }));
    }

    private void mapArl(AffiliateCompanyDbApproxResponseDTOBuilder builder, String arlCode) {
        Optional.ofNullable(arlCode)
            .ifPresent(code -> {
                builder.idArl(code);
                dataService.findArlByCode(code)
                    .ifPresent(arl -> builder.nombreArl(arl.getAdministrator()));
            });
    }

    private void mapEconomicSector(AffiliateCompanyDbApproxResponseDTOBuilder builder, String classRisk) {
        if (classRisk != null) {
            try {
                builder.idSectorEconomico(Integer.valueOf(classRisk));
            } catch (NumberFormatException e) {
                log.debug("Invalid class risk: {}", classRisk);
            }
        }
    }

    private void mapAffiliateDates(AffiliateCompanyDbApproxResponseDTOBuilder builder, Affiliate affiliate) {
        builder.fechaInicioVinculacion(formatDate(affiliate.getCoverageStartDate()))
               .fechaFinVinculacion(formatDate(affiliate.getRetirementDate()))
               .fechaAfiliacionEfectiva(formatDate(affiliate.getCoverageStartDate()));
    }

    private void mapCompanyData(AffiliateCompanyDbApproxResponseDTOBuilder builder, Affiliate affiliate) {
        builder.tpDocEmpresa(affiliate.getDocumenTypeCompany())
               .idEmpresa(affiliate.getNitCompany())
               .razonSocial(affiliate.getCompany())
               .estadoRl(mapAffiliateStatusToRaw(affiliate.getAffiliationStatus()));
    }

    private void mapZone(AffiliateCompanyDbApproxResponseDTOBuilder builder, String zone) {
        Optional.ofNullable(zone).ifPresent(builder::indZona);
    }

    private void mapWorkCenter(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long managerId) {
        if (managerId != null) {
            long t0 = System.currentTimeMillis();
            log.debug("[BALU][MAP] mapWorkCenter start managerId={}", managerId);
            dataService.findWorkCentersByManager(managerId).stream()
                .findFirst()
                .ifPresent(workCenter -> builder.idCentroTrabajo(workCenter.getId().intValue()));
            log.debug("[BALU][MAP] mapWorkCenter done in {}ms", (System.currentTimeMillis() - t0));
        }
    }

    private void mapCompanyLocation(AffiliateCompanyDbApproxResponseDTOBuilder builder, Long departmentId, Long cityId) {
        log.debug("[BALU][MAP] mapCompanyLocation start deptId={} cityId={}", departmentId, cityId);
        long t0 = System.currentTimeMillis();

        Optional.ofNullable(departmentId)
            .ifPresent(id -> {
                builder.idDepartamentoEmp(id.intValue());
                long td = System.currentTimeMillis();
                dataService.findDepartment(id)
                    .ifPresent(department -> builder.departamentoEmp(department.getDepartmentName()));
                log.debug("[BALU][MAP] mapCompanyLocation department done in {}ms", (System.currentTimeMillis() - td));
            });

        Optional.ofNullable(cityId)
            .ifPresent(id -> dataService.findMunicipality(id)
                .ifPresent(municipality -> {
                    long tm = System.currentTimeMillis();
                    builder.idMunicipioEmp(parseIntSafely(municipality.getMunicipalityCode(), id.intValue()));
                    builder.municipioEmp(municipality.getMunicipalityName());
                    log.debug("[BALU][MAP] mapCompanyLocation municipality done in {}ms", (System.currentTimeMillis() - tm));
                }));

        log.debug("[BALU][MAP] mapCompanyLocation done in {}ms", (System.currentTimeMillis() - t0));
    }
}
