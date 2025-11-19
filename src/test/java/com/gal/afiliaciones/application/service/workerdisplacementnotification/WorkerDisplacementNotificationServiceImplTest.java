package com.gal.afiliaciones.application.service.workerdisplacementnotification;

import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.GeneralNoveltyService;
import com.gal.afiliaciones.application.service.workerdisplacementnotification.impl.WorkerDisplacementNotificationServiceImpl;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementConflictException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementNotFoundException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementValidationException;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.workerdisplacementnotification.WorkerDisplacementNotification;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workerdisplacementnotification.WorkerDisplacementNotificationRepository;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.CreateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementListResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementNotificationDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementQueryRequest;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.UpdateDisplacementRequest;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.WorkerDataResponse;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class WorkerDisplacementNotificationServiceImplTest {

    @Mock AffiliateRepository affiliateRepository;
    @Mock AffiliationDependentRepository affiliationDependentRepository;
    @Mock IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock WorkerDisplacementNotificationRepository displacementRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock MunicipalityRepository municipalityRepository;
    @Mock HealthPromotingEntityRepository healthRepository;
    @Mock FundPensionRepository fundPensionRepository;
    @Mock FiledService filedService;
    @Mock IUserPreRegisterRepository userPreRegisterRepository;
    @Mock GeneralNoveltyService generalNoveltyService;

    @InjectMocks WorkerDisplacementNotificationServiceImpl service;

    @BeforeEach
    void setupSecurityContext() {
        // Arrange a Jwt principal in the SecurityContext so the service can resolve employer context
        // via IUserPreRegisterRepository and proceed with its business logic under test.
        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = Map.of("email", "user@example.com");
        Jwt jwt = new Jwt("token", null, null, headers, claims);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt, "n/a"));

        com.gal.afiliaciones.domain.model.UserMain mockUser = new com.gal.afiliaciones.domain.model.UserMain();
        mockUser.setId(123L);
        mockUser.setIdentificationType("CC");
        mockUser.setIdentification("1000001");
        when(userPreRegisterRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(mockUser));

        Affiliate employer = new Affiliate();
        employer.setIdAffiliate(1L);
        employer.setCompany("ACME");
        employer.setNitCompany("900111222");
        when(affiliateRepository.findOne(ArgumentMatchers.<Specification<Affiliate>>any())).thenReturn(Optional.of(employer));
    }

    @Test
    void validateDisplacementDates_ShouldReturnFalse_WhenOverlapExists() {
        // Verifies the overlap rule: when repository reports any matching active record,
        // the service must return false (invalid date range).
        when(displacementRepository.count(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any()))
                .thenReturn(1L);

        boolean valid = service.validateDisplacementDates("CC", "123", "900", LocalDate.now(), LocalDate.now().plusDays(1), null);
        assertFalse(valid);
    }

    @Test
    void listWithRelationshipStatus_ShouldReturnResponse_WhenMultipleRelationships() {
        // Ensures list API returns data and sets hasMultipleActiveRelationships flag
        // when the repository count for active non-employer relationships is > 1.
        AffiliationDependent affiliation = new AffiliationDependent();
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        request.setWorkerIdentificationType("CC");
        request.setWorkerIdentificationNumber("12345");

        when(affiliateRepository.countActiveNonEmployerByEmployerAndWorker(anyString(), anyString(), anyString()))
                .thenReturn(2L); // multiple

        WorkerDisplacementNotification entity = mock(WorkerDisplacementNotification.class);
        Affiliate worker = new Affiliate();
        worker.setDocumentType("CC");
        worker.setDocumentNumber("12345");
        worker.setAffiliationType("DEPENDENT");
        worker.setFiledNumber("123");
        when(entity.getWorkerAffiliate()).thenReturn(worker);
        Affiliate emp = new Affiliate();
        emp.setNitCompany("900111222");
        emp.setCompany("ACME");
        when(entity.getEmployerAffiliate()).thenReturn(emp);
        when(displacementRepository.findAll(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(displacementRepository.count(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any())).thenReturn(1L);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(worker));
        when(affiliationDependentRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliation));

        DisplacementListResponse response = service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null);

        assertNotNull(response);
        assertTrue(response.isHasMultipleActiveRelationships());
        assertEquals(1, response.getTotalDisplacements());
        assertEquals(1, response.getActiveDisplacements());
        assertEquals(0, response.getInactiveDisplacements());
    }

    @Test
    void listWithRelationshipStatus_ShouldReturnResponse_WhenMultipleRelationships__() {
        // Ensures list API returns data and sets hasMultipleActiveRelationships flag
        // when the repository count for active non-employer relationships is > 1.
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        request.setWorkerIdentificationType("CC");
        request.setWorkerIdentificationNumber("12345");

        when(affiliateRepository.countActiveNonEmployerByEmployerAndWorker(anyString(), anyString(), anyString()))
                .thenReturn(2L); // multiple

        WorkerDisplacementNotification entity = mock(WorkerDisplacementNotification.class);
        Affiliate worker = new Affiliate();
        Affiliation affiliation = new Affiliation();
        Health health =  new Health();
        affiliation.setHealthPromotingEntity(3L);
        affiliation.setPensionFundAdministrator(3L);
        worker.setDocumentType("CC");
        worker.setDocumentNumber("12345");
        worker.setAffiliationType("Trabajador Independiente");
        worker.setFiledNumber("123");
        when(entity.getWorkerAffiliate()).thenReturn(worker);
        Affiliate emp = new Affiliate();
        emp.setNitCompany("900111222");
        emp.setCompany("ACME");
        when(entity.getEmployerAffiliate()).thenReturn(emp);
        when(displacementRepository.findAll(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(displacementRepository.count(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any())).thenReturn(1L);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(worker));
        when(affiliationRepository.findByFiledNumber(worker.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(healthRepository.findById(anyLong())).thenReturn(Optional.of(health));

        DisplacementListResponse response = service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null);

        assertNotNull(response);
        assertTrue(response.isHasMultipleActiveRelationships());
        assertEquals(1, response.getTotalDisplacements());
        assertEquals(1, response.getActiveDisplacements());
        assertEquals(0, response.getInactiveDisplacements());
    }

    @Test
    void listWithRelationshipStatus_ShouldThrowNotFound_WhenNoActiveRelationships() {
        // When there are no active relationships for the worker with the current employer,
        // the service should signal not-found to align with HU behavior.
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        request.setWorkerIdentificationType("CC");
        request.setWorkerIdentificationNumber("12345");

        when(affiliateRepository.countActiveNonEmployerByEmployerAndWorker(anyString(), anyString(), anyString()))
                .thenReturn(0L);
        when(displacementRepository.findAll(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(Exception.class, () ->
                service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null));
    }

    @Test
    void getWorkerDisplacementsWithRelationshipStatus_ShouldValidateRequest_WhenMissingFields() {
        // Validates request-level constraints: missing doc type/number should raise validation error.
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        // Missing type and number
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null));
        assertInstanceOf(DisplacementValidationException.class, ex.getCause());
    }

    @Test
    void getWorkerDisplacements_(){

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));

        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);

        List<DisplacementNotificationDTO> displacementNotificationDTOS = service.getWorkerDisplacements("", "");

        assertNotNull(displacementNotificationDTOS);

    }

    @Test
    void getWorkerDataSummary_NumberNull(){
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        request.setWorkerIdentificationType("CC");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getWorkerDataSummary(request));
        assertInstanceOf(DisplacementValidationException.class, ex.getCause());

    }

    @Test
    void getWorkerDataSummary(){
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        request.setWorkerIdentificationType("CC");
        request.setWorkerIdentificationNumber("1234567890");

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);
        when(affiliateRepository.countActiveNonEmployerByEmployerAndWorker(any(), any(), anyString()))
                .thenReturn(3L);
        when(displacementRepository.count()).thenReturn(3L);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(affiliate));

        WorkerDataResponse workerDataResponse = service.getWorkerDataSummary(request);

        assertNotNull(workerDataResponse);

    }

    @Test
    void getEmployerDisplacements(){

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);

        List<DisplacementNotificationDTO> displacementNotificationDTOS = service.getEmployerDisplacements();

        assertNotNull(displacementNotificationDTOS);
    }

    @Test
    void autoMarkInProgressDisplacements(){

        List<WorkerDisplacementNotification> list = list();
        list.forEach(d -> {
            d.setStatus("EN_CURSO");
            d.deriveBusinessStatus(null);
            d.setDisplacementStartDate(LocalDate.now());
        });
        when(displacementRepository.findAll()).thenReturn(list);

        Integer result = service.autoMarkInProgressDisplacements();
        assertNotNull(result);
    }

    @Test
    void createDisplacement_UserNotFound(){

        CreateDisplacementRequest request = new CreateDisplacementRequest();
        request.setWorkerDocumentNumber("12345678900");
        request.setWorkerDocumentType("CC");
        request.setDisplacementStartDate(LocalDate.now());
        request.setDisplacementEndDate(LocalDate.now().plusDays(1));

        DisplacementNotFoundException ex = assertThrows(
                DisplacementNotFoundException.class,
                () -> service.createDisplacement(request)
        );

        assertEquals("Trabajador no encontrado", ex.getMessage());

    }

    @Test
    void createDisplacement_Exception(){

        CreateDisplacementRequest request = new CreateDisplacementRequest();
        request.setWorkerDocumentNumber("12345678900");
        request.setWorkerDocumentType("CC");
        request.setDisplacementStartDate(LocalDate.now());
        request.setDisplacementEndDate(LocalDate.now().plusDays(1));

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Affiliation affiliation = new Affiliation();
        affiliate.setFiledNumber("123456789");

        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setRisk("123");
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(affiliate));
        when( affiliationRepository.findByFiledNumber("123456789")).thenReturn(Optional.of(affiliation));

        DisplacementValidationException ex = assertThrows(
                DisplacementValidationException.class,
                () -> service.createDisplacement(request)
        );

        assertEquals("Solo se permite notificar desplazamientos para trabajadores independientes con clase de riesgo 4 o 5", ex.getMessage());

    }

    @Test
    void createDisplacement_Exception_(){

        CreateDisplacementRequest request = new CreateDisplacementRequest();
        request.setWorkerDocumentNumber("12345678900");
        request.setWorkerDocumentType("CC");
        request.setDisplacementStartDate(LocalDate.now());
        request.setDisplacementEndDate(LocalDate.now().plusDays(1));

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(affiliate));
        when(displacementRepository.count((Specification<WorkerDisplacementNotification>)  any())).thenReturn(3L);

        DisplacementConflictException ex = assertThrows(
                DisplacementConflictException.class,
                () -> service.createDisplacement(request)
        );

        assertEquals("El trabajador tiene un registro que coincide con los datos registrados, por favor valida de nuevo.", ex.getMessage());
    }

    @Test
    void createDisplacement(){

        CreateDisplacementRequest request = new CreateDisplacementRequest();
        request.setWorkerDocumentNumber("12345678900");
        request.setWorkerDocumentType("CC");
        request.setDisplacementStartDate(LocalDate.now());
        request.setDisplacementEndDate(LocalDate.now().plusDays(1));
        request.setDisplacementDepartmentId(2);
        request.setDisplacementMunicipalityId(2L);

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        Department department = new Department();
        Municipality municipality = new Municipality();
        department.setDepartmentName("Nombre");
        municipality.setMunicipalityName("Nombre");
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);
        workerDisplacementNotification.setDisplacementDepartment(department);
        workerDisplacementNotification.setDisplacementMunicipality(municipality);

        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(affiliate));
        when(displacementRepository.count((Specification<WorkerDisplacementNotification>)  any())).thenReturn(0L);
        when(departmentRepository.findByIdDepartment(any())).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(anyLong())).thenReturn(Optional.of(municipality));
        when(filedService.getNextFiledNumberWorkerDisplacement()).thenReturn("123");
        when(displacementRepository.save(any())).thenReturn(workerDisplacementNotification);
        doNothing()
                .when(generalNoveltyService)
                .saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));


        DisplacementNotificationDTO response = service.createDisplacement(request);

        assertNotNull(response);

    }

    @Test
    void updateDisplacement(){

        UpdateDisplacementRequest request = new UpdateDisplacementRequest();
        request.setId(1L);
        request.setDisplacementStartDate(LocalDate.now());
        request.setDisplacementEndDate(LocalDate.now().plusDays(1));
        request.setDisplacementDepartmentId(2);
        request.setDisplacementMunicipalityId(2L);

        UserMain user = new UserMain();
        Affiliate affiliate = new Affiliate();
        Optional<Affiliate> optional = Optional.of(affiliate);
        List<WorkerDisplacementNotification> list = list();

        Department department = new Department();
        Municipality municipality = new Municipality();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findById(request.getId())).thenReturn(Optional.of(workerDisplacementNotification));
        when(userPreRegisterRepository.findByEmailIgnoreCase("usuario@test.com"))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findOne((Example<Affiliate>) any())).thenReturn(optional);
        when(displacementRepository.findAll()).thenReturn(list);
        when(affiliateRepository.findFirstByDocumentTypeAndDocumentNumber(any(), any())).thenReturn(Optional.of(affiliate));
        when(displacementRepository.count((Specification<WorkerDisplacementNotification>)  any())).thenReturn(0L);
        when(departmentRepository.findByIdDepartment(any())).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(anyLong())).thenReturn(Optional.of(municipality));
        when(filedService.getNextFiledNumberWorkerDisplacement()).thenReturn("123");
        when(displacementRepository.save(any())).thenReturn(workerDisplacementNotification);
        doNothing()
                .when(generalNoveltyService)
                .saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));


        DisplacementNotificationDTO response =  service.updateDisplacement(request);

        assertNotNull(response);
    }

    @Test
    void getDisplacementById(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findById(anyLong())).thenReturn(Optional.of(workerDisplacementNotification));

        DisplacementNotificationDTO displacementNotificationDTO = service.getDisplacementById(1L);
        assertNotNull(displacementNotificationDTO);
    }

    @Test
    void getDisplacementById_Exception(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findById(anyLong())).thenThrow( new DisplacementNotFoundException("Desplazamiento no encontrado"));

        DisplacementNotFoundException ex = assertThrows(
                DisplacementNotFoundException.class,
                () -> service.getDisplacementById(1L)
        );

        assertEquals("Desplazamiento no encontrado", ex.getMessage());
    }

    @Test
    void getDisplacementByFiledNumber(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(workerDisplacementNotification));

        DisplacementNotificationDTO displacementNotificationDTO = service.getDisplacementByFiledNumber("");
        assertNotNull(displacementNotificationDTO);
    }

    @Test
    void getDisplacementByFiledNumber_Exception(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findByFiledNumber(anyString())).thenThrow( new DisplacementNotFoundException("Desplazamiento no encontrado"));

        DisplacementNotFoundException ex = assertThrows(
                DisplacementNotFoundException.class,
                () -> service.getDisplacementByFiledNumber("")
        );

        assertEquals("Desplazamiento no encontrado", ex.getMessage());
    }

    @Test
    void generateFiledNumber(){

        String response = "response";

        when(filedService.getNextFiledNumberWorkerDisplacement()).thenReturn(response);

        String r = service.generateFiledNumber();

        assertEquals(r, response);
    }

    @Test
    void autoInactivateExpiredDisplacements(){

        List<WorkerDisplacementNotification> list = list();

        list.forEach(d ->{
            d.setLifecycleStatus("ACTIVO");
            d.setDisplacementEndDate(LocalDate.now());
            d.setDisplacementStartDate(LocalDate.now().minusDays(1));
        });

        when(displacementRepository.findAll((Specification<WorkerDisplacementNotification>) any())).thenReturn(list);

        Integer response = service.autoInactivateExpiredDisplacements();

        assertNotNull(response);
    }

    @Test
    void inactivateDisplacement(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);

        when(displacementRepository.findById(anyLong())).thenReturn(Optional.of(workerDisplacementNotification));
        doNothing()
                .when(generalNoveltyService)
                .saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));

        service.inactivateDisplacement(1L);

        verify(displacementRepository, times(1)).findById(1L);
    }

    @Test
    void inactivateDisplacement_Exception(){

        Affiliate affiliate = new Affiliate();
        WorkerDisplacementNotification workerDisplacementNotification =  new WorkerDisplacementNotification();
        workerDisplacementNotification.setWorkerAffiliate(affiliate);
        workerDisplacementNotification.setEmployerAffiliate(affiliate);
        workerDisplacementNotification.setLifecycleStatus("OTRA");

        when(displacementRepository.findById(anyLong())).thenReturn(Optional.of(workerDisplacementNotification));

        DisplacementValidationException ex = assertThrows(
                DisplacementValidationException.class,
                () -> service.inactivateDisplacement(1L)
        );

        assertTrue(ex.getMessage().contains("El desplazamiento no puede ser inactivado:"));
    }

    List<WorkerDisplacementNotification> list(){

        WorkerDisplacementNotification workerDisplacementNotification1 = new WorkerDisplacementNotification();
        WorkerDisplacementNotification workerDisplacementNotification2 = new WorkerDisplacementNotification();
        WorkerDisplacementNotification workerDisplacementNotification3 = new WorkerDisplacementNotification();
        WorkerDisplacementNotification workerDisplacementNotification4 = new WorkerDisplacementNotification();


        return List.of(workerDisplacementNotification1, workerDisplacementNotification2, workerDisplacementNotification3, workerDisplacementNotification4);
    }
}


