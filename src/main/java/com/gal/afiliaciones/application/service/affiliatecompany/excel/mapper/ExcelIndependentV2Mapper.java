package com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper;

import com.gal.afiliaciones.domain.model.ExcelIndependentTmp;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * V2 Mapper for Excel independent data to AffiliateCompanyV2ResponseDTO.
 * Maps from tmp_excel_independientes table to the V2 response DTO with JSON2 structure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelIndependentV2Mapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Maps Excel independent data to V2 response DTO.
     * 
     * @param excel The Excel independent record
     * @return Mapped V2 response DTO
     */
    public AffiliateCompanyV2ResponseDTO map(ExcelIndependentTmp excel) {
        return AffiliateCompanyV2ResponseDTO.builder()
            // Company data first (V2 structure)
            .tpDocEmpresa(excel.getContractorDocumentType())
            .idEmpresa(excel.getContractorDocumentNumber())
            .idTipoDocEmp(excel.getContractorDocumentType()) // Same as tpDocEmpresa
            
            // Personal data
            .tipoDoc(excel.getDocumentType())
            .idPersona(excel.getDocumentNumber())
            .nombre1(excel.getFirstName())
            .nombre2(excel.getSecondName())
            .apellido1(excel.getFirstSurname())
            .apellido2(excel.getSecondSurname())
            .sexo(mapGender(excel.getSex()))
            .telefonoPersona(excel.getPhone())
            .direccion(excel.getResidenceAddress()) // Correct field name
            .fechaNacimiento(formatDate(excel.getDateOfBirth()))
            
            // Location data (from DANE codes - enrichment will populate names)
            .idDepartamento(parseIntSafely(excel.getResidenceDepartmentDaneCode()))
            .idMunicipio(parseIntSafely(excel.getResidenceMunicipalityDaneCode()))
            // departamento and municipio will be populated by enrichment service
            
            // V2 occupation fields (idCargo/cargo instead of idOcupacion/ocupacion)
            .idCargo(parseIntSafely(excel.getOccupationCode()))
            .cargo(excel.getOccupationTitle()) // Correct field name
            
            // V2 status field (estado instead of estadoRl) - Excel doesn't have status, use default
            .estado("Activo") // Default since Excel doesn't have status field
            
            // Dates
            .fechaInicioVinculacion(formatDate(excel.getContractStartDate()))
            .fechaFinVinculacion(formatDate(excel.getContractEndDate()))
            .nomVinLaboral("Independiente")
            
            // Financial entities (codes only from Excel - enrichment will populate names)
            .eps(excel.getEpsCode())
            .afp(parseIntSafely(excel.getAfpCode()))
            // Excel doesn't have ARL code field, will be populated by enrichment if needed
            .idArl(null)
            // nombreEps, nombreAfp, nombreArl will be populated by enrichment service
            
            // Salary - use baseContributionIncome like V1 mapper
            .salario(excel.getBaseContributionIncome() != null ? 
                excel.getBaseContributionIncome().doubleValue() : null)
            
            // V2 specific fields - use subCompanyCode like V1 mapper
            .idSucursal(parseIntSafely(excel.getSubCompanyCode()) != null ? 
                parseIntSafely(excel.getSubCompanyCode()) : 1) // Default to 1
            .idSede(1) // Default value
            .codigoSubempresa("0") // Default value
            .idTipoVinculado(0) // Default for independents
            .tipoVinculado("TRABAJADOR INDEPENDIENTE")
            
            // Company data (contractor) - Excel doesn't have these fields, will be populated by enrichment
            .razonSocial(null) // Will be populated by enrichment via AffiliateMercantile lookup
            .direccionEmpresa(null)
            .telefonoEmpresa(null)
            .emailEmpresa(null)
            
            // Company location - use work location like V1 mapper
            .idDepartamentoEmp(parseIntSafely(excel.getWorkDepartmentCode()))
            .idMunicipioEmp(parseIntSafely(excel.getWorkCityCode()))
            // departamentoEmp and municipioEmp will be populated by enrichment service
            
            // Economic activity - use activityToExecuteCode like V1 mapper
            .idActEconomica(parseIntSafely(excel.getActivityToExecuteCode()) != null ? 
                Long.valueOf(parseIntSafely(excel.getActivityToExecuteCode())) : null)
            // nomActEco will be populated by enrichment service
            
            // Coverage dates
            .fechaAfiliacionEfectiva(formatDate(excel.getCoverageStartDate()))
            
            // Company status - Excel doesn't have this, will be populated by enrichment
            .estadoEmpresa(null)
            
            // Work center and sector - Excel doesn't have these exact fields
            .idCentroTrabajo(null) // Not available in Excel
            .idSectorEconomico(null) // Will be populated by enrichment from economic activity
            
            // Zone indicator - Excel doesn't have this field
            .indZona(null)
            
            // V2 specific contract fields (more relevant for independents)
            .fechaInicioContrato(formatDate(excel.getContractStartDate())) // Same as fechaInicioVinculacion
            .fechaFinContrato(formatDate(excel.getContractEndDate())) // Same as fechaFinVinculacion
            .estadoContrato(mapContractStatus(excel.getContractStartDate(), excel.getContractEndDate()))
            .codigoOcupacion(parseIntSafely(excel.getOccupationCode())) // May be same as idCargo
            .ocupacionVoluntario(mapVoluntaryOccupation(excel.getContractorDocumentNumber())) // Check if voluntary
            
            .build();
    }

    private String mapGender(String sex) {
        if (sex == null) return null;
        return switch (sex.toUpperCase()) {
            case "MASCULINO", "HOMBRE", "H" -> "M";
            case "FEMENINO", "MUJER", "F" -> "F";
            default -> sex;
        };
    }

    // Removed mapStatus method since Excel doesn't have status field

    private String mapContractStatus(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            return "Vencido";
        } else if (startDate != null && startDate.isAfter(LocalDate.now())) {
            return "Futuro";
        }
        return null; // Most common case
    }

    private String mapVoluntaryOccupation(String contractorDocumentNumber) {
        // Check if it's a voluntary affiliation (special contractor number)
        if ("899999998".equals(contractorDocumentNumber)) {
            return "AFILIACION VOLUNTARIA";
        }
        return null; // Most common case
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.atStartOfDay().format(DATE_TIME_FORMATTER) : null;
    }

    private Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("Error parsing integer: {}", value);
            return null;
        }
    }

    // Removed parseLongSafely method since we use parseIntSafely and convert to Long

    // Removed parseDoubleSafely method since we use BigDecimal directly
}
