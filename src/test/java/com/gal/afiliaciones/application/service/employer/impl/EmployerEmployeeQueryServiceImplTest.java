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

        // EXCEL no se usa en este camino; no lo “stubbeamos”
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
}
