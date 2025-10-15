package com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper;

import com.gal.afiliaciones.domain.model.ExcelDependentTmp;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Mapper for Excel dependent data to AffiliateCompanyDbApproxResponseDTO.
 * Maps from tmp_excel_dependientes table to the response DTO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelDependentMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Maps Excel dependent data to response DTO.
     * 
     * @param excel The Excel dependent record
     * @return Mapped response DTO
     */
    public AffiliateCompanyDbApproxResponseDTO map(ExcelDependentTmp excel) {
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
            .direccion(excel.getAddress())
            .fechaNacimiento(formatDate(excel.getDateOfBirth()))
            
            // Location data (from DANE codes - enrichment will populate names)
            .idDepartamento(parseIntSafely(excel.getResidenceDepartmentDaneCode()))
            .idMunicipio(parseIntSafely(excel.getResidenceMunicipalityDaneCode()))
            // departamento and municipio will be populated by enrichment service
            
            // Financial entities (codes only from Excel - enrichment will populate names)
            .eps(excel.getEpsCode())
            .afp(parseIntSafely(excel.getAfpCode()))
            // nombreEps and nombreAfp will be populated by enrichment service
            
            // Economic activity (enrichment will populate nomActEco and idSectorEconomico)
            .idActEconomica(parseIntSafely(excel.getEconomicActivityCode()) != null ? 
                Long.valueOf(parseIntSafely(excel.getEconomicActivityCode())) : null)
            
            // Occupation (enrichment will handle code to name mapping)
            .idOcupacion(parseIntSafely(excel.getOccupationCode()))
            
            // Salary
            .salario(excel.getSalaryIbc() != null ? excel.getSalaryIbc().doubleValue() : null)
            
            // Date fields - Excel dependent pattern
            .fechaInicioVinculacion(formatDate(excel.getCoverageStartDate()))
            .fechaAfiliacionEfectiva(formatDate(excel.getCoverageStartDate()))
            // fechaFinVinculacion not available in Excel dependents (null)
            
            // Company data (from employer fields - enrichment will populate razonSocial)
            .tpDocEmpresa(excel.getEmployerDocumentType())
            .idEmpresa(excel.getEmployerDocumentNumber())
            // razonSocial, estadoEmpresa, estadoRl will be populated by enrichment service
            
            // Work location (enrichment will populate departamentoEmp and municipioEmp)
            .idDepartamentoEmp(parseIntSafely(excel.getWorkDepartmentCode()))
            .idMunicipioEmp(parseIntSafely(excel.getWorkCityCode()))
            
            // Sub company
            .idSucursal(parseIntSafely(excel.getSubCompanyCode()) != null ? 
                parseIntSafely(excel.getSubCompanyCode()) : 1) // Default to 1
            
            // Type and labor relationship
            .nomVinLaboral("Dependiente")
            .idTipoVinculado(3) // Default for dependents (regular dependent worker)
            
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
