package com.gal.afiliaciones.application.service.affiliatecompany.mapper;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for dependent affiliate data.
 * Handles mapping of AffiliationDependent and employer Affiliate to response DTO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DependentAffiliateMapper {

    private final AffiliateCompanyMapper commonMapper;

    /**
     * Maps dependent affiliate data to response DTO.
     * 
     * @param employerAffiliate The employer affiliate (can be null)
     * @param dependent The affiliation dependent data
     * @return Mapped response DTO
     */
    public AffiliateCompanyDbApproxResponseDTO map(Affiliate employerAffiliate, AffiliationDependent dependent) {
        var builder = AffiliateCompanyDbApproxResponseDTO.builder();

        // Personal data mapping
        mapPersonalData(builder, dependent);
        
        // Location and occupation data
        commonMapper.mapLocationData(builder, dependent.getIdDepartment(), dependent.getIdCity());
        commonMapper.mapOccupationData(builder, dependent.getIdOccupation(), null);
        
        // Financial entities
        commonMapper.mapFinancialEntities(builder, 
            dependent.getPensionFundAdministrator(),
            dependent.getHealthPromotingEntity(),
            dependent.getOccupationalRiskManager());
        
        // Economic activity
        commonMapper.mapEconomicActivity(builder, dependent.getEconomicActivityCode());
        
        // Salary mapping
        builder.salario(commonMapper.mapSalary(dependent.getSalary()));
        
        // Dependent-specific data
        builder.idSucursal(1)
               .nomVinLaboral("Dependiente")
               .idTipoVinculado(commonMapper.convertTipoVinculadoDependent(
                   dependent.getIdBondingType(), 
                   dependent.getEconomicActivityCode()));

        // Employer data if available
        if (employerAffiliate != null) {
            long t0 = System.currentTimeMillis();
            log.debug("[BALU][MAP] mapEmployerData start employerId={}", employerAffiliate.getIdAffiliate());
            mapEmployerData(builder, employerAffiliate);
            log.debug("[BALU][MAP] mapEmployerData done in {}ms", (System.currentTimeMillis() - t0));
        }

        return builder.build();
    }

    /**
     * Maps personal data from dependent to builder.
     */
    private void mapPersonalData(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder, 
                                AffiliationDependent dependent) {
        builder.tipoDoc(dependent.getIdentificationDocumentType())
               .idPersona(dependent.getIdentificationDocumentNumber())
               .nombre1(dependent.getFirstName())
               .nombre2(dependent.getSecondName())
               .apellido1(dependent.getSurname())
               .apellido2(dependent.getSecondSurname())
               .sexo(dependent.getGender())
               .telefonoPersona(dependent.getPhone1())
               .emailPersona(dependent.getEmail())
               .direccion(dependent.getAddress())
               .fechaNacimiento(commonMapper.formatDate(dependent.getDateOfBirth()));
    }

    /**
     * Maps employer affiliate data to builder.
     */
    private void mapEmployerData(AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder builder, 
                               Affiliate employerAffiliate) {
        // Basic affiliate data
        commonMapper.mapAffiliateBasicData(builder, employerAffiliate);
        
        // Main office data
        commonMapper.mapMainOfficeData(builder, employerAffiliate.getIdAffiliate());
        
        // Company status
        builder.estadoEmpresa(commonMapper.mapAffiliateStatusToRaw(employerAffiliate.getAffiliationStatus()));
    }
}
