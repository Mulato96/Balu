package com.gal.afiliaciones.application.service.updatedatesemployee.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.gal.afiliaciones.config.ex.DateUpdateException;
import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.ConsultLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeResponse;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation.ObservationsAffiliationRepository;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.UpdateCoverageDateDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionDetalleDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionQueryDTO;

import reactor.core.publisher.Mono;


@ExtendWith(MockitoExtension.class)
class DateUpdateServiceImplTest {

    @Mock AffiliateRepository affiliateRepository;
    @Mock AffiliationDependentRepository affiliationDependentRepository;
    @Mock ObservationsAffiliationRepository observationsRepository;
    @Mock ConsultEmployerClient consultEmployerClient;
    @Mock ConsultLegalRepresentativeClient consultLegalRepresentativeClient;

    @InjectMocks DateUpdateServiceImpl service;

    private Affiliate employer(Long id, String status, String nit, LocalDateTime affDate, LocalDate retire,
                               LocalDate covStart, String docTypeCompany, String company, String docNumber, String affiliationType) {
        Affiliate a = new Affiliate();
        a.setIdAffiliate(id);
        a.setAffiliationStatus(status);
        a.setNitCompany(nit);
        a.setAffiliationDate(affDate);
        a.setRetirementDate(retire);
        a.setCoverageStartDate(covStart);
        a.setDocumenTypeCompany(docTypeCompany);
        a.setCompany(company);
        a.setDocumentNumber(docNumber);
        a.setAffiliationType(affiliationType);
        a.setFiledNumber("F-" + id);
        return a;
    }

    private AffiliationDependent dependent(Long id, Long employerId, String docType, String doc, LocalDate start, LocalDate cov) {
        AffiliationDependent d = new AffiliationDependent();
        d.setId(id);
        d.setIdAffiliateEmployer(employerId);
        d.setIdentificationDocumentType(docType);
        d.setIdentificationDocumentNumber(doc);
        d.setStartDate(start);
        d.setCoverageDate(cov);
        d.setFirstName("Ana");
        d.setSecondName("María");
        d.setSurname("López");
        d.setSecondSurname("Pérez");
        d.setAddress("Calle 1 #2-3");
        d.setPhone1("111");
        d.setPhone2("222");
        d.setEmail("a@b.com");
        d.setFiledNumber("F-DEP-" + id);
        return d;
    }

