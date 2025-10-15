package com.gal.afiliaciones.application.service.tmp.impl;

import com.gal.afiliaciones.application.service.tmp.ExcelPersonConsultationService;
import com.gal.afiliaciones.domain.model.ExcelDependentTmp;
import com.gal.afiliaciones.domain.model.ExcelIndependentTmp;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelDependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelIndependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpExcelPersonDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelPersonConsultationServiceImpl implements ExcelPersonConsultationService {

    private final ExcelDependentTmpRepository dependentRepository;
    private final ExcelIndependentTmpRepository independentRepository;

    private static final DateTimeFormatter DATE_OUT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String BALU_PRE = "BALU_PRE";

    @Override
    @Transactional(readOnly = true)
    public List<TmpExcelPersonDTO> consultPersonFromTmp(String documentType, String documentNumber) {
        String normalizedDocType = documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT);

        CompletableFuture<List<TmpExcelPersonDTO>> dependentsFuture = CompletableFuture.supplyAsync(() ->
                dependentRepository.findByDocumentTypeAndDocumentNumber(normalizedDocType, documentNumber)
                        .stream()
                        .map(this::mapDependent)
                        .toList()
        );

        CompletableFuture<List<TmpExcelPersonDTO>> independentsFuture = CompletableFuture.supplyAsync(() ->
                independentRepository.findByDocumentTypeAndDocumentNumber(normalizedDocType, documentNumber)
                        .stream()
                        .map(this::mapIndependent)
                        .toList()
        );

        List<TmpExcelPersonDTO> result = new ArrayList<>();
        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();
        result.addAll(dependentsFuture.join());
        result.addAll(independentsFuture.join());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TmpAffiliateStatusDTO> consultAffiliateStatus(
            String employerDocType, String employerDocNumber,
            String personDocType, String personDocNumber
    ) {
        String empType = employerDocType == null ? null : employerDocType.trim().toUpperCase(Locale.ROOT);
        String perType = personDocType == null ? null : personDocType.trim().toUpperCase(Locale.ROOT);

        CompletableFuture<List<TmpAffiliateStatusDTO>> dependentsFuture = CompletableFuture.supplyAsync(() ->
                dependentRepository.findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                                perType, personDocNumber, empType, employerDocNumber)
                        .stream()
                        .map(e -> TmpAffiliateStatusDTO.builder()
                                .idTipoDocEmp(e.getEmployerDocumentType())
                                .idEmpresa(e.getEmployerDocumentNumber())
                                .estadoEmpresa("ACTIVA")
                                .idTipoDocPer(e.getDocumentType())
                                .idPersona(e.getDocumentNumber())
                                .estadoPersona("ACTIVO")
                                .appSource(BALU_PRE)
                                .build())
                        .toList()
        );

        CompletableFuture<List<TmpAffiliateStatusDTO>> independentsFuture = CompletableFuture.supplyAsync(() ->
                independentRepository.findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                                perType, personDocNumber, empType, employerDocNumber)
                        .stream()
                        .map(e -> TmpAffiliateStatusDTO.builder()
                                .idTipoDocEmp(e.getContractorDocumentType())
                                .idEmpresa(e.getContractorDocumentNumber())
                                .estadoEmpresa("ACTIVA")
                                .idTipoDocPer(e.getDocumentType())
                                .idPersona(e.getDocumentNumber())
                                .estadoPersona("ACTIVO")
                                .appSource(BALU_PRE)
                                .build())
                        .toList()
        );

        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();
        List<TmpAffiliateStatusDTO> result = new ArrayList<>();
        result.addAll(dependentsFuture.join());
        result.addAll(independentsFuture.join());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerEmployeeDTO> consultPersonAsEmployerEmployee(
            String employerDocType, String employerDocNumber,
            String personDocType, String personDocNumber
    ) {
        log.info("[EXCEL-TMP] consultPersonAsEmployerEmployee tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                employerDocType, employerDocNumber, personDocType, personDocNumber);

        String empType = employerDocType == null ? null : employerDocType.trim().toUpperCase(Locale.ROOT);
        String perType = personDocType == null ? null : personDocType.trim().toUpperCase(Locale.ROOT);

        boolean filterByEmployer = empType != null && !empType.isBlank() && employerDocNumber != null && !employerDocNumber.isBlank();
        if (!filterByEmployer) {
            log.debug("[EXCEL-TMP] Employer filters not provided. Falling back to person-only search");
        }

        CompletableFuture<List<EmployerEmployeeDTO>> dependentsFuture = CompletableFuture.supplyAsync(() -> {
            if (filterByEmployer) {
                return dependentRepository
                        .findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
                                perType, personDocNumber, empType, employerDocNumber)
                        .stream()
                        .map(this::mapDependentToEmployerEmployee)
                        .toList();
            } else {
                return dependentRepository
                        .findByDocumentTypeAndDocumentNumber(perType, personDocNumber)
                        .stream()
                        .map(this::mapDependentToEmployerEmployee)
                        .toList();
            }
        });

        CompletableFuture<List<EmployerEmployeeDTO>> independentsFuture = CompletableFuture.supplyAsync(() -> {
            if (filterByEmployer) {
                return independentRepository
                        .findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
                                perType, personDocNumber, empType, employerDocNumber)
                        .stream()
                        .map(this::mapIndependentToEmployerEmployee)
                        .toList();
            } else {
                return independentRepository
                        .findByDocumentTypeAndDocumentNumber(perType, personDocNumber)
                        .stream()
                        .map(this::mapIndependentToEmployerEmployee)
                        .toList();
            }
        });

        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();
        List<EmployerEmployeeDTO> result = new ArrayList<>();
        result.addAll(dependentsFuture.join());
        result.addAll(independentsFuture.join());
        log.info("[EXCEL-TMP] consultPersonAsEmployerEmployee returned {} records", result.size());
        if (log.isDebugEnabled() && !result.isEmpty()) {
            log.debug("[EXCEL-TMP] Example record: {}", result.get(0));
        }
        return result;
    }

    private EmployerEmployeeDTO mapDependentToEmployerEmployee(ExcelDependentTmp e) {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        dto.setIdTipoDocEmp(e.getEmployerDocumentType());
        dto.setIdEmpresa(e.getEmployerDocumentNumber());
        dto.setSubEmpresa(parseIntegerSafe(e.getSubCompanyCode()));
        dto.setIdTipoDocPer(e.getDocumentType());
        dto.setIdPersona(e.getDocumentNumber());
        dto.setNombre1(e.getFirstName());
        dto.setNombre2(e.getSecondName());
        dto.setApellido1(e.getFirstSurname());
        dto.setApellido2(e.getSecondSurname());
        dto.setSexo(mapSexo(e.getSex()));
        dto.setFechaInicioVinculacion(formatDate(e.getCoverageStartDate()));
        dto.setFechaFinVinculacion(null);
        dto.setFechaNacimiento(formatDate(e.getDateOfBirth()));
        dto.setIdAfp(parseIntegerSafe(e.getAfpCode()));
        dto.setIdEps(e.getEpsCode());
        dto.setDireccionPersona(e.getAddress());
        dto.setIdOcupacion(parseIntegerSafe(e.getOccupationCode()));
        dto.setSalarioMensual(mapLong(e.getSalaryIbc()));
        dto.setIdDepartamento(parseIntegerSafe(e.getResidenceDepartmentDaneCode()));
        dto.setIdMunicipio(parseIntegerSafe(e.getResidenceMunicipalityDaneCode()));
        dto.setAppSource(BALU_PRE);
        return dto;
    }

    private EmployerEmployeeDTO mapIndependentToEmployerEmployee(ExcelIndependentTmp e) {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        dto.setIdTipoDocEmp(e.getContractorDocumentType());
        dto.setIdEmpresa(e.getContractorDocumentNumber());
        dto.setSubEmpresa(parseIntegerSafe(e.getSubCompanyCode()));
        dto.setIdTipoDocPer(e.getDocumentType());
        dto.setIdPersona(e.getDocumentNumber());
        dto.setNombre1(e.getFirstName());
        dto.setNombre2(e.getSecondName());
        dto.setApellido1(e.getFirstSurname());
        dto.setApellido2(e.getSecondSurname());
        dto.setSexo(mapSexo(e.getSex()));
        dto.setFechaInicioVinculacion(formatDate(e.getCoverageStartDate()));
        dto.setFechaFinVinculacion(null);
        dto.setFechaNacimiento(formatDate(e.getDateOfBirth()));
        dto.setIdAfp(parseIntegerSafe(e.getAfpCode()));
        dto.setIdEps(e.getEpsCode());
        dto.setDireccionPersona(e.getResidenceAddress());
        dto.setIdOcupacion(parseIntegerSafe(e.getOccupationCode()));
        dto.setNombreOcupacion(e.getWorkCenterActivity());
        dto.setSalarioMensual(mapLong(e.getBaseContributionIncome()));
        dto.setIdDepartamento(parseIntegerSafe(e.getResidenceDepartmentDaneCode()));
        dto.setIdMunicipio(parseIntegerSafe(e.getResidenceMunicipalityDaneCode()));
        dto.setAppSource(BALU_PRE);
        return dto;
    }

    private TmpExcelPersonDTO mapDependent(ExcelDependentTmp e) {
        return TmpExcelPersonDTO.builder()
                .idTipoDocPer(e.getDocumentType())
                .idPersona(e.getDocumentNumber())
                .apellido1(e.getFirstSurname())
                .apellido2(e.getSecondSurname())
                .nombre1(e.getFirstName())
                .nombre2(e.getSecondName())
                .fechaNacimiento(formatDate(e.getDateOfBirth()))
                .sexo(mapSexo(e.getSex()))
                .direccionPersona(e.getAddress())
                .idDepartamento(e.getResidenceDepartmentDaneCode())
                .idMunicipio(e.getResidenceMunicipalityDaneCode())
                .idEps(e.getEpsCode())
                .idAfp(e.getAfpCode())
                .fechaInicioVinculacion(formatDate(e.getCoverageStartDate()))
                .idOcupacion(e.getOccupationCode())
                .salarioMensual(mapDecimal(e.getSalaryIbc()))
                .idTipoDocEmp(e.getEmployerDocumentType())
                .idEmpresa(e.getEmployerDocumentNumber())
                .subEmpresa(e.getSubCompanyCode())
                .build();
    }

    private TmpExcelPersonDTO mapIndependent(ExcelIndependentTmp e) {
        return TmpExcelPersonDTO.builder()
                .idTipoDocPer(e.getDocumentType())
                .idPersona(e.getDocumentNumber())
                .apellido1(e.getFirstSurname())
                .apellido2(e.getSecondSurname())
                .nombre1(e.getFirstName())
                .nombre2(e.getSecondName())
                .fechaNacimiento(formatDate(e.getDateOfBirth()))
                .sexo(mapSexo(e.getSex()))
                .direccionPersona(e.getResidenceAddress())
                .idDepartamento(e.getResidenceDepartmentDaneCode())
                .idMunicipio(e.getResidenceMunicipalityDaneCode())
                .idEps(e.getEpsCode())
                .idAfp(e.getAfpCode())
                .fechaInicioVinculacion(formatDate(e.getCoverageStartDate()))
                .idOcupacion(e.getOccupationCode())
                .salarioMensual(mapDecimal(e.getBaseContributionIncome()))
                .idTipoDocEmp(e.getContractorDocumentType())
                .idEmpresa(e.getContractorDocumentNumber())
                .subEmpresa(e.getSubCompanyCode())
                .nombreOcupacion(e.getWorkCenterActivity())
                .build();
    }

    private String mapDecimal(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : DATE_OUT.format(date);
    }

    private String mapSexo(String sexo) {
        if (sexo == null) return null;
        String s = sexo.trim().toUpperCase(Locale.ROOT);
        if ("M".equals(s) || "MASCULINO".equals(s)) return "MASCULINO";
        if ("F".equals(s) || "FEMENINO".equals(s)) return "FEMENINO";
        return s;
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return value == null ? null : Integer.valueOf(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long mapLong(BigDecimal value) {
        try {
            return value == null ? null : value.longValue();
        } catch (Exception ignored) {
            return null;
        }
    }
}


