package com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper;

import com.gal.afiliaciones.domain.model.ExcelIndependentTmp;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Mapper for Excel independent data to AffiliateCompanyDbApproxResponseDTO.
 * Maps from tmp_excel_independientes table to the response DTO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelIndependentMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Maps Excel independent data to response DTO.
     * 
     * @param excel The Excel independent record
     * @return Mapped response DTO
     */
    public AffiliateCompanyDbApproxResponseDTO map(ExcelIndependentTmp excel) {
        return AffiliateCompanyDbApproxResponseDTO.builder()
            // Personal data
            .tipoDoc(excel.getDocumentType())
            .idPersona(excel.getDocumentNumber())
            .nombre1(excel.getFirstName())
            .nombre2(excel.getSecondName())
            .apellido1(excel.getFirstSurname())
            .apellido2(excel.getSecondSurname())
            .sexo(mapGender(excel.getSex()))
            .telefonoPersona(excel.getPhone())
            .emailPersona(excel.getEmail())
            .direccion(excel.getResidenceAddress())
            .fechaNacimiento(formatDate(excel.getDateOfBirth()))
            
            // Location data (from DANE codes - enrichment will populate names)
            .idDepartamento(parseIntSafely(excel.getResidenceDepartmentDaneCode()))
            .idMunicipio(parseIntSafely(excel.getResidenceMunicipalityDaneCode()))
            // departamento and municipio will be populated by enrichment service
            
            // Financial entities (codes only from Excel - enrichment will populate names)
            .eps(excel.getEpsCode())
            .afp(parseIntSafely(excel.getAfpCode()))
            // nombreEps and nombreAfp will be populated by enrichment service
            
            // Economic activity (from activity to execute - enrichment will populate nomActEco and idSectorEconomico)
            .idActEconomica(parseIntSafely(excel.getActivityToExecuteCode()) != null ? 
                Long.valueOf(parseIntSafely(excel.getActivityToExecuteCode())) : null)
            
            // Occupation (Excel can have title as name or code as string - enrichment will resolve)
            .idOcupacion(parseIntSafely(excel.getOccupationCode()))
            .ocupacion(excel.getOccupationTitle()) // May be actual name or code as string
            
            // Salary/Income
            .salario(excel.getBaseContributionIncome() != null ? 
                excel.getBaseContributionIncome().doubleValue() : null)
            
            // Date fields - Excel independent pattern (contract dates available)
            .fechaInicioVinculacion(formatDate(excel.getContractStartDate()))
            .fechaFinVinculacion(formatDate(excel.getContractEndDate()))
            .fechaAfiliacionEfectiva(formatDate(excel.getCoverageStartDate()))
            
            // Contractor/Company data (enrichment will populate razonSocial, estadoEmpresa, estadoRl)
            .tpDocEmpresa(excel.getContractorDocumentType())
            .idEmpresa(excel.getContractorDocumentNumber())
            // razonSocial, estadoEmpresa, estadoRl will be populated by enrichment service via AffiliateMercantile lookup
            
            // Work location (enrichment will populate departamentoEmp and municipioEmp)
            .idDepartamentoEmp(parseIntSafely(excel.getWorkDepartmentCode()))
            .idMunicipioEmp(parseIntSafely(excel.getWorkCityCode()))
            
            // Sub company
            .idSucursal(parseIntSafely(excel.getSubCompanyCode()) != null ? 
                parseIntSafely(excel.getSubCompanyCode()) : 1) // Default to 1
            
            // Zone indicator (not available in Excel - could add logic if needed)
            // .indZona() // Not available in Excel independent
            
            // Type and labor relationship
            .nomVinLaboral("Independiente")
            .idTipoVinculado(0) // Hardcoded for independents per integration logic
            
            .build();
    }

    /**
     * Maps gender from Excel format to expected format.
     */
    private String mapGender(String sex) {
        if (sex == null) return null;
        String s = sex.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (s) {
            case "M", "MASCULINO" -> "MASCULINO";
            case "F", "FEMENINO" -> "FEMENINO";
            default -> s;
        };
    }

    /**
     * Safely parses integer from string.
     */
    private Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.debug("Failed to parse integer: {}", value);
            return null;
        }
    }

    /**
     * Formats date to string using the standard format.
     */
    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.atStartOfDay().format(DATE_TIME_FORMATTER) : null;
    }
}