    private void putJwt(String documentNumber, String officialId, boolean authenticated) {
        Map<String, Object> claims = new HashMap<>();
        if (documentNumber != null) claims.put("document_number", documentNumber);
        if (officialId != null) claims.put("official_id", officialId);
        Jwt jwt = new Jwt("tkn", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "none"), claims);
        Authentication auth = new AbstractAuthenticationToken(null) {
            @Override public Object getCredentials() { return "N/A"; }
            @Override public Object getPrincipal() { return jwt; }
        };
        auth.setAuthenticated(authenticated);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authenticated ? auth : null);
        SecurityContextHolder.setContext(sc);
    }

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("consultLinks - Lanza excepción cuando faltan campos requeridos")
    void consultLinks_throws_when_required_missing() {
        VinculacionQueryDTO q = new VinculacionQueryDTO();
        q.setTipoIdentificacion(null);
        q.setNumeroIdentificacion("123");

        assertThrows(DateUpdateException.class, () -> service.consultLinks(q));
    }

    @Test
    @DisplayName("consultLinks - Lanza excepción cuando no hay resultados")
    void consultLinks_throws_when_no_results() {
        VinculacionQueryDTO q = new VinculacionQueryDTO();
        q.setTipoIdentificacion("CC");
        q.setNumeroIdentificacion("1");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "1")).thenReturn(List.of());
        when(affiliationDependentRepository.findByMainAffiliateIdentification("CC", "1")).thenReturn(List.of());

        assertThrows(DateUpdateException.class, () -> service.consultLinks(q));
    }

    @Test
    @DisplayName("consultLinks - Mapea afiliados y dependientes y maneja NIT especial")
    void consultLinks_maps_affiliates_and_dependents_and_handles_special_NIT() {
        VinculacionQueryDTO q = new VinculacionQueryDTO();
        q.setTipoIdentificacion("CC");
        q.setNumeroIdentificacion("1");

        Affiliate emp = employer(10L, "ACTIVO", "899999061",
                LocalDateTime.now().minusDays(100), null, LocalDate.now().minusDays(50),
                "NIT", "ACME SAS", "1", "EMPLEADOR");
        emp.setPosition("Dev");
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "1")).thenReturn(List.of(emp));

        AffiliationDependent dep = dependent(20L, 10L, "CC", "2", LocalDate.now().minusDays(90), LocalDate.now().minusDays(80));
        when(affiliationDependentRepository.findByMainAffiliateIdentification("CC", "1")).thenReturn(List.of(dep));
        when(affiliateRepository.findById(10L)).thenReturn(Optional.of(emp));

        EmployerResponse er = new EmployerResponse();
        er.setIdSubEmpresa(1);
        er.setRazonSocialSubempresa("Sub Uno");
        when(consultEmployerClient.consult("NIT", "899999061", null)).thenReturn(Mono.just(List.of(er)));

        List<VinculacionDTO> out = service.consultLinks(q);

        assertEquals(2, out.size());
        VinculacionDTO vincEmp = out.stream().filter(v -> "EMPLEADOR".equals(v.getTipoVinculacion())).findFirst().orElseThrow();
        assertNotNull(vincEmp.getSubEmpresas());
        assertEquals(1, vincEmp.getSubEmpresas().get(0).getIdSubEmpresa());

        VinculacionDTO vincDep = out.stream().filter(v -> "TRABAJADOR DEPENDIENTE".equals(v.getTipoVinculacion())).findFirst().orElseThrow();
        assertEquals("ACME SAS", vincDep.getContratante());
    }


    @Test
    @DisplayName("updateDateCoverage - Dependiente: Actualiza correctamente y guarda observación")
    void updateDateCoverage_dependent_happy_path_and_saves_observation_with_officialId() {
        putJwt("999", "123", true);
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(20L);
        dto.setTipoVinculacion("TRABAJADOR DEPENDIENTE");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(1));
        dto.setCausalNovedad("CAU");
        dto.setObservaciones("Obs");

        Affiliate employer = employer(10L, "ACTIVO", "N", LocalDateTime.now().minusDays(30), null,
                LocalDate.now().minusDays(20), "NIT", "ACME", "111", "EMPLEADOR");
        AffiliationDependent dep = dependent(20L, 10L, "CC", "9999", LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));

        when(affiliationDependentRepository.findById(20L)).thenReturn(Optional.of(dep));
        when(affiliateRepository.findById(10L)).thenReturn(Optional.of(employer));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenAnswer(i -> i.getArgument(0));
        when(observationsRepository.save(any(ObservationsAffiliation.class))).thenAnswer(i -> i.getArgument(0));

        service.updateDateCoverage(dto);

        assertEquals(dto.getNuevaFechaCobertura(), dep.getCoverageDate());
        verify(affiliationDependentRepository).save(dep);
        verify(observationsRepository).save(any(ObservationsAffiliation.class));
    }

    @Test
    @DisplayName("updateDateCoverage - Dependiente: Rechaza fecha futura")
    void updateDateCoverage_dependent_rejects_future() {
        putJwt("x", "1", true);

        AffiliationDependent dep = dependent(21L, 11L, "CC", "777", LocalDate.now().minusDays(100), LocalDate.now().minusDays(90));
        Affiliate employer = employer(11L, "ACTIVO", "N", LocalDateTime.now().minusDays(50), null, LocalDate.now().minusDays(49),
                "N", "C", "D", "EMPLEADOR");

        when(affiliationDependentRepository.findById(21L)).thenReturn(Optional.of(dep));
        when(affiliateRepository.findById(11L)).thenReturn(Optional.of(employer));

        UpdateCoverageDateDTO dtoFuture = new UpdateCoverageDateDTO();
        dtoFuture.setIdVinculacion(21L);
        dtoFuture.setTipoVinculacion("TRABAJADOR DEPENDIENTE");
        dtoFuture.setNuevaFechaCobertura(LocalDate.now().plusDays(1));
        dtoFuture.setCausalNovedad("C");
        dtoFuture.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dtoFuture));
    }

    @Test
    @DisplayName("updateDateCoverage - Dependiente: Rechaza fecha anterior al empleador")
    void updateDateCoverage_dependent_rejects_before_employer() {
        putJwt("x", "1", true);

        AffiliationDependent dep = dependent(21L, 11L, "CC", "777", LocalDate.now().minusDays(100), LocalDate.now().minusDays(90));
        Affiliate employer = employer(11L, "ACTIVO", "N", LocalDateTime.now().minusDays(50), null, LocalDate.now().minusDays(49),
                "N", "C", "D", "EMPLEADOR");

        when(affiliationDependentRepository.findById(21L)).thenReturn(Optional.of(dep));
        when(affiliateRepository.findById(11L)).thenReturn(Optional.of(employer));

        UpdateCoverageDateDTO dtoBefore = new UpdateCoverageDateDTO();
        dtoBefore.setIdVinculacion(21L);
        dtoBefore.setTipoVinculacion("TRABAJADOR DEPENDIENTE");
        dtoBefore.setNuevaFechaCobertura(LocalDate.now().minusDays(60));
        dtoBefore.setCausalNovedad("C");
        dtoBefore.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dtoBefore));
    }


    @Test
    @DisplayName("updateDateCoverage - Independiente: Actualiza correctamente")
    void updateDateCoverage_independent_ok_and_updates_affiliate() {
        putJwt("999", "10", true);
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(30L);
        dto.setTipoVinculacion("723");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(2));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        Affiliate ind = employer(30L, "Activa", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "NIT", "Ind", "ABC", "TRABAJADOR INDEPENDIENTE");

        when(affiliateRepository.findById(30L)).thenReturn(Optional.of(ind));
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(i -> i.getArgument(0));
        when(observationsRepository.save(any(ObservationsAffiliation.class))).thenAnswer(i -> i.getArgument(0));

        service.updateDateCoverage(dto);
        assertEquals(dto.getNuevaFechaCobertura(), ind.getCoverageStartDate());
        verify(affiliateRepository).save(ind);
    }

    @Test
    @DisplayName("updateDateCoverage - Independiente: Rechaza si está inactivo")
    void updateDateCoverage_independent_rejects_inactive() {
        putJwt("X", "9", true);

        Affiliate ind = employer(31L, "INACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "Ind", "DOC", "TRABAJADOR INDEPENDIENTE");

        when(affiliateRepository.findById(31L)).thenReturn(Optional.of(ind));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(31L);
        dto.setTipoVinculacion("TRABAJADOR INDEPENDIENTE");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(1));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Independiente: Rechaza fecha futura")
    void updateDateCoverage_independent_rejects_future() {
        putJwt("X", "9", true);

        Affiliate ind = employer(31L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "Ind", "DOC", "TRABAJADOR INDEPENDIENTE");

        when(affiliateRepository.findById(31L)).thenReturn(Optional.of(ind));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(31L);
        dto.setTipoVinculacion("TRABAJADOR INDEPENDIENTE");
        dto.setNuevaFechaCobertura(LocalDate.now().plusDays(1));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Independiente: Rechaza fecha anterior al inicio")
    void updateDateCoverage_independent_rejects_before_start() {
        putJwt("X", "9", true);

        Affiliate ind = employer(31L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "Ind", "DOC", "TRABAJADOR INDEPENDIENTE");

        when(affiliateRepository.findById(31L)).thenReturn(Optional.of(ind));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(31L);
        dto.setTipoVinculacion("TRABAJADOR INDEPENDIENTE");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(20));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }


    @Test
    @DisplayName("updateDateCoverage - Empleador: Actualiza correctamente")
    void updateDateCoverage_employer_ok_updates_and_may_shift_affiliationDate() {
        putJwt("999", "55", true);
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(40L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(30));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        Affiliate emp = employer(40L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "ACME", "DOC-EMP", "EMPLEADOR");

        when(affiliateRepository.findById(40L)).thenReturn(Optional.of(emp));
        when(affiliateRepository.findMinCoverageDateOfDependents(40L)).thenReturn(Optional.empty());
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(i -> i.getArgument(0));
        when(observationsRepository.save(any(ObservationsAffiliation.class))).thenAnswer(i -> i.getArgument(0));

        service.updateDateCoverage(dto);

        assertEquals(dto.getNuevaFechaCobertura(), emp.getCoverageStartDate());
        assertEquals(dto.getNuevaFechaCobertura().atStartOfDay(), emp.getAffiliationDate());
    }

    @Test
    @DisplayName("updateDateCoverage - Empleador: Rechaza si está inactivo")
    void updateDateCoverage_employer_rejects_inactive() {
        putJwt("Z", "77", true);

        Affiliate emp = employer(41L, "INACTIVO", "N", LocalDateTime.now().minusDays(100),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(50),
                "N", "ACME", "DOC", "EMPLEADOR");

        when(affiliateRepository.findById(41L)).thenReturn(Optional.of(emp));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(41L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(20));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Empleador: Rechaza fecha futura")
    void updateDateCoverage_employer_rejects_future() {
        putJwt("Z", "77", true);

        Affiliate emp = employer(41L, "ACTIVO", "N", LocalDateTime.now().minusDays(100),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(50),
                "N", "ACME", "DOC", "EMPLEADOR");

        when(affiliateRepository.findById(41L)).thenReturn(Optional.of(emp));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(41L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().plusDays(1));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Empleador: Rechaza fecha posterior a inactivación")
    void updateDateCoverage_employer_rejects_after_retirement() {
        putJwt("Z", "77", true);

        Affiliate emp = employer(41L, "ACTIVO", "N", LocalDateTime.now().minusDays(100),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(50),
                "N", "ACME", "DOC", "EMPLEADOR");

        when(affiliateRepository.findById(41L)).thenReturn(Optional.of(emp));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(41L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(5));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Empleador: Rechaza fecha posterior a dependientes")
    void updateDateCoverage_employer_rejects_after_dependents() {
        putJwt("Z", "77", true);

        Affiliate emp = employer(41L, "ACTIVO", "N", LocalDateTime.now().minusDays(100),
                null, LocalDate.now().minusDays(50),
                "N", "ACME", "DOC", "EMPLEADOR");

        when(affiliateRepository.findById(41L)).thenReturn(Optional.of(emp));
        when(affiliateRepository.findMinCoverageDateOfDependents(41L))
                .thenReturn(Optional.of(LocalDate.now().minusDays(7)));

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(41L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(5));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Rechaza campos faltantes")
    void updateDateCoverage_rejects_missing_fields() {
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(null);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now());

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Rechaza tipo de vinculación inválido")
    void updateDateCoverage_rejects_invalid_tipo() {
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(1L);
        dto.setTipoVinculacion("DESCONOCIDO");
        dto.setNuevaFechaCobertura(LocalDate.now());
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Rechaza cuando falta autenticación")
    void updateDateCoverage_rejects_when_no_auth() {
        SecurityContextHolder.clearContext();
        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(60L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(1));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        Affiliate emp = employer(60L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "ACME", "DOC-SELF", "EMPLEADOR");
        when(affiliateRepository.findById(60L)).thenReturn(Optional.of(emp));

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }

    @Test
    @DisplayName("updateDateCoverage - Rechaza auto-actualización")
    void updateDateCoverage_rejects_self_update() {
        putJwt("DOC-SELF", "999", true);

        UpdateCoverageDateDTO dto = new UpdateCoverageDateDTO();
        dto.setIdVinculacion(60L);
        dto.setTipoVinculacion("EMPLEADOR");
        dto.setNuevaFechaCobertura(LocalDate.now().minusDays(1));
        dto.setCausalNovedad("C");
        dto.setObservaciones("O");

        Affiliate emp = employer(60L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "N", "ACME", "DOC-SELF", "EMPLEADOR");
        when(affiliateRepository.findById(60L)).thenReturn(Optional.of(emp));

        assertThrows(DateUpdateException.class, () -> service.updateDateCoverage(dto));
    }
    @Test
    @DisplayName("getLinksDetail - Obtiene detalle de empleador con representante legal")
    void getLinksDetail_employer_includes_legal_representative() {
        Affiliate emp = employer(70L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "NIT", "ACME", "100", "EMPLEADOR");
        emp.setDocumentType("NIT");

        when(affiliateRepository.findById(70L)).thenReturn(Optional.of(emp));

        LegalRepresentativeResponse rep = new LegalRepresentativeResponse();
        rep.setIdTipoDoc("CC");
        rep.setIdPersona("123");
        rep.setNombre1("Juan");
        rep.setNombre2("P.");
        rep.setApellido1("Gómez");
        rep.setApellido2("");
        when(consultLegalRepresentativeClient.consult("NIT", emp.getNitCompany(), null))
                .thenReturn(Mono.just(List.of(rep)));

        VinculacionDetalleDTO dto = service.getLinksDetail("EMPLEADOR", 70L);

        assertEquals("NIT", dto.getTipoDocumentoIdentificacion());
        assertEquals("ACME", dto.getNombreCompletoORazonSocial());
        assertEquals("CC", dto.getTipoDocumentoRepLegal());
        assertEquals("123", dto.getNumeroIdentificacionRepLegal());
    }

    @Test
    @DisplayName("getLinksDetail - Obtiene detalle de independiente")
    void getLinksDetail_independent() {
        Affiliate ind = employer(80L, "ACTIVO", "N", LocalDateTime.now().minusDays(10), null,
                LocalDate.now().minusDays(5), "CC", "IND", "999", "TRABAJADOR INDEPENDIENTE");
        ind.setDocumentType("CC");

        when(affiliateRepository.findById(80L)).thenReturn(Optional.of(ind));
        VinculacionDetalleDTO dto = service.getLinksDetail("TRABAJADOR INDEPENDIENTE", 80L);

        assertEquals("CC", dto.getTipoDocumentoIdentificacion());
        assertEquals("IND", dto.getNombreCompletoORazonSocial());
    }

    @Test
    @DisplayName("getLinksDetail - Obtiene detalle de dependiente")
    void getLinksDetail_dependent() {
        AffiliationDependent dep = dependent(90L, 80L, "TI", "777",
                LocalDate.now().minusDays(9), LocalDate.now().minusDays(7));
        when(affiliationDependentRepository.findById(90L)).thenReturn(Optional.of(dep));

        VinculacionDetalleDTO dto = service.getLinksDetail("TRABAJADOR DEPENDIENTE", 90L);

        assertEquals("TI", dto.getTipoDocumentoIdentificacion());
        assertEquals("777", dto.getNumeroIdentificacion());
    }

    @Test
    @DisplayName("getLinksDetail - Rechaza tipo inválido")
    void getLinksDetail_rejects_invalid_type() {
        assertThrows(DateUpdateException.class, () -> service.getLinksDetail("TIPO_INVALIDO", 1L));
    }
}