package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateResult;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateStatusGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpStatusResult;
import com.gal.afiliaciones.application.service.tmp.ExcelPersonConsultationService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeListResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeQueryRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // evita UnnecessaryStubbingException en stubs residuales
class EmployerEmployeeQueryServiceImplTest {

    @Mock AffiliationDependentRepository affiliationDependentRepository;
    @Mock AffiliationDetailRepository affiliationDetailRepository;
    @Mock AffiliateRepository affiliateRepository;
    @Mock OccupationRepository occupationRepository;
    @Mock MunicipalityRepository municipalityRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock FundPensionRepository fundPensionRepository;
    @Mock ArlRepository arlRepository;
    @Mock HealthPromotingEntityRepository healthRepository;
    @Mock OccupationDecree1563Repository occupationDecree1563Repository;
    @Mock ExcelPersonConsultationService excelPersonConsultationService;
    @Mock ConsultSiarpAffiliateClient consultSiarpAffiliateClient;
    @Mock SiarpAffiliateGateway siarpAffiliateGateway;
    @Mock ConsultSiarpAffiliateStatusClient consultSiarpAffiliateStatusClient;
    @Mock SiarpAffiliateStatusGateway siarpAffiliateStatusGateway;

    @InjectMocks
    EmployerEmployeeQueryServiceImpl service;

    private EmployerEmployeeQueryRequestDTO req;

    @BeforeEach
    void setUp() {
        req = new EmployerEmployeeQueryRequestDTO();
        req.setTDocEmp("NIT");
        req.setIdEmp("900123456");
        req.setTDocAfi("CC");
        req.setIdAfi("12345678");
    }

    // ------- Helpers con tipos correctos -------
    private Affiliate depAffiliate() {
        Affiliate a = new Affiliate();
        a.setIdAffiliate(1L);
        a.setFiledNumber("F001");
        a.setAffiliationType("Trabajador Dependiente");
        a.setDocumentType("NIT");
        a.setNitCompany("900123456");
        a.setCompany("ACME");
        a.setDocumentNumber("12345678");
        a.setCoverageStartDate(LocalDate.of(2024, 1, 10));
        a.setRetirementDate(LocalDate.of(2024, 6, 30));
        return a;
    }

    private AffiliationDependent depDetail() {
        AffiliationDependent d = new AffiliationDependent();
        d.setIdentificationDocumentType("CC");
        d.setIdentificationDocumentNumber("12345678");
        d.setFirstName("JUAN");
        d.setSecondName("CARLOS");
        d.setSurname("PEREZ");
        d.setSecondSurname("LOPEZ");
        d.setGender("M");
        d.setDateOfBirth(LocalDate.of(1990, 5, 17));
        d.setPensionFundAdministrator(10L);
        d.setHealthPromotingEntity(20L);
        d.setOccupationalRiskManager("30");
        d.setAddress("CL 1 # 2-3");
        d.setIdOccupation(40L);
        d.setSalary(new BigDecimal("2500000"));
        d.setIdDepartment(5L);
        d.setIdCity(1001L);
        return d;
    }

    private Affiliate indAffiliate() {
        Affiliate a = new Affiliate();
        a.setIdAffiliate(2L);
        a.setFiledNumber("F002");
        a.setAffiliationType("Trabajador Independiente");
        a.setDocumentType("NIT");
        a.setNitCompany("900123456");
        a.setCompany("ACME");
        a.setDocumentNumber("87654321");
        return a;
    }

