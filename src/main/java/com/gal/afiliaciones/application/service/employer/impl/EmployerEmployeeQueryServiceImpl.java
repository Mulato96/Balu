package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.employer.EmployerEmployeeQueryService;
import com.gal.afiliaciones.application.service.tmp.ExcelPersonConsultationService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeListResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeQueryRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateClient;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateResult;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusClient;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateStatusGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpStatusResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerEmployeeQueryServiceImpl implements EmployerEmployeeQueryService {

    private final AffiliationDependentRepository affiliationDependentRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliateRepository affiliateRepository;
    private final OccupationRepository occupationRepository;
    private final MunicipalityRepository municipalityRepository;
    private final DepartmentRepository departmentRepository;
    private final FundPensionRepository fundPensionRepository;
    private final ArlRepository arlRepository;
    private final HealthPromotingEntityRepository healthRepository;
    private final OccupationDecree1563Repository occupationDecree1563Repository; // kept for existing mappers
    private final ExcelPersonConsultationService excelPersonConsultationService;
    private final ConsultSiarpAffiliateClient consultSiarpAffiliateClient;
    private final SiarpAffiliateGateway siarpAffiliateGateway;
    private final ConsultSiarpAffiliateStatusClient consultSiarpAffiliateStatusClient;
    private final SiarpAffiliateStatusGateway siarpAffiliateStatusGateway;

    private static final String BALU_PRE = "BALU_PRE";
    private static final String FORMATT_DATE = "dd-MM-yyyy";

    @Override
    public EmployerEmployeeListResponseDTO queryEmployeeByParameters2(EmployerEmployeeQueryRequestDTO request) {
        log.info("[Orchestrator] Start queryEmployeeByParameters tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());

        CompletableFuture<List<EmployerEmployeeDTO>> siarpFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SiarpAffiliateResult result = siarpAffiliateGateway
                        .getAffiliate(request.getTDocAfi(), request.getIdAfi())
                        .blockOptional().orElse(null);
                if (result != null && result.dto().isPresent()) {
                    List<EmployerEmployeeDTO> list = result.dto().get();
                    log.debug("[Orchestrator] SIARP query (DTO) completed with {} results", list.size());
                    return list;
                }
                // RAW returned or empty; for service contract we return empty
                log.debug("[Orchestrator] SIARP query returned RAW or empty; returning empty list from service");
                return List.of();
            } catch (Exception ex) {
                log.warn("[Orchestrator] SIARP query failed: {}", ex.getMessage());
                return List.of();
            }
        });

        CompletableFuture<List<EmployerEmployeeDTO>> baluFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("[Orchestrator] Launching BALU query in parallel");
            List<EmployerEmployeeDTO> result = queryFromBalu(request);
            log.debug("[Orchestrator] BALU query completed with {} results", result.size());
            return result;
        });

        CompletableFuture<List<EmployerEmployeeDTO>> excelFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("[Orchestrator] Launching EXCEL TMP query in parallel");
            List<EmployerEmployeeDTO> result = excelPersonConsultationService.consultPersonAsEmployerEmployee(
                    request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
            log.debug("[Orchestrator] EXCEL TMP query completed with {} results", result.size());
            return result;
        });

        // Priority: SIARP → BALU → EXCEL TMP
        List<EmployerEmployeeDTO> siarpResult = siarpFuture.join();
        if (siarpResult != null && !siarpResult.isEmpty()) {
            log.info("[Orchestrator] Returning SIARP response with {} records", siarpResult.size());
            if (log.isDebugEnabled()) {
                log.debug("[Orchestrator] Example SIARP record: {}", siarpResult.get(0));
            }
            return EmployerEmployeeListResponseDTO.builder()
                    .employees(siarpResult)
                    .success(true)
                    .message("Consulta exitosa - Fuente: SIARP")
                    .build();
        }

        List<EmployerEmployeeDTO> baluResult = baluFuture.join();
        if (baluResult != null && !baluResult.isEmpty()) {
            log.info("[Orchestrator] Returning BALU response with {} records", baluResult.size());
            if (log.isDebugEnabled() && !baluResult.isEmpty()) {
                log.debug("[Orchestrator] Example BALU record: {}", baluResult.get(0));
            }
            // mark source
            baluResult.forEach(e -> e.setAppSource("BALU"));
            return EmployerEmployeeListResponseDTO.builder()
                    .employees(baluResult)
                    .success(true)
                    .message("Consulta exitosa - Fuente: BALU")
                    .build();
        }

        // If BALU empty, use EXCEL TMP
        List<EmployerEmployeeDTO> excelResult = excelFuture.join();
        if (excelResult != null && !excelResult.isEmpty()) {
            log.info("[Orchestrator] BALU empty. Returning EXCEL TMP response with {} records", excelResult.size());
            if (log.isDebugEnabled() && !excelResult.isEmpty()) {
                log.debug("[Orchestrator] Example EXCEL record: {}", excelResult.get(0));
            }
            excelResult.forEach(e -> e.setAppSource(BALU_PRE));
            // Enrich with descriptive names (best-effort; keep codes if not found)
            enrichExcelDescriptions(excelResult);
            return EmployerEmployeeListResponseDTO.builder()
                    .employees(excelResult)
                    .success(true)
                    .message("Consulta exitosa - Fuente: EXCEL TMP")
                    .build();
        }

        log.info("[Orchestrator] No records found in BALU or EXCEL TMP");
        return EmployerEmployeeListResponseDTO.builder()
                .employees(new ArrayList<>())
                .success(true)
                .message("No se encontraron afiliados con los parámetros especificados")
                .build();
    }

    @Override
    public EmployerEmployeeListResponseDTO queryEmployeeByParameters2(EmployerEmployeeQueryRequestDTO request, String appSource) {
        if (appSource != null) {
            String src = appSource.trim().toUpperCase(java.util.Locale.ROOT);
            if ("BALU".equals(src)) {
                java.util.List<EmployerEmployeeDTO> baluOnly = queryFromBalu(request);
                baluOnly.forEach(e -> e.setAppSource("BALU"));
                return EmployerEmployeeListResponseDTO.builder()
                        .employees(baluOnly)
                        .success(true)
                        .message("Consulta exitosa - Fuente forzada: BALU")
                        .build();
            }
            if (BALU_PRE.equals(src)) {
                java.util.List<EmployerEmployeeDTO> excelOnly = excelPersonConsultationService.consultPersonAsEmployerEmployee(
                        request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
                excelOnly.forEach(e -> e.setAppSource(BALU_PRE));
                // Enrich with descriptive names (best-effort; keep codes if not found)
                enrichExcelDescriptions(excelOnly);
                return EmployerEmployeeListResponseDTO.builder()
                        .employees(excelOnly)
                        .success(true)
                        .message("Consulta exitosa - Fuente forzada: EXCEL TMP")
                        .build();
            }
        }
        return queryEmployeeByParameters2(request);
    }

    @Override
    public java.util.List<TmpAffiliateStatusDTO> queryEmployeeByParameters1(EmployerEmployeeQueryRequestDTO request) {
        log.info("[Orchestrator-Status] Start queryEmployeeByParameters1 tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());

        // Priority: SIARP → BALU → EXCEL
        CompletableFuture<java.util.List<TmpAffiliateStatusDTO>> siarpFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SiarpStatusResult result = siarpAffiliateStatusGateway
                        .getStatus(request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi())
                        .blockOptional().orElse(null);
                if (result != null && result.dto().isPresent()) {
                    List<TmpAffiliateStatusDTO> list = result.dto().get();
                    log.debug("[Orchestrator-Status] SIARP status (DTO) with {} results", list.size());
                    return list;
                } else {
                    // RAW present or empty; current service contract returns DTO list.
                    // We return empty list here. The controller endpoint will handle raw return directly.
                    log.debug("[Orchestrator-Status] SIARP status returned RAW or empty; returning empty list from service");
                    return List.of();
                }
            } catch (Exception ex) {
                log.warn("[Orchestrator-Status] SIARP status query failed: {}", ex.getMessage());
                return List.of();
            }
        });

        // Run BALU and EXCEL in parallel
        CompletableFuture<java.util.List<TmpAffiliateStatusDTO>> baluFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("[Orchestrator-Status] Launching BALU status query");
            return queryStatusFromBalu(request);
        });
        CompletableFuture<java.util.List<TmpAffiliateStatusDTO>> excelFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("[Orchestrator-Status] Launching EXCEL status query");
            return excelPersonConsultationService.consultAffiliateStatus(
                    request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
        });

        java.util.List<TmpAffiliateStatusDTO> siarp = siarpFuture.join();
        if (siarp != null && !siarp.isEmpty()) {
            log.info("[Orchestrator-Status] Returning SIARP status with {} records", siarp.size());
            return siarp;
        }

        java.util.List<TmpAffiliateStatusDTO> balu = baluFuture.join();
        if (balu != null && !balu.isEmpty()) {
            log.info("[Orchestrator-Status] Returning BALU status with {} records", balu.size());
            return balu;
        }

        java.util.List<TmpAffiliateStatusDTO> excel = excelFuture.join();
        log.info("[Orchestrator-Status] Returning EXCEL status with {} records", excel != null ? excel.size() : 0);
        return excel != null ? excel : java.util.List.of();
    }

    @Override
    public java.util.List<TmpAffiliateStatusDTO> queryEmployeeByParameters1(EmployerEmployeeQueryRequestDTO request, String appSource) {
        if (appSource != null) {
            String src = appSource.trim().toUpperCase(java.util.Locale.ROOT);
            if ("BALU".equals(src)) {
                return queryStatusFromBalu(request);
            }
            if (BALU_PRE.equals(src)) {
                return excelPersonConsultationService.consultAffiliateStatus(
                        request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
            }
        }
        return queryEmployeeByParameters1(request);
    }

    private java.util.List<TmpAffiliateStatusDTO> queryStatusFromBalu(EmployerEmployeeQueryRequestDTO request) {
        String empType = request.getTDocEmp() == null ? null : request.getTDocEmp().trim().toUpperCase(java.util.Locale.ROOT);
        String perType = request.getTDocAfi() == null ? null : request.getTDocAfi().trim().toUpperCase(java.util.Locale.ROOT);
        String idEmp = request.getIdEmp() == null ? null : request.getIdEmp().trim();
        String idAfi = request.getIdAfi() == null ? null : request.getIdAfi().trim();

        boolean filterByEmployer = empType != null && !empType.isBlank() && idEmp != null && !idEmp.isBlank();

        java.util.List<Affiliate> affiliates;
        if (filterByEmployer) {
            affiliates = affiliateRepository.findByCompanyAndAffiliateDocument(empType, idEmp, perType, idAfi);
        } else {
            affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(perType, idAfi);
        }
        log.debug("[BALU-Status] Affiliates found: {}", affiliates.size());

        java.util.List<TmpAffiliateStatusDTO> result = new java.util.ArrayList<>();
        for (Affiliate a : affiliates) {
            TmpAffiliateStatusDTO dto = TmpAffiliateStatusDTO.builder()
                    .idTipoDocEmp(a.getDocumenTypeCompany())
                    .idEmpresa(a.getNitCompany())
                    .estadoEmpresa("ACTIVA")
                    .idTipoDocPer(a.getDocumentType())
                    .idPersona(a.getDocumentNumber())
                    .estadoPersona("ACTIVO")
                    .appSource("BALU")
                    .build();
            result.add(dto);
        }
        return result;
    }

    // removed unused equals helpers after refactor

    // Removed local Excel mapping and parse helpers; Excel service now returns EmployerEmployeeDTO

    private List<EmployerEmployeeDTO> queryFromBalu(EmployerEmployeeQueryRequestDTO request) {
        log.info("[BALU] Querying employee with parameters: tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());

        List<EmployerEmployeeDTO> employees = new ArrayList<>();

        // First, search in affiliate table with the 4 parameters
        // Normalize document types (uppercase/trim) similar to Excel service
        String empType = request.getTDocEmp() == null ? null : request.getTDocEmp().trim().toUpperCase(java.util.Locale.ROOT);
        String perType = request.getTDocAfi() == null ? null : request.getTDocAfi().trim().toUpperCase(java.util.Locale.ROOT);
        String idEmp = request.getIdEmp() == null ? null : request.getIdEmp().trim();
        String idAfi = request.getIdAfi() == null ? null : request.getIdAfi().trim();

        log.info("[BALU] Searching affiliates with (normalized): tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                empType, idEmp, perType, idAfi);

        boolean hasEmployerFilters = empType != null && !empType.isBlank()
                && idEmp != null && !idEmp.isBlank();

        List<Affiliate> affiliates;
        if (hasEmployerFilters) {
            affiliates = affiliateRepository.findByCompanyAndAffiliateDocument(
                    empType, idEmp, perType, idAfi);
            log.debug("[BALU] Used company+person filter. Affiliates found: {}", affiliates.size());
        } else {
            affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                    perType, idAfi);
            log.debug("[BALU] Used person-only filter. Affiliates found: {}", affiliates.size());
        }

        log.info("[BALU] Found {} affiliates for parameters", affiliates.size());

        if (affiliates.isEmpty()) {
            log.warn("[BALU] No affiliates found for parameters: tDocEmp={}, idEmp={}, tDocAfi={}, idAfi={}",
                    request.getTDocEmp(), request.getIdEmp(), request.getTDocAfi(), request.getIdAfi());
            return employees;
        }

        // For each affiliate found, get the filed_number and search in the appropriate table
        for (Affiliate affiliate : affiliates) {
            log.info("[BALU] Processing affiliate with filed_number: {} and affiliation_type: {}",
                    affiliate.getFiledNumber(), affiliate.getAffiliationType());

            if (affiliate.getFiledNumber() == null) {
                log.warn("[BALU] Affiliate {} has null filed_number, skipping", affiliate.getIdAffiliate());
                continue;
            }

            // Check affiliation type to determine which table to query
            if ("Trabajador Dependiente".equals(affiliate.getAffiliationType())) {
                log.info("[BALU] Searching in affiliation_dependent for filed_number: {}", affiliate.getFiledNumber());
                Optional<AffiliationDependent> dependents = affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber());
                if (dependents.isPresent()){
                    EmployerEmployeeDTO employeeDTO = mapDependentToEmployeeDTO(dependents.get(), affiliate);
                    employees.add(employeeDTO);
                }

            } else if ("Trabajador Independiente".equals(affiliate.getAffiliationType())) {
                log.info("[BALU] Searching in affiliation_detail for filed_number: {}", affiliate.getFiledNumber());
                Optional<Affiliation> independents = affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber());

                if (independents.isPresent()){
                    EmployerEmployeeDTO employeeDTO = mapIndependentToEmployeeDTO(independents.get(), affiliate);
                    employees.add(employeeDTO);
                }
            } else {
                log.warn("[BALU] Unknown affiliation type: {} for affiliate {}",
                        affiliate.getAffiliationType(), affiliate.getIdAffiliate());
            }
        }

        log.info("[BALU] Built {} employee records from BALU repositories", employees.size());
        return employees;
    }

    // Removed unused searchDependentsByParameters and searchIndependentsByParameters helper methods after refactor

    private EmployerEmployeeDTO mapDependentToEmployeeDTO(AffiliationDependent dependent, Affiliate affiliate) {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        
        // Map from dependent and affiliate data
        dto.setIdTipoDocEmp(affiliate.getDocumentType());
        dto.setIdEmpresa(affiliate.getNitCompany());
        dto.setRazonSocial(affiliate.getCompany());
        dto.setSubEmpresa(0); // Default value for dependent
        dto.setIdTipoDocPer(dependent.getIdentificationDocumentType());
        dto.setIdPersona(dependent.getIdentificationDocumentNumber());
        dto.setNombre1(dependent.getFirstName());
        dto.setNombre2(dependent.getSecondName());
        dto.setApellido1(dependent.getSurname());
        dto.setApellido2(dependent.getSecondSurname());
        dto.setSexo(dependent.getGender());
        dto.setFechaInicioVinculacion(affiliate.getCoverageStartDate() != null ?
                affiliate.getCoverageStartDate().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        dto.setFechaFinVinculacion(affiliate.getRetirementDate() != null ?
                affiliate.getRetirementDate().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        dto.setFechaNacimiento(dependent.getDateOfBirth() != null ? 
            dependent.getDateOfBirth().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        
        // Get AFP information from database
        if (dependent.getPensionFundAdministrator() != null) {
            dto.setIdAfp(dependent.getPensionFundAdministrator().intValue());
            fundPensionRepository.findById(dependent.getPensionFundAdministrator().longValue())
                    .ifPresent(afp -> dto.setNombreAfp(afp.getNameAfp()));
        }
        
        // Get EPS information from database
        if (dependent.getHealthPromotingEntity() != null) {
            healthRepository.findById(dependent.getHealthPromotingEntity())
                    .ifPresent(eps -> {
                        dto.setIdEps(eps.getCodeEPS());
                        dto.setNombreEps(eps.getNameEPS());
                    });
        }
        
        // Get ARP information from database
        if (dependent.getOccupationalRiskManager() != null) {
            try {
                Long arlId = Long.parseLong(dependent.getOccupationalRiskManager());
                dto.setIdArp(arlId.intValue());
                arlRepository.findById(arlId)
                        .ifPresent(arl -> dto.setNombreArp(arl.getAdministrator()));
            } catch (NumberFormatException e) {
                log.warn("Invalid ARL ID format: {}", dependent.getOccupationalRiskManager());
            }
        }
        
        dto.setDireccionPersona(dependent.getAddress());

        // Get occupation information from database
        if (dependent.getIdOccupation() != null) {
            dto.setIdOcupacion(dependent.getIdOccupation().intValue());
            occupationRepository.findById(dependent.getIdOccupation())
                    .ifPresent(occupation -> dto.setNombreOcupacion(occupation.getNameOccupation()));
        }
        
        dto.setSalarioMensual(dependent.getSalary() != null ? 
            dependent.getSalary().longValue() : null);
        
        // Get department information from database - keep same ID
        if (dependent.getIdDepartment() != null) {
            dto.setIdDepartamento(dependent.getIdDepartment().intValue());
            departmentRepository.findById((long) dependent.getIdDepartment().intValue())
                    .ifPresent(dept -> dto.setNombreDepartamento(dept.getDepartmentName()));
        }
        
        // Get municipality information from database - use municipality code without leading zeros
        if (dependent.getIdCity() != null) {
            municipalityRepository.findById(dependent.getIdCity())
                    .ifPresent(mun -> {
                        dto.setIdMunicipio(Integer.parseInt(mun.getMunicipalityCode()));
                        dto.setNombreMunicipio(mun.getMunicipalityName());
                    });
        }
        
        return dto;
    }

    private EmployerEmployeeDTO mapIndependentToEmployeeDTO(Affiliation independent, Affiliate affiliate) {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        
        // Map from independent and affiliate data
        dto.setIdTipoDocEmp(affiliate.getDocumentType());
        dto.setIdEmpresa(affiliate.getNitCompany());
        dto.setRazonSocial(affiliate.getCompany());
        dto.setSubEmpresa(0); // Default value for independent
        dto.setIdTipoDocPer(independent.getIdentificationDocumentType());
        dto.setIdPersona(independent.getIdentificationDocumentNumber());
        dto.setNombre1(independent.getFirstName());
        dto.setNombre2(independent.getSecondName());
        dto.setApellido1(independent.getSurname());
        dto.setApellido2(independent.getSecondSurname());
        dto.setSexo(independent.getGender());
        dto.setFechaInicioVinculacion(independent.getContractStartDate() != null ? 
            independent.getContractStartDate().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        dto.setFechaFinVinculacion(independent.getContractEndDate() != null ? 
            independent.getContractEndDate().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        dto.setFechaNacimiento(independent.getDateOfBirth() != null ? 
            independent.getDateOfBirth().format(DateTimeFormatter.ofPattern(FORMATT_DATE)) : null);
        
        // Get AFP information from database
        if (independent.getPensionFundAdministrator() != null) {
            dto.setIdAfp(independent.getPensionFundAdministrator().intValue());
            fundPensionRepository.findById(independent.getPensionFundAdministrator().longValue())
                    .ifPresent(afp -> dto.setNombreAfp(afp.getNameAfp()));
        }
        
        // Get EPS information from database
        if (independent.getHealthPromotingEntity() != null) {
            healthRepository.findById(independent.getHealthPromotingEntity())
                    .ifPresent(eps -> {
                        dto.setIdEps(eps.getCodeEPS());
                        dto.setNombreEps(eps.getNameEPS());
                    });
        }
        
        // Get ARP information from database
        if (independent.getCurrentARL() != null) {
            try {
                Long arlId = Long.parseLong(independent.getCurrentARL());
                dto.setIdArp(arlId.intValue());
                arlRepository.findById(arlId)
                        .ifPresent(arl -> dto.setNombreArp(arl.getAdministrator()));
            } catch (NumberFormatException e) {
                log.warn("Invalid ARL ID format: {}", independent.getCurrentARL());
            }
        }
        
        dto.setDireccionPersona(independent.getAddress());
        
        // Get occupation information from database - for independent, use occupation field
        if (independent.getOccupation() != null) {
            dto.setNombreOcupacion(independent.getOccupation());
            // Set a default occupation ID for independents
            dto.setIdOcupacion(9999); // Default occupation ID for independents
        }
        
        dto.setSalarioMensual(independent.getContractMonthlyValue() != null ? 
            independent.getContractMonthlyValue().longValue() : null);
        
        // Get department information from database - keep same ID
        if (independent.getDepartment() != null) {
            dto.setIdDepartamento(independent.getDepartment().intValue());
            departmentRepository.findById((long) independent.getDepartment().intValue())
                    .ifPresent(dept -> dto.setNombreDepartamento(dept.getDepartmentName()));
        }
        
        // Get municipality information from database - use municipality code without leading zeros
        if (independent.getCityMunicipality() != null) {
            municipalityRepository.findById(independent.getCityMunicipality())
                    .ifPresent(mun -> {
                        dto.setIdMunicipio(Integer.parseInt(mun.getMunicipalityCode()));
                        dto.setNombreMunicipio(mun.getMunicipalityName());
                    });
        }
        
        return dto;
    }

    private void enrichExcelDescriptions(List<EmployerEmployeeDTO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (EmployerEmployeeDTO dto : records) {
            try {
                Integer depId = dto.getIdDepartamento();
                if (depId != null) {
                    departmentRepository.findById((long) depId.intValue())
                            .ifPresent(d -> dto.setNombreDepartamento(d.getDepartmentName()));
                }

                Integer munId = dto.getIdMunicipio();
                if (munId != null) {
                    municipalityRepository.findById((long) munId.intValue())
                            .ifPresent(m -> dto.setNombreMunicipio(m.getMunicipalityName()));
                }

                String epsCode = dto.getIdEps();
                if (epsCode != null && !epsCode.isBlank()) {
                    healthRepository.findByCodeEPS(epsCode)
                            .ifPresent(h -> dto.setNombreEps(h.getNameEPS()));
                }
            } catch (Exception ex) {
                log.debug("[EXCEL-TMP] Enrichment skipped for record: {}", ex.getMessage());
            }
        }
    }
}
