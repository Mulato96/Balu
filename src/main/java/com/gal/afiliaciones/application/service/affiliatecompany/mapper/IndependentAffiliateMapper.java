package com.gal.afiliaciones.application.service.affiliatecompany.mapper;

import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for independent affiliate data.
 * Handles mapping of Affiliation and Affiliate to response DTO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndependentAffiliateMapper {

    private final AffiliateCompanyMapper commonMapper;
    private final AffiliateDataService dataService;

    /**
     * Maps independent affiliate data to response DTO.
     * 
     * @param affiliate The affiliate data (can be null)
     * @param independent The affiliation data
     * @return Mapped response DTO
     */
    public AffiliateCompanyDbApproxResponseDTO map(Affiliate affiliate, Affiliation independent) {
        var builder = AffiliateCompanyDbApproxResponseDTO.builder();

        // Personal data mapping
        mapPersonalData(builder, independent);
        
        // Location and occupation data
        commonMapper.mapLocationData(builder, independent.getDepartment(), independent.getCityMunicipality());
        commonMapper.mapOccupationData(builder, null, independent.getOccupation());
        
        // Financial entities
        commonMapper.mapFinancialEntities(builder,
            independent.getPensionFundAdministrator(),
            independent.getHealthPromotingEntity(),
            independent.getCurrentARL());
        
        // Economic activity
        commonMapper.mapEconomicActivity(builder, independent.getCodeMainEconomicActivity());
        
        // Salary mapping
        builder.salario(commonMapper.mapSalary(independent.getContractMonthlyValue()));
        
        // Independent-specific data
        builder.idSucursal(1)
               .nomVinLaboral("Independiente")
               .idTipoVinculado(0); // Hardcoded for independents per integration logic

        // Zone indicator from affiliation_detail
        mapZoneIndicator(builder, independent);
        
        // Legal representative data
        builder.tpDocEmpresa(independent.getIdentificationDocumentTypeLegalRepresentative());

        // Independent contract dates (primary source)
        mapIndependentContractDates(builder, independent);
        
        // Affiliate dates if available (for additional coverage date)
        if (affiliate != null) {
            mapAffiliateDates(builder, affiliate);
        }

        // Contractor/mercantile data
        mapContractorData(builder, independent);

        return builder.build();
    }

    /**
     * Maps personal data from independent affiliation to builder.
     */
    private void mapPersonalData(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                               Affiliation independent) {
        builder.tipoDoc(independent.getIdentificationDocumentType())
               .idPersona(independent.getIdentificationDocumentNumber())
               .nombre1(independent.getFirstName())
               .nombre2(independent.getSecondName())
               .apellido1(independent.getSurname())
               .apellido2(independent.getSecondSurname())
               .sexo(independent.getGender())
               .emailPersona(independent.getEmail())
               .direccion(independent.getAddress())
               .telefonoPersona(independent.getPhone1())
               .fechaNacimiento(commonMapper.formatDate(independent.getDateOfBirth()));
    }

    /**
     * Maps zone indicator (R=Rural, U=Urban) from affiliation data.
     */
    private void mapZoneIndicator(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                 Affiliation independent) {
        if (independent.getIsRuralZone() != null) {
            String zoneIndicator = Boolean.TRUE.equals(independent.getIsRuralZone()) ? "R" : "U";
            builder.indZona(zoneIndicator);
        }
    }

    /**
     * Maps independent contract dates from affiliation data.
     * This matches the EmployerEmployeeQueryServiceImpl pattern.
     */
    private void mapIndependentContractDates(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                           Affiliation independent) {
        builder.fechaInicioVinculacion(commonMapper.formatDate(independent.getContractStartDate()))
               .fechaFinVinculacion(commonMapper.formatDate(independent.getContractEndDate()));
    }

    /**
     * Maps affiliate dates if affiliate data is available.
     */
    private void mapAffiliateDates(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                  Affiliate affiliate) {
        builder.fechaAfiliacionEfectiva(commonMapper.formatDate(affiliate.getCoverageStartDate()));
    }

    /**
     * Maps contractor/mercantile data from affiliation.
     */
    private void mapContractorData(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                  Affiliation independent) {
        // Find affiliate_mercantile by contractor document
        if (independent.getIdentificationDocumentTypeContractor() != null && 
            independent.getIdentificationDocumentNumberContractor() != null) {
            
            dataService.findAffiliateMercantile(
                independent.getIdentificationDocumentTypeContractor(),
                independent.getIdentificationDocumentNumberContractor()
            ).ifPresent(affiliateMercantile -> {
                // Set company data from affiliate_mercantile
                builder.idEmpresa(affiliateMercantile.getNumberIdentification())
                       .razonSocial(affiliateMercantile.getBusinessName())
                       .estadoRl(commonMapper.mapAffiliateStatusToRaw(affiliateMercantile.getAffiliationStatus()))
                       .estadoEmpresa(commonMapper.mapAffiliateStatusToRaw(affiliateMercantile.getAffiliationStatus()));

                // Map contractor affiliate data if available
                mapContractorAffiliateData(builder, affiliateMercantile);
            });
        }
    }

    /**
     * Maps contractor affiliate data (main office and work center).
     */
    private void mapContractorAffiliateData(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                           com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile affiliateMercantile) {
        if (affiliateMercantile.getFiledNumber() == null) return;

        dataService.findAffiliateByFiledNumber(affiliateMercantile.getFiledNumber())
            .ifPresent(contractorAffiliate -> {
                if (contractorAffiliate.getIdAffiliate() != null) {
                    // Map main office data
                    mapContractorMainOffice(builder, contractorAffiliate.getIdAffiliate());
                    
                    // Map work center data
                    mapContractorWorkCenter(builder, contractorAffiliate.getIdAffiliate());
                }
            });
    }

    /**
     * Maps contractor main office data.
     */
    private void mapContractorMainOffice(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                        Long contractorAffiliateId) {
        dataService.findMainOfficeByAffiliate(contractorAffiliateId)
            .ifPresent(mainOffice -> {
                builder.direccionEmpresa(mainOffice.getAddress())
                       .telefonoEmpresa(mainOffice.getMainOfficePhoneNumber())
                       .emailEmpresa(mainOffice.getMainOfficeEmail());
                
                // Map company location
                if (mainOffice.getIdDepartment() != null) {
                    builder.idDepartamentoEmp(mainOffice.getIdDepartment().intValue());
                    dataService.findDepartment(mainOffice.getIdDepartment())
                        .ifPresent(department -> builder.departamentoEmp(department.getDepartmentName()));
                }
                
                if (mainOffice.getIdCity() != null) {
                    dataService.findMunicipality(mainOffice.getIdCity())
                        .ifPresent(municipality -> {
                            builder.idMunicipioEmp(commonMapper.parseIntSafely(
                                municipality.getMunicipalityCode(), 
                                mainOffice.getIdCity().intValue()));
                            builder.municipioEmp(municipality.getMunicipalityName());
                        });
                }
            });
    }

    /**
     * Maps contractor work center data.
     */
    private void mapContractorWorkCenter(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder,
                                        Long contractorAffiliateId) {
        dataService.findWorkCentersByManager(contractorAffiliateId).stream()
            .findFirst()
            .ifPresent(workCenter -> builder.idCentroTrabajo(workCenter.getId().intValue()));
    }
}