    private Affiliation indDetail() {
        Affiliation i = new Affiliation();
        i.setIdentificationDocumentType("CC");
        i.setIdentificationDocumentNumber("87654321");
        i.setFirstName("MARIA");
        i.setSecondName("ELENA");
        i.setSurname("GARCIA");
        i.setSecondSurname("MARTINEZ");
        i.setGender("F");
        i.setContractStartDate(LocalDate.of(2023, 3, 1));
        i.setContractEndDate(LocalDate.of(2023, 9, 30));
        i.setDateOfBirth(LocalDate.of(1988, 8, 8));
        i.setPensionFundAdministrator(11L);
        i.setHealthPromotingEntity(21L);
        i.setCurrentARL("31");
        i.setAddress("CR 4 # 5-6");
        i.setOccupation("DESARROLLADOR");
        i.setContractMonthlyValue(new BigDecimal("3000000"));
        i.setDepartment(6L);
        i.setCityMunicipality(2002L);
        return i;
    }

    // ----------------- Tests -----------------

    @Test
    @DisplayName("queryEmployeeByParameters2: prioriza SIARP si trae registros")
    void queryEmployeeByParameters2_siarpFirst() {
        // SIARP con data
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        SiarpAffiliateResult result = mock(SiarpAffiliateResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(dto)));
        when(siarpAffiliateGateway.getAffiliate(eq("CC"), eq("12345678")))
                .thenReturn(Mono.just(result));

        // BALU: solo el necesario (el servicio sí lo llama aunque gane SIARP)
        when(affiliateRepository.findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678")))
                .thenReturn(List.of());

        // (Quitamos stubs de findAllByDocumentTypeAndDocumentNumber y de EXCEL para no generar stubs innecesarios)

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertTrue(out.getMessage().contains("SIARP"));
    }

