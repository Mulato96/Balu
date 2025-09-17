package com.gal.afiliaciones.application.service.workerdisplacementnotification;

import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.workerdisplacementnotification.impl.WorkerDisplacementNotificationServiceImpl;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementNotFoundException;
import com.gal.afiliaciones.config.ex.workerdisplacement.DisplacementValidationException;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.workerdisplacementnotification.WorkerDisplacementNotification;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.workerdisplacementnotification.WorkerDisplacementNotificationRepository;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementListResponse;
import com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification.DisplacementQueryRequest;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        when(entity.getWorkerAffiliate()).thenReturn(worker);
        Affiliate emp = new Affiliate();
        emp.setNitCompany("900111222");
        emp.setCompany("ACME");
        when(entity.getEmployerAffiliate()).thenReturn(emp);
        when(displacementRepository.findAll(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(displacementRepository.count(ArgumentMatchers.<Specification<WorkerDisplacementNotification>>any())).thenReturn(1L);

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

        assertThrows(DisplacementNotFoundException.class, () ->
                service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null));
    }

    @Test
    void getWorkerDisplacementsWithRelationshipStatus_ShouldValidateRequest_WhenMissingFields() {
        // Validates request-level constraints: missing doc type/number should raise validation error.
        DisplacementQueryRequest request = new DisplacementQueryRequest();
        // Missing type and number
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getWorkerDisplacementsWithRelationshipStatus(request, 0, 10, null, null));
        assertTrue(ex.getCause() instanceof DisplacementValidationException);
    }
}


