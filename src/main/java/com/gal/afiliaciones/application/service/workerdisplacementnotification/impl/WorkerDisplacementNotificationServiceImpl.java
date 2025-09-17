package com.gal.afiliaciones.application.service.workerdisplacementnotification.impl;

import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.GeneralNoveltyService;
import com.gal.afiliaciones.application.service.workerdisplacementnotification.WorkerDisplacementNotificationService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.workerdisplacementnotification.WorkerDisplacementNotification;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workerdisplacementnotification.WorkerDisplacementNotificationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.WorkerDisplacementNotificationSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.CreateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementListResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementNotificationDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementQueryRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.UpdateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.WorkerDataResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.WorkerSummaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementNotFoundException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementValidationException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkerDisplacementNotificationServiceImpl implements WorkerDisplacementNotificationService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final WorkerDisplacementNotificationRepository displacementRepository;
    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final HealthPromotingEntityRepository healthRepository;
    private final FundPensionRepository fundPensionRepository;
    private final FiledService filedService;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final GeneralNoveltyService generalNoveltyService;

    // Reused literals consolidated as constants to satisfy Sonar rules
    private static final String ERR_REQUEST_NULL = "La solicitud no puede ser nula";
    private static final String ERR_DISPLACEMENT_NOT_FOUND = "Desplazamiento no encontrado";
    private static final String NAME_NOT_AVAILABLE = "Nombre no disponible";
    private static final String SORT_FIELD_START_DATE = "displacementStartDate";
    private static final String LIFECYCLE_ACTIVE = "ACTIVO";
    private static final String STATUS_EN_CURSO = "EN_CURSO";
    private static final String STATUS_REGISTRADO = "REGISTRADO";
    private static final String STATUS_CULMINADO = "CULMINADO";
    private static final String STATUS_TERMINADO = "TERMINADO";

    // Removed obsolete getWorkerDisplacementsPage method to align with interface

    private Affiliate findEmployer() {
        log.debug("=== SEARCHING FOR EMPLOYER (CURRENT USER CONTEXT) ===");
        CurrentUserContext current = resolveCurrentUserContext();
        if (current == null) {
            log.error("No authenticated user context available to resolve employer");
            throw new AffiliateNotFound("No se pudo resolver el empleador del usuario actual");
        }

        Optional<Affiliate> employerOpt = affiliateRepository.findOne(
                AffiliateSpecification.findMercantileByLegalRepresentative(current.documentNumber())
        );
        if (employerOpt.isEmpty()) {
            employerOpt = affiliateRepository.findOne(
                    AffiliateSpecification.findDomesticEmployerByLegalRepresentative(current.documentNumber())
            );
        }
        if (employerOpt.isEmpty()) {
            employerOpt = affiliateRepository.findOne(
                    AffiliateSpecification.findByEmployerAndIdentification(current.documentType(), current.documentNumber())
            );
        }

        Affiliate foundEmployer = employerOpt.orElseThrow(() -> {
            log.error("NO EMPLOYER FOUND - Current user document: {}-{}", current.documentType(), current.documentNumber());
            return new AffiliateNotFound("Empleador no encontrado para el usuario actual");
        });

        log.info("EMPLOYER FOUND - ID: {}, Company: '{}', NIT: '{}'", foundEmployer.getIdAffiliate(), foundEmployer.getCompany(), foundEmployer.getNitCompany());
        log.debug("Employer details: {}", foundEmployer);

        return foundEmployer;
    }

    

    

    // buildWorkerFoundResponse was used solely by the removed consult endpoint.
    // Kept intentionally removed to simplify API surface.

    private Optional<WorkerDetails> fetchWorkerDetails(Affiliate workerAffiliate) {
        String filed = workerAffiliate.getFiledNumber();
        if (filed == null) {
            return Optional.empty();
        }

        try {
            if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(workerAffiliate.getAffiliationType())) {
                return affiliationRepository.findByFiledNumber(filed).map(a -> new WorkerDetails(
                        a.getFirstName(), a.getSecondName(), a.getSurname(), a.getSecondSurname(),
                        a.getDateOfBirth(), a.getGender(), a.getNationality(),
                        buildCompleteAddress(a), a.getHealthPromotingEntity(), a.getPensionFundAdministrator(),
                        a.getPhone1(), a.getPhone2(), a.getEmail()
                ));
            } else {
                return affiliationDependentRepository.findByFiledNumber(filed).map(a -> new WorkerDetails(
                        a.getFirstName(), a.getSecondName(), a.getSurname(), a.getSecondSurname(),
                        a.getDateOfBirth(), a.getGender(), a.getNationality(),
                        buildCompleteAddress(a), a.getHealthPromotingEntity(), a.getPensionFundAdministrator(),
                        a.getPhone1(), a.getPhone2(), a.getEmail()
                ));
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener detalles del trabajador para filedNumber {}: {}", filed, ex.getMessage());
            return Optional.empty();
        }
    }

   

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String buildCompleteAddress(Affiliation affiliation) {
        return affiliation.getAddress();
    }

    private String buildCompleteAddress(AffiliationDependent affiliation) {
        return affiliation.getAddress();
    }

    // ===== NEW DISPLACEMENT CRUD METHODS =====

    @Override
    public List<DisplacementNotificationDTO> getWorkerDisplacements(String workerDocType, String workerDocNumber) {
        log.info("Obteniendo desplazamientos para trabajador: {} - {} y empleador autenticado", 
                workerDocType, workerDocNumber);

        try {
            Affiliate employer = findEmployer();
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .byWorker(workerDocType, workerDocNumber)
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employer.getNitCompany()))
                            .and(WorkerDisplacementNotificationSpecification.withFetchJoins());

            List<WorkerDisplacementNotification> displacements = displacementRepository
                    .findAll(spec, Sort.by(Sort.Direction.DESC, SORT_FIELD_START_DATE));
            
            log.info("Encontrados {} desplazamientos", displacements.size());
            
            return displacements.stream()
                    .map(this::mapToDTO)
                    .toList();
                    
        } catch (Exception ex) {
            log.error("Error obteniendo desplazamientos: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo desplazamientos", ex);
        }
    }

    

    @Override
    public DisplacementListResponse getWorkerDisplacementsWithRelationshipStatus(DisplacementQueryRequest request,
                                                                                 Integer pageParam,
                                                                                 Integer sizeParam,
                                                                                 String sortByParam,
                                                                                 String sortOrderParam) {
        log.info("Obteniendo desplazamientos con estado de relaciones para trabajador: {} - {} (empleador inferido)", 
                request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber());

        try {
            // Validate request
            validateRequest(request);
            
            // Find employer using current logged-in user context
            Affiliate employer = findEmployer();
            String employerNit = employer.getNitCompany();
            
            // Build pageable from params (defaults if null)
            int page = pageParam != null ? pageParam : 0;
            int size = sizeParam != null ? sizeParam : 10;
            String sortBy = (sortByParam == null || sortByParam.isBlank()
                    || "null".equalsIgnoreCase(sortByParam)
                    || "undefined".equalsIgnoreCase(sortByParam))
                    ? SORT_FIELD_START_DATE
                    : sortByParam;
            Sort.Direction direction;
            if (sortOrderParam == null || sortOrderParam.isBlank()
                    || "NULL".equalsIgnoreCase(sortOrderParam)
                    || "null".equalsIgnoreCase(sortOrderParam)
                    || "undefined".equalsIgnoreCase(sortOrderParam)) {
                direction = Sort.Direction.DESC;
            } else {
                direction = Sort.Direction.fromString(sortOrderParam);
            }
            String sortOrder = direction.name();
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // Page of displacements (paginated slice for the response list)
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .byWorker(request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber())
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employerNit));

            Page<WorkerDisplacementNotification> pageResult = displacementRepository.findAll(spec, pageable);

            List<DisplacementNotificationDTO> displacements = pageResult.map(this::mapToDTO).getContent();

            // Count active relationships and derive multiple flag
            int activeRelationshipsCount = countActiveRelationships(
                    request.getWorkerIdentificationType(), 
                    request.getWorkerIdentificationNumber(), 
                    employerNit);
            // If there are neither displacements nor active relationships, deny with 404
            if (activeRelationshipsCount == 0) {
                throw new DisplacementNotFoundException("Trabajador sin desplazamientos ni relaciones activas con el empleador actual");
            }
            boolean hasMultiple = activeRelationshipsCount > 1;

            // Totals across ALL results (not just the current page)
            int totalDisplacements = (int) pageResult.getTotalElements();
            long activeDisplacementsLong = displacementRepository.count(
                    WorkerDisplacementNotificationSpecification
                            .byWorker(request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber())
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employerNit))
                            .and(WorkerDisplacementNotificationSpecification.active())
            );
            int activeDisplacements = (int) activeDisplacementsLong;
            int inactiveDisplacements = Math.max(0, totalDisplacements - activeDisplacements);

            String message = String.format("Se encontraron %d desplazamientos (%d activos, %d inactivos). %s", 
                    totalDisplacements, activeDisplacements, inactiveDisplacements,
                    hasMultiple ? "El trabajador tiene múltiples relaciones activas." : "El trabajador tiene una relación activa.");
            
            // Build worker header using existing detailed fetch helpers
            WorkerSummaryDTO workerHeader = buildWorkerSummaryHeader(request, displacements);

            DisplacementListResponse response = DisplacementListResponse.builder()
                    .worker(workerHeader)
                    .hasMultipleActiveRelationships(hasMultiple)
                    .activeRelationshipsCount(activeRelationshipsCount)
                    .displacements(displacements)
                    .totalDisplacements(totalDisplacements)
                    .activeDisplacements(activeDisplacements)
                    .inactiveDisplacements(inactiveDisplacements)
                    .message(message)
                    .page(page)
                    .size(size)
                    .totalPages(pageResult.getTotalPages())
                    .sortBy(sortBy)
                    .sortOrder(sortOrder)
                    .build();
            
            log.info("Respuesta generada exitosamente - Total desplazamientos: {}, Múltiples relaciones: {}", 
                    totalDisplacements, hasMultiple);
            
            return response;
            
        } catch (DisplacementNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error obteniendo desplazamientos con estado de relaciones: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo información de desplazamientos", ex);
        }
    }

    @Override
    public WorkerDataResponse getWorkerDataSummary(DisplacementQueryRequest request) {
        log.info("Obteniendo resumen de datos de trabajador (sin paginación ni lista): {} - {}",
                request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber());

        try {
            // Validate request
            validateRequest(request);

            // Resolve employer
            Affiliate employer = findEmployer();
            String employerNit = employer.getNitCompany();

            // Count active relationships
            int activeRelationshipsCount = countActiveRelationships(
                    request.getWorkerIdentificationType(),
                    request.getWorkerIdentificationNumber(),
                    employerNit);
            if (activeRelationshipsCount == 0) {
                throw new DisplacementNotFoundException("Trabajador sin relaciones activas con el empleador actual");
            }
            boolean hasMultiple = activeRelationshipsCount > 1;

            // Totals across ALL results without fetching list
            int totalDisplacements = (int) displacementRepository.count(
                    WorkerDisplacementNotificationSpecification
                            .byWorker(request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber())
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employerNit))
            );
            int activeDisplacements = (int) displacementRepository.count(
                    WorkerDisplacementNotificationSpecification
                            .byWorker(request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber())
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employerNit))
                            .and(WorkerDisplacementNotificationSpecification.active())
            );
            int inactiveDisplacements = Math.max(0, totalDisplacements - activeDisplacements);

            String message = String.format("Se encontraron %d desplazamientos (%d activos, %d inactivos). %s",
                    totalDisplacements, activeDisplacements, inactiveDisplacements,
                    hasMultiple ? "El trabajador tiene múltiples relaciones activas." : "El trabajador tiene una relación activa.");

            // Build worker header (no displacement list, so pass empty list)
            WorkerSummaryDTO workerHeader = buildWorkerSummaryHeader(request, java.util.Collections.emptyList());

            WorkerDataResponse response = WorkerDataResponse.builder()
                    .worker(workerHeader)
                    .hasMultipleActiveRelationships(hasMultiple)
                    .activeRelationshipsCount(activeRelationshipsCount)
                    .totalDisplacements(totalDisplacements)
                    .activeDisplacements(activeDisplacements)
                    .inactiveDisplacements(inactiveDisplacements)
                    .message(message)
                    .build();

            log.info("Resumen generado exitosamente - Total desplazamientos: {}, Múltiples relaciones: {}",
                    totalDisplacements, hasMultiple);

            return response;

        } catch (DisplacementNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error obteniendo resumen de datos del trabajador: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo resumen de datos del trabajador", ex);
        }
    }

    private WorkerSummaryDTO buildWorkerSummaryHeader(DisplacementQueryRequest request, List<DisplacementNotificationDTO> displacements) {
        WorkerSummaryDTO.WorkerSummaryDTOBuilder builder = WorkerSummaryDTO.builder()
                .documentType(request.getWorkerIdentificationType())
                .documentNumber(request.getWorkerIdentificationNumber());


        try {
            Affiliate worker = findWorkerAffiliate(request.getWorkerIdentificationType(), request.getWorkerIdentificationNumber());
            if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(worker.getAffiliationType())) {
                enrichSummaryFromIndependent(worker, builder);
            } else {
                enrichSummaryFromDependent(worker, builder);
            }
        } catch (Exception ex) {
            log.warn("No se pudo construir cabecera completa del trabajador: {}", ex.getMessage());
        }

        // derive full name, affiliation type, display name from first displacement as fallback
        String fullName = displacements.isEmpty() ? null : displacements.get(0).getWorkerFullName();
        String affiliationType = displacements.isEmpty() ? null : displacements.get(0).getWorkerAffiliationType();
        builder
                .fullName(fullName)
                .affiliationType(affiliationType)
                .displayName(String.format("%s - %s (%s)",
                        request.getWorkerIdentificationType(),
                        request.getWorkerIdentificationNumber(),
                        fullName != null ? fullName : ""));

        return builder.build();
    }

    private void enrichSummaryFromIndependent(Affiliate worker, WorkerSummaryDTO.WorkerSummaryDTOBuilder builder) {
        Optional<Affiliation> affOpt = affiliationRepository.findByFiledNumber(worker.getFiledNumber());
        if (affOpt.isEmpty()) {
            return;
        }
        Affiliation aff = affOpt.get();
        String computedAddress = buildCompleteAddress(aff);
        builder
                .firstName(aff.getFirstName())
                .secondName(aff.getSecondName())
                .firstSurname(aff.getSurname())
                .secondSurname(aff.getSecondSurname())
                .birthDate(aff.getDateOfBirth() != null ? aff.getDateOfBirth().toString() : null)
                .age(calculateAge(aff.getDateOfBirth()))
                .gender(aff.getGender())
                .nationality(aff.getNationality() != null ? aff.getNationality().toString() : null)
                .completeAddress(computedAddress)
                .eps(resolveEpsName(aff.getHealthPromotingEntity()))
                .afp(resolveAfpName(aff.getPensionFundAdministrator()))
                .phone1(aff.getPhone1())
                .phone2(aff.getPhone2())
                .email(aff.getEmail());
    }

    private void enrichSummaryFromDependent(Affiliate worker, WorkerSummaryDTO.WorkerSummaryDTOBuilder builder) {
        Optional<AffiliationDependent> affOpt = affiliationDependentRepository.findByFiledNumber(worker.getFiledNumber());
        if (affOpt.isEmpty()) {
            return;
        }
        AffiliationDependent aff = affOpt.get();
        String computedAddress = buildCompleteAddress(aff);
        builder
                .firstName(aff.getFirstName())
                .secondName(aff.getSecondName())
                .firstSurname(aff.getSurname())
                .secondSurname(aff.getSecondSurname())
                .birthDate(aff.getDateOfBirth() != null ? aff.getDateOfBirth().toString() : null)
                .age(calculateAge(aff.getDateOfBirth()))
                .gender(aff.getGender())
                .nationality(aff.getNationality() != null ? aff.getNationality().toString() : null)
                .completeAddress(computedAddress)
                .eps(resolveEpsName(aff.getHealthPromotingEntity()))
                .afp(resolveAfpName(aff.getPensionFundAdministrator()))
                .phone1(aff.getPhone1())
                .phone2(aff.getPhone2())
                .email(aff.getEmail());
    }

    private String resolveEpsName(Long epsId) {
        if (epsId == null) return null;
        try {
            return healthRepository.findById(epsId).map(Health::getNameEPS).orElse(null);
        } catch (Exception ignored) {
            // Repository failures should not break flow when resolving EPS name
            return null;
        }
    }

    private String resolveAfpName(Long afpId) {
        if (afpId == null) return null;
        try {
            return fundPensionRepository.findById(afpId).map(FundPension::getNameAfp).orElse(null);
        } catch (Exception ignored) {
            // Repository failures should not break flow when resolving AFP name
            return null;
        }
    }

    @Override
    public List<DisplacementNotificationDTO> getEmployerDisplacements() {
        log.info("Obteniendo todos los desplazamientos para empleador autenticado");

        try {
            Affiliate employer = findEmployer();
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .byEmployerNit(employer.getNitCompany())
                            .and(WorkerDisplacementNotificationSpecification.withFetchJoins());

            List<WorkerDisplacementNotification> displacements = displacementRepository
                    .findAll(spec, Sort.by(Sort.Direction.DESC, SORT_FIELD_START_DATE));

            log.info("Encontrados {} desplazamientos para el empleador", displacements.size());

            return displacements.stream()
                    .map(this::mapToDTO)
                    .toList();

        } catch (Exception ex) {
            log.error("Error obteniendo desplazamientos del empleador: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo desplazamientos del empleador", ex);
        }
    }

    private int countActiveRelationships(String workerDocType, String workerDocNumber, String employerNit) {
        log.debug("Contando relaciones activas para trabajador: {} - {} con empleador NIT: {}", 
                workerDocType, workerDocNumber, employerNit);
        
        // Query DB directly for count of active, non-employer affiliations for this worker and employer
        int count = (int) affiliateRepository
                .countActiveNonEmployerByEmployerAndWorker(employerNit, workerDocType, workerDocNumber);
        
        log.debug("Encontradas {} relaciones activas", count);
        return count;
    }

    @Override
    public DisplacementNotificationDTO createDisplacement(CreateDisplacementRequest request) {
        log.info("Creando desplazamiento para trabajador: {} - {} (Empleador inferido)", 
                request.getWorkerDocumentType(), request.getWorkerDocumentNumber());

        try {
            // Validate request
            validateCreateRequest(request);
            
            // Find worker and employer affiliates (employer inferred from current user)
            Affiliate workerAffiliate = findWorkerAffiliate(request.getWorkerDocumentType(), request.getWorkerDocumentNumber());
            Affiliate employerAffiliate = findEmployerAffiliate();

            // Enforce risk rule for independent workers: only risk 4 or 5 allowed
            if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(workerAffiliate.getAffiliationType())) {
                Integer workerRisk = resolveIndependentWorkerRisk(workerAffiliate.getFiledNumber());
                if (workerRisk == null || (workerRisk != 4 && workerRisk != 5)) {
                    throw new DisplacementValidationException("Solo se permite notificar desplazamientos para trabajadores independientes con clase de riesgo 4 o 5");
                }
            }
            
            // Validate date overlaps
            if (!validateDisplacementDates(request.getWorkerDocumentType(), request.getWorkerDocumentNumber(), 
                    employerAffiliate.getNitCompany(), request.getDisplacementStartDate(), 
                    request.getDisplacementEndDate(), null)) {
                throw new DisplacementConflictException(
                        "El trabajador tiene un registro que coincide con los datos registrados, por favor valida de nuevo.");
            }
            
            // Find department and municipality (lookups only, validation is handled externally)
            Department department = findDepartmentById(request.getDisplacementDepartmentId());
            Municipality municipality = findMunicipalityById(request.getDisplacementMunicipalityId());
            
            // Generate filed number using centralized service with standardized SOL_SND prefix
            String filedNumber = filedService.getNextFiledNumberWorkerDisplacement();
            
            // Create displacement entity
            WorkerDisplacementNotification displacement = WorkerDisplacementNotification.builder()
                    .filedNumber(filedNumber)
                    .workerAffiliate(workerAffiliate)
                    .employerAffiliate(employerAffiliate)
                    .displacementDepartment(department)
                    .displacementMunicipality(municipality)
                    .displacementStartDate(request.getDisplacementStartDate())
                    .displacementEndDate(request.getDisplacementEndDate())
                    .displacementReason(request.getDisplacementReason())
                    .createdByUserId(resolveCurrentUserId())
                    .build();

            // Business status on creation: REGISTRADO when startDate is in the future
            if (request.getDisplacementStartDate() != null && request.getDisplacementStartDate().isAfter(LocalDate.now())) {
                displacement.setStatus(STATUS_REGISTRADO);
            }
            
            // Save displacement
            WorkerDisplacementNotification saved = displacementRepository.save(displacement);
            
            log.info("Desplazamiento creado exitosamente con radicado: {}", filedNumber);

            // Upsert novelty for HU #265040 visibility
            upsertGeneralNoveltyForDisplacement(saved);
            
            return mapToDTO(saved);
            
        } catch (DisplacementValidationException ex) {
            log.error("Error de validación creando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (DisplacementConflictException ex) {
            // Conflict (409): overlapping dates
            log.error("Conflicto creando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (DisplacementNotFoundException ex) {
            // Let 404 bubble to the global handler (e.g., municipio/departamento no encontrado)
            log.error("Recurso no encontrado creando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado creando desplazamiento: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error creando desplazamiento", ex);
        }
    }

    @Override
    public DisplacementNotificationDTO updateDisplacement(UpdateDisplacementRequest request) {
        log.info("Actualizando desplazamiento ID: {}", request.getId());

        try {
            // Find existing displacement
            WorkerDisplacementNotification existing = displacementRepository.findById(request.getId())
                    .orElseThrow(() -> new DisplacementNotFoundException(ERR_DISPLACEMENT_NOT_FOUND));
            
            // Validate business rules
            if (!existing.canBeEdited()) {
                throw new DisplacementValidationException("El desplazamiento no es editable: lifecycle inactivo, fecha fin > hoy o estado CULMINADO");
            }
            
            // Validate request
            validateUpdateRequest(request);
            
            // Enforce risk rule for independent workers on update as well
            Affiliate workerAffiliate = existing.getWorkerAffiliate();
            if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(workerAffiliate.getAffiliationType())) {
                Integer workerRisk = resolveIndependentWorkerRisk(workerAffiliate.getFiledNumber());
                if (workerRisk == null || (workerRisk != 4 && workerRisk != 5)) {
                    throw new DisplacementValidationException("Solo se permite notificar desplazamientos para trabajadores independientes con clase de riesgo 4 o 5");
                }
            }

            // Validate date overlaps (excluding current record)
            String employerNit = existing.getEmployerAffiliate().getNitCompany();
            String workerDocType = existing.getWorkerAffiliate().getDocumentType();
            String workerDocNumber = existing.getWorkerAffiliate().getDocumentNumber();
            
            if (!validateDisplacementDates(workerDocType, workerDocNumber, employerNit, 
                    request.getDisplacementStartDate(), request.getDisplacementEndDate(), request.getId())) {
                throw new DisplacementConflictException(
                        "El trabajador tiene un registro que coincide con los datos registrados, por favor valida de nuevo.");
            }
            
            // Find department and municipality (lookups only, validation is handled externally)
            Department department = findDepartmentById(request.getDisplacementDepartmentId());
            Municipality municipality = findMunicipalityById(request.getDisplacementMunicipalityId());
            
            // Early termination if new end date is earlier than initially captured end date
            if (existing.getInitialEndDate() != null &&
                request.getDisplacementEndDate() != null &&
                request.getDisplacementEndDate().isBefore(existing.getInitialEndDate())) {
                existing.markAsEarlyTerminated(request.getDisplacementEndDate(), resolveCurrentUserId());
                // Also allow updating location/reason metadata
                existing.setDisplacementDepartment(department);
                existing.setDisplacementMunicipality(municipality);
                existing.setDisplacementReason(request.getDisplacementReason());
            } else {
                // Normal update path
                existing.setDisplacementStartDate(request.getDisplacementStartDate());
                existing.setDisplacementEndDate(request.getDisplacementEndDate());
                existing.setDisplacementDepartment(department);
                existing.setDisplacementMunicipality(municipality);
                existing.setDisplacementReason(request.getDisplacementReason());
                existing.setUpdatedByUserId(resolveCurrentUserId());
                existing.setUpdatedDate(LocalDateTime.now());
            }
            
            // Save updated displacement
            WorkerDisplacementNotification updated = displacementRepository.save(existing);
            
            log.info("Desplazamiento actualizado exitosamente");

            // Upsert novelty for HU #265040 visibility
            upsertGeneralNoveltyForDisplacement(updated);
            
            return mapToDTO(updated);
            
        } catch (DisplacementValidationException ex) {
            log.error("Error de validación actualizando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (DisplacementConflictException ex) {
            // Conflict (409): overlapping dates
            log.error("Conflicto actualizando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (DisplacementNotFoundException ex) {
            // Propagate 404 (e.g., municipio/departamento no encontrado)
            log.error("Recurso no encontrado actualizando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado actualizando desplazamiento: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error actualizando desplazamiento", ex);
        }
    }

    @Override
    public void inactivateDisplacement(Long displacementId) {
        log.info("Inactivando desplazamiento ID: {}", displacementId);

        try {
            WorkerDisplacementNotification displacement = displacementRepository.findById(displacementId)
                    .orElseThrow(() -> new DisplacementNotFoundException(ERR_DISPLACEMENT_NOT_FOUND));
            
            LocalDate today = LocalDate.now();
            if (!displacement.isDeletable(today)) {
                String reason = buildInactivationRuleExplanation(displacement, today);
                log.warn("Reglas de inactivación no cumplidas para ID {}: {}", displacementId, reason);
                throw new DisplacementValidationException("El desplazamiento no puede ser inactivado: " + reason);
            }

            displacement.softDelete(resolveCurrentUserId());
            
            displacementRepository.save(displacement);
            
            log.info("Desplazamiento inactivado exitosamente. ID={}, lifecycleStatus={}, status={}",
                    displacementId, displacement.getLifecycleStatus(), displacement.getStatus());

            // Upsert novelty for HU #265040 visibility
            upsertGeneralNoveltyForDisplacement(displacement);
            
        } catch (DisplacementValidationException ex) {
            log.error("Error de validación inactivando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (DisplacementNotFoundException ex) {
            // Propagate 404
            log.error("Recurso no encontrado inactivando desplazamiento: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado inactivando desplazamiento: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error inactivando desplazamiento", ex);
        }
    }

    private String buildInactivationRuleExplanation(WorkerDisplacementNotification d, LocalDate today) {
        StringBuilder reasons = new StringBuilder();
        if (!LIFECYCLE_ACTIVE.equals(d.getLifecycleStatus())) {
            appendReason(reasons, "lifecycle no activo (" + d.getLifecycleStatus() + ")");
        }
        String business = d.deriveBusinessStatus(today);
        if (STATUS_CULMINADO.equals(business) || STATUS_TERMINADO.equals(business)) {
            appendReason(reasons, "estado final (" + business + ")");
        }
        appendReason(reasons, String.format("rango %s..%s, hoy=%s",
                String.valueOf(d.getDisplacementStartDate()),
                String.valueOf(d.getDisplacementEndDate()),
                String.valueOf(today)));
        return reasons.toString();
    }

    private void appendReason(StringBuilder sb, String reason) {
        if (!sb.isEmpty()) sb.append("; ");
        sb.append(reason);
    }

    @Override
    public DisplacementNotificationDTO getDisplacementById(Long displacementId) {
        log.info("Obteniendo desplazamiento por ID: {}", displacementId);

        try {
            WorkerDisplacementNotification displacement = displacementRepository.findById(displacementId)
                    .orElseThrow(() -> new DisplacementNotFoundException(ERR_DISPLACEMENT_NOT_FOUND));
            
            return mapToDTO(displacement);
            
        } catch (DisplacementNotFoundException ex) {
            // Propagate 404
            log.error("Recurso no encontrado consultando desplazamiento por ID: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error obteniendo desplazamiento por ID: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo desplazamiento", ex);
        }
    }

    @Override
    public DisplacementNotificationDTO getDisplacementByFiledNumber(String filedNumber) {
        log.info("Obteniendo desplazamiento por radicado: {}", filedNumber);

        try {
            WorkerDisplacementNotification displacement = displacementRepository.findByFiledNumber(filedNumber)
                    .orElseThrow(() -> new DisplacementNotFoundException(ERR_DISPLACEMENT_NOT_FOUND));
            
            return mapToDTO(displacement);
            
        } catch (DisplacementNotFoundException ex) {
            // Propagate 404
            log.error("Recurso no encontrado consultando desplazamiento por radicado: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error obteniendo desplazamiento por radicado: {}", ex.getMessage(), ex);
            throw new DisplacementOperationException("Error obteniendo desplazamiento", ex);
        }
    }

    @Override
    public boolean validateDisplacementDates(String workerDocType, String workerDocNumber, String employerNit, 
                                           LocalDate startDate, LocalDate endDate, Long excludeId) {
        log.debug("Validando fechas de desplazamiento para trabajador: {} - {}", workerDocType, workerDocNumber);

        try {
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .byWorker(workerDocType, workerDocNumber)
                            .and(WorkerDisplacementNotificationSpecification.byEmployerNit(employerNit))
                            .and(WorkerDisplacementNotificationSpecification.active())
                            .and(WorkerDisplacementNotificationSpecification.excludeId(excludeId))
                            .and(WorkerDisplacementNotificationSpecification.overlapping(startDate, endDate));

            boolean exists = displacementRepository.count(spec) > 0;
            boolean isValid = !exists;
            log.debug("Validación de fechas: {} (solapamientos encontrados: {})", 
                    isValid ? "VÁLIDA" : "INVÁLIDA", exists ? 1 : 0);
            
            return isValid;
            
        } catch (Exception ex) {
            log.error("Error validando fechas de desplazamiento: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String generateFiledNumber() {
        // Delegate to centralized consecutive/filing number service for consistency
        return filedService.getNextFiledNumberWorkerDisplacement();
    }

    @Override
    public int autoInactivateExpiredDisplacements() {
        log.info("Iniciando finalización automática de desplazamientos vencidos");

        try {
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .active()
                            .and(WorkerDisplacementNotificationSpecification.endDateBefore(LocalDate.now()));

            List<WorkerDisplacementNotification> expired = displacementRepository.findAll(spec);
            
            int count = 0;
            for (WorkerDisplacementNotification displacement : expired) {
                if (displacement.shouldBeAutoInactivated()) {
                    displacement.markAsAutoFinalized(LocalDateTime.now(), null);
                    displacementRepository.save(displacement);
                    // Upsert novelty for HU #265040 visibility
                    upsertGeneralNoveltyForDisplacement(displacement);
                    count++;
                }
            }
            
            log.info("Finalizados automáticamente {} desplazamientos (Culminado)", count);
            
            return count;
            
        } catch (Exception ex) {
            log.error("Error en inactivación automática: {}", ex.getMessage(), ex);
            return 0;
        }
    }

    // New job: persist EN_CURSO where today is within range and record is not final
    public int autoMarkInProgressDisplacements() {
        log.info("Iniciando marcado automático de desplazamientos EN_CURSO");

        try {
            LocalDate today = LocalDate.now();
            Specification<WorkerDisplacementNotification> spec =
                    WorkerDisplacementNotificationSpecification
                            .active()
                            .and(WorkerDisplacementNotificationSpecification.notFinalized())
                            .and(WorkerDisplacementNotificationSpecification.currentDateWithinRange(today));

            List<WorkerDisplacementNotification> inProgress = displacementRepository.findAll(spec);

            int count = 0;
            for (WorkerDisplacementNotification d : inProgress) {
                // Only update if not already EN_CURSO
                String current = d.getStatus();
                String desired = d.deriveBusinessStatus(today);
                if (!STATUS_EN_CURSO.equals(current) && STATUS_EN_CURSO.equals(desired)) {
                    d.setStatus(STATUS_EN_CURSO);
                    displacementRepository.save(d);
                    // Upsert novelty for HU #265040 visibility
                    upsertGeneralNoveltyForDisplacement(d);
                    count++;
                }
            }

            log.info("Marcados EN_CURSO {} desplazamientos", count);
            return count;

        } catch (Exception ex) {
            log.error("Error marcando EN_CURSO: {}", ex.getMessage(), ex);
            return 0;
        }
    }

    private DisplacementNotificationDTO mapToDTO(WorkerDisplacementNotification entity) {
        Long departmentId = null;
        if (entity.getDisplacementDepartment() != null && entity.getDisplacementDepartment().getIdDepartment() != null) {
            departmentId = entity.getDisplacementDepartment().getIdDepartment().longValue();
        }

        Long municipalityId = null;
        if (entity.getDisplacementMunicipality() != null) {
            municipalityId = entity.getDisplacementMunicipality().getIdMunicipality();
        }

        return DisplacementNotificationDTO.builder()
                .id(entity.getId())
                .filedNumber(entity.getFiledNumber())
                .workerDocumentType(entity.getWorkerAffiliate().getDocumentType())
                .workerDocumentNumber(entity.getWorkerAffiliate().getDocumentNumber())
                .workerFullName(buildWorkerFullName(entity.getWorkerAffiliate()))
                .workerAffiliationType(entity.getWorkerAffiliate().getAffiliationType())
                .workerJobPosition(entity.getWorkerAffiliate().getPosition())
                .employerDocumentType(entity.getEmployerAffiliate().getDocumentType())
                .employerDocumentNumber(entity.getEmployerAffiliate().getNitCompany())
                .contractorCompanyName(entity.getEmployerAffiliate().getCompany())
                .displacementStartDate(entity.getDisplacementStartDate())
                .displacementEndDate(entity.getDisplacementEndDate())
                .displacementDepartmentCode(entity.getDisplacementDepartment() != null ? 
                        entity.getDisplacementDepartment().getDepartmentCode() : null)
                .displacementDepartmentName(entity.getDisplacementDepartment() != null ? 
                        entity.getDisplacementDepartment().getDepartmentName() : null)
                .displacementDepartmentId(departmentId)
                .displacementMunicipalityCode(entity.getDisplacementMunicipality() != null ? 
                        entity.getDisplacementMunicipality().getMunicipalityCode() : null)
                .displacementMunicipalityName(entity.getDisplacementMunicipality() != null ? 
                        entity.getDisplacementMunicipality().getMunicipalityName() : null)
                .displacementMunicipalityId(municipalityId)
                .displacementReason(entity.getDisplacementReason())
                .status(entity.deriveBusinessStatus(LocalDate.now()))
                .lifecycleStatus(entity.getLifecycleStatus())
                .canBeEdited(entity.canBeEdited())
                .canBeInactivated(entity.canBeInactivated())
                .createdDate(entity.getCreatedDate())
                .createdByUserId(entity.getCreatedByUserId())
                .updatedDate(entity.getUpdatedDate())
                .updatedByUserId(entity.getUpdatedByUserId())
                .build();
    }

    private void upsertGeneralNoveltyForDisplacement(WorkerDisplacementNotification d) {
        try {
            String status = resolveNoveltyStatus(d);

            SaveGeneralNoveltyRequest req = SaveGeneralNoveltyRequest.builder()
                    .idAffiliation(d.getWorkerAffiliate().getIdAffiliate())
                    .filedNumber(d.getFiledNumber())
                    .noveltyType(Constant.NOVELTY_TYPE_WORKER_DISPLACEMENT)
                    .status(status)
                    .observation(buildNoveltyObservation(d))
                    .build();

            generalNoveltyService.saveGeneralNovelty(req);
        } catch (Exception ex) {
            log.warn("No se pudo registrar novedad general para desplazamiento {}: {}", d.getFiledNumber(), ex.getMessage());
        }
    }

    private String resolveNoveltyStatus(WorkerDisplacementNotification d) {
        try {
            return d.deriveBusinessStatus(LocalDate.now());
        } catch (Exception ex) {
            return d.getStatus();
        }
    }

    private String buildNoveltyObservation(WorkerDisplacementNotification d) {
        String dept = d.getDisplacementDepartment() != null ? d.getDisplacementDepartment().getDepartmentName() : null;
        String muni = d.getDisplacementMunicipality() != null ? d.getDisplacementMunicipality().getMunicipalityName() : null;
        String destino;
        if (dept != null || muni != null) {
            StringBuilder sb = new StringBuilder();
            if (dept != null) sb.append(dept);
            if (dept != null && muni != null) sb.append(" - ");
            if (muni != null) sb.append(muni);
            destino = sb.toString();
        } else {
            destino = "Destino no informado";
        }

        String motivo = d.getDisplacementReason() != null ? d.getDisplacementReason() : "Sin motivo";
        return String.format("Desplazamiento %s → %s a %s. Motivo: %s",
                d.getDisplacementStartDate(),
                d.getDisplacementEndDate(),
                destino,
                motivo);
    }

    private String buildWorkerFullName(Affiliate workerAffiliate) {
        try {
            return fetchWorkerDetails(workerAffiliate)
                    .map(WorkerDetails::fullName)
                    .orElse(NAME_NOT_AVAILABLE);
        } catch (Exception ex) {
            log.warn("Error obteniendo nombre completo del trabajador: {}", ex.getMessage());
            return NAME_NOT_AVAILABLE;
        }
    }

    private Integer resolveIndependentWorkerRisk(String filedNumber) {
        if (filedNumber == null) return null;
        try {
            Optional<Affiliation> affOpt = affiliationRepository.findByFiledNumber(filedNumber);
            if (affOpt.isPresent()) {
                return parseRiskAsInteger(affOpt.get().getRisk());
            }
        } catch (Exception ignored) {
            // Intentionally ignore: absence of risk data should not fail the flow
        }
        return null;
    }

    private Integer parseRiskAsInteger(String riskStr) {
        if (riskStr == null || riskStr.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(riskStr.trim());
        } catch (NumberFormatException ex) {
            // Invalid numeric format: treat as unknown risk
            return null;
        }
    }
    private void validateCreateRequest(CreateDisplacementRequest request) {
        if (request == null) {
            throw new DisplacementValidationException(ERR_REQUEST_NULL);
        }
        if (!request.isDateRangeValid()) {
            throw new DisplacementValidationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        LocalDate minAllowed = LocalDate.now().minusDays(90);
        if (request.getDisplacementStartDate().isBefore(minAllowed)) {
            throw new DisplacementValidationException("La fecha de inicio no puede ser menor a 90 días atrás");
        }
        if (request.getDisplacementEndDate().isBefore(minAllowed)) {
            throw new DisplacementValidationException("La fecha de fin no puede ser menor a 90 días atrás");
        }
    }

    private void validateUpdateRequest(UpdateDisplacementRequest request) {
        if (request == null) {
            throw new DisplacementValidationException(ERR_REQUEST_NULL);
        }
        if (!request.isDateRangeValid()) {
            throw new DisplacementValidationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        LocalDate minAllowed = LocalDate.now().minusDays(90);
        if (request.getDisplacementStartDate().isBefore(minAllowed)) {
            throw new DisplacementValidationException("La fecha de inicio no puede ser menor a 90 días atrás");
        }
        if (request.getDisplacementEndDate().isBefore(minAllowed)) {
            throw new DisplacementValidationException("La fecha de fin no puede ser menor a 90 días atrás");
        }
    }

    private void validateRequest(DisplacementQueryRequest request) {
        log.debug("=== VALIDATING REQUEST ===");
        log.debug("Request object: {}", request);

        if (request == null) {
            log.error("Request is null");
            throw new DisplacementValidationException(ERR_REQUEST_NULL);
        }

        if (request.getWorkerIdentificationType() == null || request.getWorkerIdentificationType().trim().isEmpty()) {
            log.error("Worker identification type is null or empty");
            throw new DisplacementValidationException("El tipo de identificación del trabajador es requerido");
        }

        if (request.getWorkerIdentificationNumber() == null || request.getWorkerIdentificationNumber().trim().isEmpty()) {
            log.error("Worker identification number is null or empty");
            throw new DisplacementValidationException("El número de identificación del trabajador es requerido");
        }

        log.debug("Request validation passed successfully");
    }

    private Affiliate findWorkerAffiliate(String docType, String docNumber) {
        return affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(docType, docNumber)
                .orElseThrow(() -> new DisplacementNotFoundException("Trabajador no encontrado"));
    }

    private Affiliate findEmployerAffiliate() {
        // Delegate to common employer resolution
        return findEmployer();
    }

    private Department findDepartmentById(Integer departmentId) {
        if (departmentId == null) {
            throw new DisplacementValidationException("Departamento no válido");
        }
        return departmentRepository.findByIdDepartment(departmentId)
                .orElseThrow(() -> new DisplacementNotFoundException("Departamento no encontrado"));
    }

    private Municipality findMunicipalityById(Long municipalityId) {
        if (municipalityId == null) {
            throw new DisplacementValidationException("Municipio no válido");
        }
        return municipalityRepository.findById(municipalityId)
                .orElseThrow(() -> new DisplacementNotFoundException("Municipio no encontrado"));
    }

    private Long resolveCurrentUserId() {
        CurrentUserContext current = resolveCurrentUserContext();
        return current != null ? current.userId() : null;
    }

    private CurrentUserContext resolveCurrentUserContext() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                return userPreRegisterRepository
                        .findByEmailIgnoreCase(email)
                        .map(u -> new CurrentUserContext(u.getId(), u.getIdentificationType(), u.getIdentification()))
                        .orElse(null);
            }
        } catch (Exception ignored) {
            // Intentionally ignore: if there is no authenticated principal we return null
        }
        return null;
    }

    // Dedicated exceptions to replace generic ones (Sonar S112)
    private static class DisplacementOperationException extends RuntimeException {
        DisplacementOperationException(String message, Throwable cause) { super(message, cause); }
    }

    // Use global exception class instead of inner class

    
    private record CurrentUserContext(Long userId, String documentType, String documentNumber) {}
    private record WorkerDetails(
            String firstName,
            String secondName,
            String firstSurname,
            String secondSurname,
            LocalDate birthDate,
            String gender,
            Object nationality,
            String address,
            Long epsId,
            Long afpId,
            String phone1,
            String phone2,
            String email
    ) {
        String fullName() {
            StringBuilder sb = new StringBuilder();
            if (firstName != null && !firstName.isBlank()) sb.append(firstName).append(' ');
            if (secondName != null && !secondName.isBlank()) sb.append(secondName).append(' ');
            if (firstSurname != null && !firstSurname.isBlank()) sb.append(firstSurname).append(' ');
            if (secondSurname != null && !secondSurname.isBlank()) sb.append(secondSurname);
            String result = sb.toString().trim();
            return result.isEmpty() ? NAME_NOT_AVAILABLE : result;
        }
    }
}