    @Test
    @DisplayName("queryEmployeeByParameters2: si SIARP vacío, usa BALU (dep)")
    void queryEmployeeByParameters2_baluDep() {
        when(siarpAffiliateGateway.getAffiliate(any(), any())).thenReturn(Mono.empty());

        Affiliate dep = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678")))
                .thenReturn(List.of(dep));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));


        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2: si SIARP y BALU vacíos, usa EXCEL")
    void queryEmployeeByParameters2_excel() {
        when(siarpAffiliateGateway.getAffiliate(any(), any())).thenReturn(Mono.empty());
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());
        // (Quitamos stub de findAllByDocumentTypeAndDocumentNumber: no se invoca en este camino)

        EmployerEmployeeDTO excel = new EmployerEmployeeDTO();
        excel.setIdDepartamento(5);
        excel.setIdMunicipio(1001);
        excel.setIdEps("EPS001");
        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(excel));

        // Enriquecimiento (sí se usan)
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());
        when(municipalityRepository.findById(1001L)).thenReturn(Optional.empty());
        when(healthRepository.findByCodeEPS("EPS001")).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU_PRE", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2(appSource=BALU): fuerza BALU (dep+ind)")
    void testQueryEmployeeByParameters2_forcedBALU() {
        Affiliate dep = depAffiliate();
        Affiliate ind = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(dep, ind));

        when(affiliationDependentRepository.findByFiledNumber("F001")).thenReturn(Optional.of(depDetail()));
        when(affiliationDetailRepository.findByFiledNumber("F002")).thenReturn(Optional.of(indDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(2, out.getEmployees().size());
        assertEquals("BALU", out.getEmployees().get(0).getAppSource());
        assertEquals("BALU", out.getEmployees().get(1).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2(appSource=BALU_PRE): fuerza EXCEL y tolera errores de enriquecimiento")
    void testQueryEmployeeByParameters2_forcedExcel() {
        EmployerEmployeeDTO excel = new EmployerEmployeeDTO();
        excel.setIdDepartamento(5);
        excel.setIdMunicipio(1001);
        excel.setIdEps("EPS001");
        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(excel));

        // Solo estos stubs (sí se usan). Antes sobraban algunos y disparaban UnnecessaryStubbing
        when(departmentRepository.findById(5L)).thenThrow(new RuntimeException("dep"));
        when(municipalityRepository.findById(1001L)).thenThrow(new RuntimeException("mun"));
        when(healthRepository.findByCodeEPS("EPS001")).thenThrow(new RuntimeException("eps"));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU_PRE");
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU_PRE", out.getEmployees().get(0).getAppSource());
    }

    // ----------------- Status -----------------

    @Test
    @DisplayName("queryEmployeeByParameters1: SIARP primero; si vacío cae a BALU y luego EXCEL")
    void status_priorityChain() {
        // 1) SIARP con data
        TmpAffiliateStatusDTO st = TmpAffiliateStatusDTO.builder()
                .idTipoDocEmp("NIT").idEmpresa("900123456")
                .idTipoDocPer("CC").idPersona("12345678")
                .appSource("SIARP").build();
        SiarpStatusResult res = mock(SiarpStatusResult.class);
        when(res.dto()).thenReturn(Optional.of(List.of(st)));
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.just(res));

        List<TmpAffiliateStatusDTO> out1 = service.queryEmployeeByParameters1(req);
        assertEquals(1, out1.size());
        assertEquals("SIARP", out1.get(0).getAppSource());

        // 2) SIARP vacío -> BALU
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out2 = service.queryEmployeeByParameters1(req);
        assertEquals(1, out2.size());
        assertEquals("BALU", out2.get(0).getAppSource());

        // 3) SIARP y BALU vacíos -> EXCEL
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any())).thenReturn(List.of());
        TmpAffiliateStatusDTO stExcel = TmpAffiliateStatusDTO.builder().idPersona("X").build();
        when(excelPersonConsultationService.consultAffiliateStatus(any(), any(), any(), any()))
                .thenReturn(List.of(stExcel));

        List<TmpAffiliateStatusDTO> out3 = service.queryEmployeeByParameters1(req);
        assertEquals(1, out3.size());
        assertEquals("X", out3.get(0).getIdPersona());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1(appSource=BALU) y (appSource=BALU_PRE)")
    void status_forcedSources() {
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(depAffiliate()));
        List<TmpAffiliateStatusDTO> balu = service.queryEmployeeByParameters1(req, "BALU");
        assertEquals(1, balu.size());
        assertEquals("BALU", balu.get(0).getAppSource());

        TmpAffiliateStatusDTO st = TmpAffiliateStatusDTO.builder().idPersona("Z").build();
        when(excelPersonConsultationService.consultAffiliateStatus(any(), any(), any(), any()))
                .thenReturn(List.of(st));
        List<TmpAffiliateStatusDTO> excel = service.queryEmployeeByParameters1(req, "BALU_PRE");
        assertEquals(1, excel.size());
        assertEquals("Z", excel.get(0).getIdPersona());
    }


    @Test
    @DisplayName("queryEmployeeByParameters2: todos los futuros vacíos")
    void queryEmployeeByParameters2_allEmpty() {
        when(siarpAffiliateGateway.getAffiliate(any(), any())).thenReturn(Mono.empty());
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);

        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertTrue(out.getEmployees().isEmpty());
        assertTrue(out.getMessage().contains("No se encontraron"));
    }

    @Test
    @DisplayName("queryEmployeeByParameters2: SIARP lanza excepción, cae a BALU")
    void queryEmployeeByParameters2_siarpException() {
        when(siarpAffiliateGateway.getAffiliate(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("SIARP error")));

        Affiliate dep = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(dep));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);

        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2: SIARP retorna resultado pero sin DTO (RAW)")
    void queryEmployeeByParameters2_siarpRaw() {
        SiarpAffiliateResult result = mock(SiarpAffiliateResult.class);
        when(result.dto()).thenReturn(Optional.empty()); // RAW response
        when(siarpAffiliateGateway.getAffiliate(any(), any()))
                .thenReturn(Mono.just(result));

        Affiliate dep = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(dep));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);

        // Debe caer a BALU porque SIARP retornó RAW
        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2(appSource): valor diferente de BALU/BALU_PRE")
    void queryEmployeeByParameters2_unknownAppSource() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        SiarpAffiliateResult result = mock(SiarpAffiliateResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(dto)));
        when(siarpAffiliateGateway.getAffiliate(any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        // Debe caer al método sin appSource (orquestador normal)
        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "UNKNOWN");

        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2(appSource): null appSource")
    void queryEmployeeByParameters2_nullAppSource() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        SiarpAffiliateResult result = mock(SiarpAffiliateResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(dto)));
        when(siarpAffiliateGateway.getAffiliate(any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, null);

        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(1, out.getEmployees().size());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1: SIARP lanza excepción, cae a BALU")
    void queryEmployeeByParameters1_siarpException() {
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("SIARP status error")));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req);

        assertEquals(1, out.size());
        assertEquals("BALU", out.get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1: SIARP retorna RAW")
    void queryEmployeeByParameters1_siarpRaw() {
        SiarpStatusResult result = mock(SiarpStatusResult.class);
        when(result.dto()).thenReturn(Optional.empty()); // RAW
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req);

        // Debe caer a BALU
        assertEquals(1, out.size());
        assertEquals("BALU", out.get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1: todos vacíos")
    void queryEmployeeByParameters1_allEmpty() {
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.empty());
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(excelPersonConsultationService.consultAffiliateStatus(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req);

        assertTrue(out.isEmpty());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1: EXCEL retorna null")
    void queryEmployeeByParameters1_excelNull() {
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.empty());
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(excelPersonConsultationService.consultAffiliateStatus(any(), any(), any(), any()))
                .thenReturn(null);

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req);

        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1(appSource): valor desconocido")
    void queryEmployeeByParameters1_unknownAppSource() {
        TmpAffiliateStatusDTO st = TmpAffiliateStatusDTO.builder()
                .idPersona("12345678")
                .appSource("SIARP")
                .build();
        SiarpStatusResult result = mock(SiarpStatusResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(st)));
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "SIARP_DIRECT");

        // Debe caer al método sin appSource
        assertEquals(1, out.size());
        assertEquals("SIARP", out.get(0).getAppSource());
    }

    @Test
    @DisplayName("queryStatusFromBalu: sin filtros de empleador")
    void queryStatusFromBalu_noEmployerFilter() {
        req.setTDocEmp(null);
        req.setIdEmp(null);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678")))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "BALU");

        assertEquals(1, out.size());
        assertEquals("BALU", out.get(0).getAppSource());
    }

    @Test
    @DisplayName("queryStatusFromBalu: con filtros vacíos")
    void queryStatusFromBalu_emptyEmployerFilter() {
        req.setTDocEmp("");
        req.setIdEmp("  ");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678")))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "BALU");

        assertEquals(1, out.size());
    }

    @Test
    @DisplayName("queryFromBalu: sin filtros de empleador")
    void queryFromBalu_noEmployerFilter() {
        req.setTDocEmp(null);
        req.setIdEmp(null);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678")))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
    }

    @Test
    @DisplayName("queryFromBalu: affiliate con filed_number null")
    void queryFromBalu_nullFiledNumber() {
        Affiliate aff = depAffiliate();
        aff.setFiledNumber(null);

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        // Debe skipear el affiliate con filed_number null
        assertTrue(out.getEmployees().isEmpty());
    }

    @Test
    @DisplayName("queryFromBalu: dependiente no encontrado en repo")
    void queryFromBalu_dependentNotFound() {
        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertTrue(out.getEmployees().isEmpty());
    }

    @Test
    @DisplayName("queryFromBalu: independiente no encontrado en repo")
    void queryFromBalu_independentNotFound() {
        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertTrue(out.getEmployees().isEmpty());
    }

    @Test
    @DisplayName("queryFromBalu: tipo de afiliación desconocido")
    void queryFromBalu_unknownAffiliationType() {
        Affiliate aff = depAffiliate();
        aff.setAffiliationType("Tipo Desconocido");

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertTrue(out.getEmployees().isEmpty());
    }



    @Test
    @DisplayName("mapDependentToEmployeeDTO: AFP no encontrado")
    void mapDependent_afpNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setPensionFundAdministrator(999L);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(fundPensionRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdAfp());
        assertNull(dto.getNombreAfp());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: EPS no encontrado")
    void mapDependent_epsNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setHealthPromotingEntity(999L);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(healthRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdEps());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: ARL con formato inválido")
    void mapDependent_invalidArlFormat() {
        AffiliationDependent dep = depDetail();
        dep.setOccupationalRiskManager("INVALID");

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdArp());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: ARL no encontrado en repo")
    void mapDependent_arlNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setOccupationalRiskManager("999");

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(arlRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdArp());
        assertNull(dto.getNombreArp());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: Ocupación no encontrada")
    void mapDependent_occupationNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setIdOccupation(999L);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(occupationRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdOcupacion());
        assertNull(dto.getNombreOcupacion());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: Departamento no encontrado")
    void mapDependent_departmentNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setIdDepartment(999L);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdDepartamento());
        assertNull(dto.getNombreDepartamento());
    }

    @Test
    @DisplayName("mapDependentToEmployeeDTO: Municipio no encontrado")
    void mapDependent_municipalityNotFound() {
        AffiliationDependent dep = depDetail();
        dep.setIdCity(9999L);

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(dep));
        when(municipalityRepository.findById(9999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdMunicipio());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: con campos null")
    void mapIndependent_nullFields() {
        Affiliation ind = new Affiliation();
        ind.setIdentificationDocumentType("CC");
        ind.setIdentificationDocumentNumber("87654321");
        // Todos los demás campos null

        Affiliate aff = new Affiliate();
        aff.setFiledNumber("F002");
        aff.setAffiliationType("Trabajador Independiente");
        aff.setDocumentType("NIT");
        aff.setNitCompany("900123456");
        aff.setCompany("ACME");

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getNombre1());
        assertNull(dto.getFechaNacimiento());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: AFP no encontrado")
    void mapIndependent_afpNotFound() {
        Affiliation ind = indDetail();
        ind.setPensionFundAdministrator(999L);

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));
        when(fundPensionRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdAfp());
        assertNull(dto.getNombreAfp());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: EPS no encontrado")
    void mapIndependent_epsNotFound() {
        Affiliation ind = indDetail();
        ind.setHealthPromotingEntity(999L);

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));
        when(healthRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdEps());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: ARL con formato inválido")
    void mapIndependent_invalidArlFormat() {
        Affiliation ind = indDetail();
        ind.setCurrentARL("INVALID");

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdArp());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: ARL no encontrado en repo")
    void mapIndependent_arlNotFound() {
        Affiliation ind = indDetail();
        ind.setCurrentARL("999");

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));
        when(arlRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdArp());
        assertNull(dto.getNombreArp());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: Ocupación null")
    void mapIndependent_occupationNull() {
        Affiliation ind = indDetail();
        ind.setOccupation(null);

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getNombreOcupacion());
        assertNull(dto.getIdOcupacion());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: Departamento no encontrado")
    void mapIndependent_departmentNotFound() {
        Affiliation ind = indDetail();
        ind.setDepartment(999L);

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertEquals(999, dto.getIdDepartamento());
        assertNull(dto.getNombreDepartamento());
    }

    @Test
    @DisplayName("mapIndependentToEmployeeDTO: Municipio no encontrado")
    void mapIndependent_municipalityNotFound() {
        Affiliation ind = indDetail();
        ind.setCityMunicipality(9999L);

        Affiliate aff = indAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDetailRepository.findByFiledNumber("F002"))
                .thenReturn(Optional.of(ind));
        when(municipalityRepository.findById(9999L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getIdMunicipio());
    }

    @Test
    @DisplayName("enrichExcelDescriptions: con epsCode null")
    void enrichExcel_nullEpsCode() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        dto.setIdEps(null);
        dto.setIdDepartamento(5);
        dto.setIdMunicipio(1001);

        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(dto));
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());
        when(municipalityRepository.findById(1001L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU_PRE");

        assertEquals(1, out.getEmployees().size());
        verify(healthRepository, never()).findByCodeEPS(any());
    }

    @Test
    @DisplayName("enrichExcelDescriptions: con epsCode vacío")
    void enrichExcel_blankEpsCode() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        dto.setIdEps("  ");
        dto.setIdDepartamento(5);
        dto.setIdMunicipio(1001);

        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(dto));
        when(departmentRepository.findById(5L)).thenReturn(Optional.empty());
        when(municipalityRepository.findById(1001L)).thenReturn(Optional.empty());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU_PRE");

        assertEquals(1, out.getEmployees().size());
        verify(healthRepository, never()).findByCodeEPS(any());
    }

    @Test
    @DisplayName("enrichExcelDescriptions: todos los campos null")
    void enrichExcel_allNull() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        dto.setIdDepartamento(null);
        dto.setIdMunicipio(null);
        dto.setIdEps(null);

        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(dto));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU_PRE");

        assertEquals(1, out.getEmployees().size());
        verify(departmentRepository, never()).findById(any());
        verify(municipalityRepository, never()).findById(any());
        verify(healthRepository, never()).findByCodeEPS(any());
    }




    @Test
    @DisplayName("queryFromBalu: normalización de tipos de documento")
    void queryFromBalu_documentTypeNormalization() {
        req.setTDocEmp("  nit  ");
        req.setIdEmp("  900123456  ");
        req.setTDocAfi("  cc  ");
        req.setIdAfi("  12345678  ");

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678")))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        verify(affiliateRepository).findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678"));
    }

    @Test
    @DisplayName("queryStatusFromBalu: normalización de tipos de documento")
    void queryStatusFromBalu_documentTypeNormalization() {
        req.setTDocEmp("  nit  ");
        req.setIdEmp("  900123456  ");
        req.setTDocAfi("  cc  ");
        req.setIdAfi("  12345678  ");

        when(affiliateRepository.findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678")))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "BALU");

        assertEquals(1, out.size());
        verify(affiliateRepository).findByCompanyAndAffiliateDocument(eq("NIT"), eq("900123456"), eq("CC"), eq("12345678"));
    }
    @Test
    @DisplayName("enrichExcelDescriptions: lista vacía")
    void enrichExcel_emptyList() {
        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU_PRE");

        assertNotNull(out);
        assertTrue(out.getEmployees().isEmpty());
    }
    @Test
    @DisplayName("mapDependentToEmployeeDTO: con campos null")
    void mapDependent_nullFields() {
        AffiliationDependent dep = new AffiliationDependent();
        dep.setIdentificationDocumentType("CC");
        dep.setIdentificationDocumentNumber("12345678");

        Affiliate aff = new Affiliate();
        aff.setFiledNumber("F999");
        aff.setAffiliationType("Trabajador Dependiente");
        aff.setDocumentType("NIT");
        aff.setNitCompany("900123456");
        aff.setCompany("ACME");

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F999"))
                .thenReturn(Optional.of(dep));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        EmployerEmployeeDTO dto = out.getEmployees().get(0);
        assertNull(dto.getNombre1());
        assertNull(dto.getFechaNacimiento());
    }

    @Test
    @DisplayName("queryFromBalu: con filtros vacíos")
    void queryFromBalu_emptyEmployerFilters() {
        req.setTDocEmp("");
        req.setIdEmp("");

        Affiliate aff = depAffiliate();
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678")))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "BALU");

        assertEquals(1, out.getEmployees().size());
        verify(affiliateRepository).findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678"));
    }

    @Test
    @DisplayName("queryStatusFromBalu: con filtros vacíos de empleador")
    void queryStatusFromBalu_emptyEmployerFilters() {
        req.setTDocEmp("");
        req.setIdEmp("");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678")))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "BALU");

        assertEquals(1, out.size());
        verify(affiliateRepository).findAllByDocumentTypeAndDocumentNumber(eq("CC"), eq("12345678"));
    }
    @Test
    @DisplayName("queryEmployeeByParameters2: SIARP con múltiples registros")
    void queryEmployeeByParameters2_siarpMultipleRecords() {
        EmployerEmployeeDTO dto1 = new EmployerEmployeeDTO();
        dto1.setIdPersona("111");
        EmployerEmployeeDTO dto2 = new EmployerEmployeeDTO();
        dto2.setIdPersona("222");

        SiarpAffiliateResult result = mock(SiarpAffiliateResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(dto1, dto2)));
        when(siarpAffiliateGateway.getAffiliate(any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req);

        assertTrue(Boolean.TRUE.equals(out.getSuccess()));
        assertEquals(2, out.getEmployees().size());
    }
    @Test
    @DisplayName("queryEmployeeByParameters1: SIARP con múltiples registros")
    void queryEmployeeByParameters1_siarpMultipleRecords() {
        TmpAffiliateStatusDTO st1 = TmpAffiliateStatusDTO.builder().idPersona("111").appSource("SIARP").build();
        TmpAffiliateStatusDTO st2 = TmpAffiliateStatusDTO.builder().idPersona("222").appSource("SIARP").build();

        SiarpStatusResult result = mock(SiarpStatusResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(st1, st2)));
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req);

        assertEquals(2, out.size());
        assertEquals("SIARP", out.get(0).getAppSource());
    }
    @Test
    @DisplayName("queryEmployeeByParameters2(appSource): con espacios")
    void queryEmployeeByParameters2_appSourceWithSpaces() {
        Affiliate aff = depAffiliate();
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));
        when(affiliationDependentRepository.findByFiledNumber("F001"))
                .thenReturn(Optional.of(depDetail()));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "  BALU  ");

        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1(appSource): con espacios")
    void queryEmployeeByParameters1_appSourceWithSpaces() {
        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(depAffiliate()));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "  BALU  ");

        assertEquals(1, out.size());
        assertEquals("BALU", out.get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters2(appSource): lowercase BALU_PRE")
    void queryEmployeeByParameters2_lowercaseBaluPre() {
        EmployerEmployeeDTO dto = new EmployerEmployeeDTO();
        when(excelPersonConsultationService.consultPersonAsEmployerEmployee(any(), any(), any(), any()))
                .thenReturn(List.of(dto));

        EmployerEmployeeListResponseDTO out = service.queryEmployeeByParameters2(req, "balu_pre");

        assertEquals(1, out.getEmployees().size());
        assertEquals("BALU_PRE", out.getEmployees().get(0).getAppSource());
    }

    @Test
    @DisplayName("queryEmployeeByParameters1(appSource): null appSource")
    void queryEmployeeByParameters1_nullAppSource() {
        TmpAffiliateStatusDTO st = TmpAffiliateStatusDTO.builder()
                .idPersona("12345678")
                .appSource("SIARP")
                .build();
        SiarpStatusResult result = mock(SiarpStatusResult.class);
        when(result.dto()).thenReturn(Optional.of(List.of(st)));
        when(siarpAffiliateStatusGateway.getStatus(any(), any(), any(), any()))
                .thenReturn(Mono.just(result));

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, null);

        assertEquals(1, out.size());
        assertEquals("SIARP", out.get(0).getAppSource());
    }
    @Test
    @DisplayName("queryStatusFromBalu: affiliate con documTypeCompany null")
    void queryStatusFromBalu_nullDocumTypeCompany() {
        Affiliate aff = depAffiliate();
        aff.setDocumenTypeCompany(null);

        when(affiliateRepository.findByCompanyAndAffiliateDocument(any(), any(), any(), any()))
                .thenReturn(List.of(aff));

        List<TmpAffiliateStatusDTO> out = service.queryEmployeeByParameters1(req, "BALU");

        assertEquals(1, out.size());
        assertNull(out.get(0).getIdTipoDocEmp());
    }
}
