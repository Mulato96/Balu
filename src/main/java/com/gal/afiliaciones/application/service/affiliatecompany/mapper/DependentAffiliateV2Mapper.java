package com.gal.afiliaciones.application.service.affiliatecompany.mapper;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * V2 Mapper for dependent affiliate data.
 * Handles mapping of AffiliationDependent and employer Affiliate to V2 response DTO.
 * Follows the same pattern as DependentAffiliateMapper but maps to JSON2 structure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DependentAffiliateV2Mapper {

    private final AffiliateCompanyV2Mapper commonMapper;

    /**
     * Maps dependent affiliate data to V2 response DTO.
     * 
     * @param employerAffiliate The employer affiliate (can be null)
     * @param dependent The affiliation dependent data
     * @return Mapped V2 response DTO
     */
    public AffiliateCompanyV2ResponseDTO map(Affiliate employerAffiliate, AffiliationDependent dependent) {
        var builder = AffiliateCompanyV2ResponseDTO.builder();

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
        
        // Dependent-specific data (V2 structure)
        builder.idSucursal(1)
               .nomVinLaboral("Dependiente")
               .idTipoVinculado(commonMapper.convertTipoVinculadoDependent(
                   dependent.getIdBondingType(), 
                   dependent.getEconomicActivityCode()))
               .tipoVinculado("TRABAJADORES DEPENDIENTES") // V2 specific field
               .idSede(1) // V2 specific field - default value
               .codigoSubempresa("0"); // V2 specific field - default value

        // Map V2 status field (estado instead of estadoRl)
        builder.estado(commonMapper.mapDependentStatus(dependent));

        // V2 specific contract fields (typically null for dependents)
        builder.fechaInicioContrato(null)
               .fechaFinContrato(null)
               .estadoContrato(null)
               .codigoOcupacion(null) // May be mapped from occupation if available
               .ocupacionVoluntario(null); // Typically null for dependents

        // Employer data if available
        if (employerAffiliate != null) {
            mapEmployerData(builder, employerAffiliate);
        }

        return builder.build();
    }

    /**
     * Maps personal data from dependent to V2 builder.
     */
    private void mapPersonalData(AffiliateCompanyV2ResponseDTO.AffiliateCompanyV2ResponseDTOBuilder builder, 
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
     * Maps employer affiliate data to V2 builder.
     */
    private void mapEmployerData(AffiliateCompanyV2ResponseDTO.AffiliateCompanyV2ResponseDTOBuilder builder, 
                               Affiliate employerAffiliate) {
        // Basic affiliate data
        commonMapper.mapAffiliateBasicData(builder, employerAffiliate);
        
        // Main office data
        commonMapper.mapMainOfficeData(builder, employerAffiliate.getIdAffiliate());
        
        // Company status
        builder.estadoEmpresa(commonMapper.mapAffiliateStatusToRaw(employerAffiliate.getAffiliationStatus()));
        
        // V2 specific employer fields
        builder.tpDocEmpresa(employerAffiliate.getDocumentType())
               .idTipoDocEmp(employerAffiliate.getDocumentType()); // Same as tpDocEmpresa in V2
    }
}
