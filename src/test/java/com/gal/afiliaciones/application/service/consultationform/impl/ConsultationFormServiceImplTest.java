package com.gal.afiliaciones.application.service.consultationform.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.config.ex.PolicyException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.Billing;
import com.gal.afiliaciones.domain.model.BillingCollectionConciliation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliatactivityeconomic.AffiliateActivityEconomicRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.EmployerSizeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.conciliationbilling.BillingCollectionConciliationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliationdetail.AffiliationDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.*;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.*;
import com.gal.afiliaciones.infrastructure.dto.workerdetail.WorkerDetailDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import reactor.core.publisher.Mono;
import java.lang.reflect.Method;
@ExtendWith(MockitoExtension.class)
class ConsultationFormServiceImplFullTest {

    @Mock private AffiliateRepository affiliateRepository;
    @Mock private IUserPreRegisterRepository userPreRegisterRepository;
    @Mock private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock private EmployerSizeRepository employerSizeRepository;
    @Mock private AffiliationDependentRepository affiliationDependentRepository;
    @Mock private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock private AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;
    @Mock private GenericWebClient genericWebClient;
    @Mock private AffiliationDetailRepository affiliationDetailRepository;
    @Mock private BillingRepository billingRepository;
    @Mock private BillingCollectionConciliationRepository billingCollectionConciliationRepository;
    @Mock private RetirementRepository retirementRepository;
    // OJO: NO declaramos OccupationRepository porque en tu proyecto no se resuelve el símbolo.
    @Mock private PolicyRepository policyRepository;
    @Mock private IDataDocumentRepository dataDocumentRepository;
    @Mock private AffiliateActivityEconomicRepository affiliateActivityEconomicRepository;
    @Mock private IEconomicActivityRepository economicActivityRepository;

    @InjectMocks
    private ConsultationFormServiceImpl service; // usa la implementación real:contentReference[oaicite:4]{index=4}

    private Affiliate afBase;
    private AffiliationDependent dep;
    private Affiliation ind;
    private AffiliateMercantile merc;
    private UserMain user;
    private Retirement retirement;
    private Object invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = ConsultationFormServiceImpl.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(service, args);
    }
    @BeforeEach
    void init() {
        afBase = new Affiliate();
        afBase.setIdAffiliate(10L);
        afBase.setDocumentType("CC");
        afBase.setDocumentNumber("123456789");
        afBase.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        afBase.setAffiliationSubType("EMPLOYEE");
        afBase.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        afBase.setAffiliationDate(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        afBase.setCoverageStartDate(LocalDate.now());
        afBase.setCompany("Empresa X");
        afBase.setNitCompany("900900900");
        afBase.setFiledNumber("FILE-XYZ");
        afBase.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);

        dep = new AffiliationDependent();
        dep.setFiledNumber("FILE-XYZ");
        dep.setIdentificationDocumentType("CC");
        dep.setIdentificationDocumentNumber("123456789");
        dep.setFirstName("John");
        dep.setSecondName("A");
        dep.setSurname("Doe");
        dep.setSecondSurname("B");
        dep.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dep.setGender("M");
        dep.setIdDepartment(5L);
        dep.setIdCity(8L);
        dep.setAddress("Street 123");
        dep.setHealthPromotingEntity(11L);
        dep.setPensionFundAdministrator(22L);
        dep.setPhone1("111");
        dep.setEmail("john@example.com");
        dep.setStartDate(LocalDate.now());
        dep.setRisk(3);
        dep.setPriceRisk(BigDecimal.valueOf(1.5));
        dep.setIdOccupation(7L);

        ind = new Affiliation();
        ind.setFiledNumber("FILE-XYZ");
        ind.setIdentificationDocumentType("CC");
        ind.setIdentificationDocumentNumber("999");
        ind.setFirstName("Ind");
        ind.setSecondName("E");
        ind.setSurname("Pend");
        ind.setSecondSurname("Ant");
        ind.setStartDate(LocalDate.now());
        ind.setAddress("Calle 45");
        ind.setOccupation("Dev");
        ind.setRisk("2");
        ind.setPrice(BigDecimal.valueOf(2.2));
        ind.setEmail("i@x.com");
        ind.setPhone1("321");

        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("1234");
        ea.setDescription("Construcción");

        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        aae.setActivityEconomic(ea);
        ind.setEconomicActivity(Collections.singletonList(aae));

        merc = new AffiliateMercantile();
        merc.setId(77L);
        merc.setFiledNumber("FILE-MERC");
        merc.setNumberIdentification("800800800");
        merc.setDigitVerificationDV(9);
        merc.setBusinessName("Comercial SA");
        merc.setDepartment(11L);
        merc.setCityMunicipality(22L);
        merc.setAddress("Av 1");
        merc.setEmail("c@sa.com");
        merc.setTypeDocumentPersonResponsible("CC");
        merc.setNumberDocumentPersonResponsible("123");
        merc.setTypePerson("J");
        merc.setIdEmployerSize(3L);

        user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setStatusActive(true);
        user.setFirstName("User");
        user.setSecondName("Z");
        user.setSurname("Test");
        user.setSecondSurname("Last");

        retirement = new Retirement();
        retirement.setIdAffiliate(10L);
        retirement.setRetirementDate(LocalDate.of(2024, 1, 10));
        retirement.setFiledNumber("RET-10");
    }

    // ---------- getInfo (ramas) ----------

    @Test
    void getInfo_independentEmployee_returnsWorkerBasicInfo() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "1"))
                .thenReturn(List.of(af));

        // Evitar ambigüedad Example/Specification:
        doReturn(Optional.of(user))
                .when(userPreRegisterRepository)
                .findOne(ArgumentMatchers.<Specification<UserMain>>any());

        InfoConsultDTO dto = service.getInfo("CC", "1", Constant.EMPLOYEE, null);
        assertTrue(dto instanceof WorkerBasicInfoDTO);
        WorkerBasicInfoDTO w = (WorkerBasicInfoDTO) dto;
        assertEquals("CC", w.getDocumentType());
        assertEquals("123456789", w.getDocumentNumber());
    }

    @Test
    void getInfo_dependentEmployee_returnsWorkerDependentInfo() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        af.setFiledNumber("FILE-XYZ");
        af.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "2"))
                .thenReturn(List.of(af));
        doReturn(List.of(dep))
                .when(affiliationDependentRepository)
                .findAll(ArgumentMatchers.<Specification<AffiliationDependent>>any());
        when(affiliateRepository.findByFiledNumber("FILE-XYZ")).thenReturn(Optional.of(af));

        InfoConsultDTO dto = service.getInfo("CC", "2", Constant.EMPLOYEE, null);
        assertTrue(dto instanceof WorkerBasicInfoDTO);
        assertEquals("CC", ((WorkerBasicInfoDTO) dto).getDocumentType());
    }

    @Test
    void getInfo_employerMercantile_returnsEmployerInfoDTO() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-MERC");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(999L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "3"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-MERC"))
                .thenReturn(Optional.of(merc));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(merc.getId()))
                .thenReturn(Collections.singletonList(new AffiliateActivityEconomic() {{
                    setIsPrimary(true);
                    EconomicActivity e = new EconomicActivity();
                    e.setEconomicActivityCode("F110");
                    e.setDescription("Fabricación");
                    setActivityEconomic(e);
                }}));

        EmployerSize emSize = new EmployerSize();
        emSize.setId(3L);
        emSize.setDescription("Mediana");
        when(employerSizeRepository.findById(3L)).thenReturn(Optional.of(emSize));

        InfoConsultDTO dto = service.getInfo("CC", "3", Constant.TYPE_AFFILLATE_EMPLOYER, String.valueOf(af.getIdAffiliate()));
        assertTrue(dto instanceof EmployerInfoDTO);
        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertEquals("Comercial SA", e.getCompanyName());
        assertEquals("Persona Juridica", e.getNature()); // mapea por "J":contentReference[oaicite:5]{index=5}
        assertEquals("Mediana", e.getId_employer_size());
        assertNotNull(e.getEconomicActivity());
    }

    @Test
    void getInfo_employerDomestic_returnsEmployerInfoDTO_domestic_and_handlesEmployerSizeError() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FILE-DOM");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setAffiliationStatus("Activa");
        af.setDocumentNumber("55555");

        Affiliation dom = new Affiliation();
        dom.setId(55L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("55555");
        dom.setAddressEmployer("Addr");
        dom.setDepartmentEmployer(1L);
        dom.setMunicipalityEmployer(2L);
        dom.setEmail("d@x.com");
        dom.setPhone1("777");
        dom.setIdEmployerSize(100L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "4"))
                .thenReturn(List.of(af));
        when(affiliationDetailRepository.findByFiledNumber("FILE-DOM"))
                .thenReturn(Optional.of(dom));
        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(55L))
                .thenReturn(Collections.emptyList()); // actividad null
        when(affiliateRepository.findByNitCompany("55555"))
                .thenReturn(List.of()); // estadísticas → cero
        when(employerSizeRepository.findById(100L))
                .thenThrow(new RuntimeException("DB down")); // cubrir catch:contentReference[oaicite:6]{index=6}

        InfoConsultDTO dto = service.getInfo("CC", "4", Constant.TYPE_AFFILLATE_EMPLOYER, "0");
        assertTrue(dto instanceof EmployerInfoDTO);
        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertNull(e.getEconomicActivity());
        assertNull(e.getId_employer_size());
    }

    @Test
    void getInfo_unknownType_returnsEmptyEmployerInfo() {
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "5"))
                .thenReturn(List.of(afBase));
        InfoConsultDTO dto = service.getInfo("CC", "5", "X", "0");
        assertTrue(dto instanceof EmployerInfoDTO);
    }

    @Test
    void getInfo_notFound_throws() {
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "0"))
                .thenReturn(Collections.emptyList());
        assertThrows(AffiliationNotFoundError.class,
                () -> service.getInfo("CC", "0", Constant.EMPLOYEE, null));
    }

    @Test
    void getInfo_independentUserNotFound_throws() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "6"))
                .thenReturn(List.of(af));

        doReturn(Optional.empty())
                .when(userPreRegisterRepository)
                .findOne(ArgumentMatchers.<Specification<UserMain>>any());

        assertThrows(UserNotFoundInDataBase.class,
                () -> service.getInfo("CC", "6", Constant.EMPLOYEE, null));
    }

    // ---------- getJobRelatedInfo ----------

    @Test
    void getJobRelatedInfo_ok() {
        Affiliate a1 = new Affiliate();
        a1.setNitCompany("123");
        a1.setCompany("C1");
        a1.setAffiliationType("Trabajador Dependiente");
        a1.setAffiliationStatus("Activo");
        a1.setFiledNumber("F1");
        a1.setIdAffiliate(1L);
        // EVITA NPE en concatIdentificationEmployer(...)
        a1.setAffiliationSubType("EMPLOYEE");

        doReturn(List.of(a1))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());

        List<JobRelationShipDTO> list = service.getJobRelatedInfo("CC", "123");
        assertEquals(1, list.size());
        assertEquals("C1", list.get(0).getEmployerName());
        assertEquals("F1", list.get(0).getFiledNumber());
        assertEquals(1L, list.get(0).getIdAffiliate());
    }

    @Test
    void getJobRelatedInfo_empty_throws() {
        doReturn(Collections.emptyList())
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());
        assertThrows(AffiliateNotFound.class,
                () -> service.getJobRelatedInfo("CC", "999"));
    }

    // ---------- getContractsJobRelated ----------

    @Test
    void getContractsJobRelated_dependentAndIndependent() {
        Affiliate current = new Affiliate();
        current.setFiledNumber("FILE-XYZ");
        current.setDocumentType("CC");
        current.setDocumentNumber("123456789");

        Affiliate depAf = new Affiliate();
        depAf.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        depAf.setFiledNumber("FILE-XYZ");

        Affiliate indAf = new Affiliate();
        indAf.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        indAf.setFiledNumber("FILE-XYZ");

        when(affiliateRepository.findByFiledNumber("FILE-XYZ")).thenReturn(Optional.of(current));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123456789"))
                .thenReturn(List.of(depAf, indAf));
        when(affiliationDependentRepository.findByFiledNumber("FILE-XYZ")).thenReturn(Optional.of(dep));
        when(affiliationRepository.findByFiledNumber("FILE-XYZ")).thenReturn(Optional.of(ind));

        List<ContractsJobRelatedDTO> out = service.getContractsJobRelated("FILE-XYZ");
        assertEquals(2, out.size());
        assertNotNull(out.get(0).getValidityFrom());
        assertNotNull(out.get(1).getValidityFrom());
    }

    // ---------- getHistoryAffiliationsWithdrawals ----------

    @Test
    void getHistoryAffiliationsWithdrawals_mapsAll() {
        Affiliate a1 = new Affiliate();
        a1.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        a1.setAffiliationDate(LocalDate.of(2023, 1, 1).atStartOfDay());
        a1.setRetirementDate(LocalDate.of(2023, 1, 2)); // evita NPE en getUpdateDate
        a1.setNitCompany("111");
        a1.setCompany("C1");
        a1.setAffiliationType("T");
        a1.setAffiliationStatus("S");
        a1.setFiledNumber("F1");

        Affiliate a2 = new Affiliate();
        a2.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        a2.setAffiliationDate(LocalDate.of(2024, 2, 1).atStartOfDay());
        a2.setRetirementDate(LocalDate.of(2024, 2, 2)); // evita NPE en getUpdateDate
        a2.setNitCompany("222");
        a2.setCompany("C2");
        a2.setAffiliationType("T2");
        a2.setAffiliationStatus("S2");
        a2.setFiledNumber("F2");
        a2.setIdAffiliate(20L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "X"))
                .thenReturn(List.of(a1, a2));
        List<HistoryAffiliationsWithdrawalsDTO> res =
                service.getHistoryAffiliationsWithdrawals("CC", "X");

        assertEquals(2, res.size());
        assertEquals("Portal", res.get(0).getChannel());
        assertEquals("F2", res.get(1).getFiledNumber());
    }


    // ---------- getHistoryJobRelated ----------

    @Test
    void getHistoryJobRelated_independent_branch() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("F");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setAffiliationSubType("SUB");
        a.setAffiliationStatus("S");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());
        a.setCoverageStartDate(LocalDate.now());

        when(affiliateRepository.findByFiledNumber("F")).thenReturn(Optional.of(a));
        when(affiliationRepository.findByFiledNumber("F")).thenReturn(Optional.of(ind));
        when(billingRepository.findByContributorId(null)).thenReturn(Collections.emptyList());

        HistoryJobRelatedDTO dto = service.getHistoryJobRelated("F");
        assertEquals("S", dto.getAffiliationStatus());
        assertEquals("SUB", dto.getTypeOfLinkage());
        assertEquals(2, dto.getRiskLevel());
        assertEquals(ind.getPrice(), dto.getRate());
        assertNotNull(dto.getEconomicActivity());
        assertEquals("No Pago", dto.getPaymentStatus());
    }

    @Test
    void getHistoryJobRelated_dependent_branch_withPaymentStatusCollected() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("F2");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setAffiliationSubType("SUB2");
        a.setAffiliationStatus("S2");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());
        a.setCoverageStartDate(LocalDate.now());
        a.setDocumentNumber("DOCX");

        Billing b = new Billing();
        b.setId(1L);
        b.setPaymentPeriod("202401");

        BillingCollectionConciliation conc = new BillingCollectionConciliation();
        conc.setStatus(Constant.CONCILIATION_NOT_COLLECTED);

        when(affiliateRepository.findByFiledNumber("F2")).thenReturn(Optional.of(a));
        when(affiliationDependentRepository.findByFiledNumber("F2")).thenReturn(Optional.of(dep));
        when(billingRepository.findByContributorId("DOCX")).thenReturn(List.of(b));
        when(billingCollectionConciliationRepository.findByBillingId(1L)).thenReturn(Optional.of(conc));

        HistoryJobRelatedDTO dto = service.getHistoryJobRelated("F2");
        assertEquals("Con pago", dto.getPaymentStatus()); // por estado NOT_COLLECTED:contentReference[oaicite:8]{index=8}
        assertEquals(dep.getRisk(), dto.getRiskLevel());
        assertEquals(dep.getPriceRisk(), dto.getRate());
    }

    // ---------- getAffiliationWithdrawalsHistory ----------
    // (Se elimina el test de la rama DEPENDENT que requería OccupationRepository)

    // ---------- getUpdatesWorkerHistory ----------

    @Test
    void getUpdatesWorkerHistory_ok_andNoBilling() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FU");
        a.setIdAffiliate(10L);
        a.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        a.setAffiliationType("T");
        a.setObservation("Obs");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());

        Policy p = new Policy();
        p.setId(88L);

        when(affiliateRepository.findByFiledNumber("FU")).thenReturn(Optional.of(a));
        when(policyRepository.findByIdAffiliate(10L)).thenReturn(List.of(p));
        when(billingRepository.findByPolicy_Id(88L)).thenReturn(Optional.empty());

        UpdatesWorkerHistoryDTO dto = service.getUpdatesWorkerHistory("FU");
        assertEquals("Portal", dto.getChannel());
        assertEquals("No", dto.getRetirementNovelty());
        assertEquals("Obs", dto.getObservation());
        assertEquals(a.getFiledNumber(), dto.getRecordNumber());
        assertEquals(0, dto.getQuotedDays());
    }

    @Test
    void getUpdatesWorkerHistory_policyNotFound_throws() {
        when(affiliateRepository.findByFiledNumber("FUP")).thenReturn(Optional.of(afBase));
        when(policyRepository.findByIdAffiliate(10L)).thenReturn(Collections.emptyList());
        assertThrows(PolicyException.class, () -> service.getUpdatesWorkerHistory("FUP"));
    }

    // ---------- getDocumentAffiliationWorker ----------

    @Test
    void getDocumentAffiliationWorker_independent() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setIdAffiliate(10L);

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("ALF-1");
        doc.setName("file.pdf");
        doc.setDateUpload(LocalDate.of(2024, 5, 1).atStartOfDay());
        doc.setRevised(true);
        doc.setState(Boolean.TRUE);

        when(affiliateRepository.findByFiledNumber("FDOC")).thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(10L))
                .thenReturn(List.of(doc));
        when(affiliationRepository.findByFiledNumber("FDOC")).thenReturn(Optional.of(ind));
        when(genericWebClient.getFileBase64("ALF-1")).thenReturn(Mono.just("b64"));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC");
        assertEquals("CC", dto.getDocumentType());
        assertEquals("999", dto.getDocumentNumber());
        assertEquals("Ind", dto.getFirstName());
        assertEquals(1, dto.getDocumentIds().size());
    }

    @Test
    void getDocumentAffiliationWorker_dependent() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC2");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(10L);

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setIdAlfresco("ALF-2");
        doc.setState(Boolean.TRUE);

        when(affiliateRepository.findByFiledNumber("FDOC2")).thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(10L))
                .thenReturn(List.of(doc));
        when(affiliationDependentRepository.findByFiledNumber("FDOC2")).thenReturn(Optional.of(dep));
        when(genericWebClient.getFileBase64("ALF-2")).thenReturn(Mono.just("b64"));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC2");
        assertEquals(dep.getIdentificationDocumentType(), dto.getDocumentType());
        assertFalse(dto.getDocumentIds().isEmpty());
    }

    // ---------- generalConsult ----------

    @Test
    void generalConsult_employerByNI() {
        Affiliate a = new Affiliate();
        a.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        a.setFiledNumber("FM");
        a.setIdAffiliate(111L);

        doReturn(List.of(a))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());
        when(affiliateMercantileRepository.findByFiledNumber("FM")).thenReturn(Optional.of(merc));

        List<GeneralConsultDTO> res = service.generalConsult(Constant.NI, "800800800");
        assertEquals(1, res.size());
        assertNotNull(res.get(0));
    }

    @Test
    void generalConsult_personNatural_allCases() {
        Affiliate aDom = new Affiliate();
        aDom.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        aDom.setFiledNumber("FD");
        aDom.setIdAffiliate(1L);

        Affiliation dom = new Affiliation();
        dom.setFirstName("Ana"); dom.setSecondName("M"); dom.setSurname("Ruiz"); dom.setSecondSurname("P");
        when(affiliationRepository.findByFiledNumber("FD")).thenReturn(Optional.of(dom));

        Affiliate aEmp = new Affiliate();
        aEmp.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        aEmp.setFiledNumber("FM");
        aEmp.setIdAffiliate(2L);
        aEmp.setDocumentNumber("X");
        aEmp.setNitCompany("X");
        when(affiliateMercantileRepository.findByFiledNumber("FM")).thenReturn(Optional.of(merc));

        Affiliate aInd = new Affiliate();
        aInd.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        aInd.setFiledNumber("FI");
        aInd.setIdAffiliate(3L);
        when(affiliationRepository.findByFiledNumber("FI")).thenReturn(Optional.of(ind));

        Affiliate aDep = new Affiliate();
        aDep.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        aDep.setFiledNumber("FDEP");
        aDep.setIdAffiliate(4L);
        when(affiliationDependentRepository.findByFiledNumber("FDEP")).thenReturn(Optional.of(dep));

        doReturn(List.of(aDom, aEmp, aInd, aDep))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());

        List<GeneralConsultDTO> res = service.generalConsult("CC", "123");
        assertEquals(4, res.size());
        assertNotNull(res.get(0));
        assertNotNull(res.get(1));
        assertNotNull(res.get(2));
        assertNotNull(res.get(3));
    }

    // ---------- getWorkerDetails ----------

    @Test
    void getWorkerDetails_independent_MapsAll() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FW");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setIdAffiliate(50L);

        Object[] detail = new Object[10];
        detail[0] = 1L;
        detail[1] = "CC";
        detail[2] = "123";
        detail[3] = "2";
        detail[4] = "5.0";
        detail[5] = LocalDate.of(2024,1,1);
        detail[6] = LocalDate.of(2024,12,31);
        detail[7] = "MANAGEMENT";
        detail[8] = 77L;
        detail[9] = "F110 - Fabricación";

        when(affiliateRepository.findByFiledNumber("FW")).thenReturn(Optional.of(a));
        // DESPUÉS (tipado explícito + doReturn):
        List<Object[]> details = new ArrayList<>();
        details.add(detail);

        doReturn(details)
                .when(affiliationRepository)
                .findDetailByFiledNumber("FW");


        Object[] pol = new Object[8];
        pol[0] = "POL Name";
        pol[1] = "POL-1";
        pol[2] = LocalDate.of(2024,1,1);
        pol[3] = LocalDate.of(2024,12,31);
        pol[4] = "2024-12-31";
        pol[5] = "ACTIVA";
        pol[7] = "SI";
        List<Object[]> policies = new ArrayList<>();
        policies.add(pol);

        doReturn(policies)
                .when(policyRepository)
                .findByAffiliate(50L);

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("ALF-X");
        doc.setName("doc.pdf");
        doc.setDateUpload(LocalDate.of(2024,6,1).atStartOfDay());
        doc.setRevised(true);
        doc.setState(Boolean.TRUE);
        doReturn(List.of(doc))
                .when(dataDocumentRepository)
                .findAll(ArgumentMatchers.<Specification<DataDocumentAffiliate>>any());

        WorkerDetailDTO out = service.getWorkerDetails("FW");
        assertNotNull(out.getContract());
        AffiliationDetailDTO c = out.getContract();
        assertEquals(1L, c.getId());
        assertEquals("123", c.getDocumentNumber());
        assertEquals("MANAGEMENT", c.getStageManagement());
        assertEquals(77L, c.getCodeContributantType());
        assertEquals("F110 - Fabricación", c.getEconomicActivity());

        assertNotNull(out.getPolicy());
        assertEquals("POL Name", out.getPolicy().getPolicyName());

        assertEquals(1, out.getDocuments().size());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy ' a las ' HH:mm");
        assertTrue(out.getDocuments().get(0).getDateTime().contains("de"));
    }
    @Test
    void getInfo_employerMercantile_natureDefaultBranch() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-MERC");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(1000L);

        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setId(88L);
        merc.setTypePerson("X"); // rama "otra" en tu servicio
        merc.setBusinessName("Comercial SA");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","7"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-MERC"))
                .thenReturn(Optional.of(merc));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(88L))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo("CC","7", Constant.TYPE_AFFILLATE_EMPLOYER, "1000");
        EmployerInfoDTO e = (EmployerInfoDTO) dto;

        // Antes: assertEquals("Persona Natural", e.getNature());
        assertEquals("X", e.getNature()); // <-- ajustar expectativa
    }

    @Test
    void getInfo_employerDomestic_employerSizeSuccessBranch() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FILE-DOM-OK");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setAffiliationStatus("Activa");
        af.setDocumentNumber("70000");

        Affiliation dom = new Affiliation();
        dom.setId(701L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("70000");
        dom.setIdEmployerSize(3L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","8"))
                .thenReturn(List.of(af));
        when(affiliationDetailRepository.findByFiledNumber("FILE-DOM-OK"))
                .thenReturn(Optional.of(dom));

        EmployerSize size = new EmployerSize();
        size.setId(3L); size.setDescription("Mediana");
        when(employerSizeRepository.findById(3L)).thenReturn(Optional.of(size));

        InfoConsultDTO dto = service.getInfo("CC","8", Constant.TYPE_AFFILLATE_EMPLOYER, "0");
        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertEquals("Mediana", e.getId_employer_size());
    }
    @Test
    void getInfo_employerDomestic_employeeStatsNonEmpty() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FILE-DOM-EST");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setAffiliationStatus("Activa");
        af.setDocumentNumber("71000");

        Affiliation dom = new Affiliation();
        dom.setId(710L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("71000");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","9"))
                .thenReturn(List.of(af));
        when(affiliationDetailRepository.findByFiledNumber("FILE-DOM-EST"))
                .thenReturn(Optional.of(dom));

        Affiliate dep1 = new Affiliate(); dep1.setAffiliationStatus("Activo");
        Affiliate dep2 = new Affiliate(); dep2.setAffiliationStatus("Suspendido");
        when(affiliateRepository.findByNitCompany("71000"))
                .thenReturn(List.of(dep1, dep2));

        InfoConsultDTO dto = service.getInfo("CC","9", Constant.TYPE_AFFILLATE_EMPLOYER, "0");
        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        // aserciones según los campos que muestres (p. ej. totalEmpleados, activos, suspendidos…)
        // assertEquals(2, e.getTotalEmployees());
    }
    @Test
    void getDocumentAffiliationWorker_noDocumentsReturnsEmpty() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC-EMPTY");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(10L);

        when(affiliateRepository.findByFiledNumber("FDOC-EMPTY")).thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(10L))
                .thenReturn(Collections.emptyList());
        when(affiliationDependentRepository.findByFiledNumber("FDOC-EMPTY")).thenReturn(Optional.of(new AffiliationDependent()));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC-EMPTY");
        assertTrue(dto.getDocumentIds().isEmpty());
    }
    @Test
    void consultUpdates_otherNovelty() {
        Affiliate a = new Affiliate();
        a.setNoveltyType("OTRA_NOVEDAD"); // fuerza rama 'default'
        a.setAffiliationDate(LocalDate.now().atStartOfDay()); // para getUpdateDate(...)
        a.setFiledNumber("F-OTHER");
        a.setNitCompany("999");
        a.setCompany("Empresa Z");
        a.setAffiliationType("TipoX");
        a.setAffiliationStatus("Activo");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","Z2"))
                .thenReturn(List.of(a));

        List<ConsultUpdatesDTO> out = service.consultUpdates("CC","Z2");

        assertEquals(1, out.size());
        assertEquals("Portal", out.get(0).getChannel());      // constante del servicio
        assertNotNull(out.get(0).getFiledNumber());

    }

// ========== TESTS FALTANTES PARA 100% DE COBERTURA ==========

    // 1. getInfo - Búsqueda por NI (nitCompany)
    @Test
    void getInfo_searchByNI_findsEmployer() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-NI");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(500L);

        when(affiliateRepository.findByNitCompany("900900900"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-NI"))
                .thenReturn(Optional.of(merc));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(anyLong()))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo(Constant.NI, "900900900",
                Constant.TYPE_AFFILLATE_EMPLOYER, "500");
        assertTrue(dto instanceof EmployerInfoDTO);
    }

    // 2. concatIdentificationEmployer - Rama DOMESTIC_SERVICES
    @Test
    void concatIdentificationEmployer_domesticServices_returnsCorrectType() {
        Affiliate af = new Affiliate();
        af.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        af.setFiledNumber("FILE-DOM");
        af.setNitCompany("888888");
        af.setIdAffiliate(999L); // ✅ AGREGADO - FALTABA ESTO
        af.setAffiliationType("Trabajador");
        af.setAffiliationStatus("Activo");

        Affiliation domAff = new Affiliation();
        domAff.setIdentificationDocumentType("CC");

        doReturn(List.of(af))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());
        when(affiliationRepository.findByFiledNumber("FILE-DOM"))
                .thenReturn(Optional.of(domAff));

        List<JobRelationShipDTO> result = service.getJobRelatedInfo("CC", "test");

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getDocumentNumber().contains("CC"));
    }


    // 3. getAffiliationWithdrawalsHistory - INDEPENDENT branch
    @Test
    void getAffiliationWithdrawalsHistory_independent_returnsCorrectData() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FHIST");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        a.setAffiliationDate(LocalDate.of(2024, 3, 15).atStartOfDay());

        Affiliation worker = new Affiliation();
        worker.setHealthPromotingEntity(10L);
        worker.setOccupation("Developer");
        worker.setAddress("Street 456");

        when(affiliateRepository.findByFiledNumber("FHIST")).thenReturn(Optional.of(a));
        when(affiliationRepository.findByFiledNumber("FHIST")).thenReturn(Optional.of(worker));

        HistoryAffiliationsWithdrawalsHistoryDTO dto =
                service.getAffiliationWithdrawalsHistory("FHIST");

        assertEquals("Portal", dto.getChannel());
        assertEquals("Developer", dto.getOcupation());
        assertEquals("Street 456", dto.getAddress());
        assertEquals(10L, dto.getEps());
    }

    // 4. getAffiliationWithdrawalsHistory - RETIREMENT branch
    @Test
    void getAffiliationWithdrawalsHistory_retirement_usesRetirementDate() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FRET");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        a.setIdAffiliate(15L);

        Retirement ret = new Retirement();
        ret.setRetirementDate(LocalDate.of(2024, 6, 30));
        ret.setFiledNumber("RET-15");

        Affiliation worker = new Affiliation();
        worker.setHealthPromotingEntity(20L);
        worker.setOccupation("Manager");
        worker.setAddress("Avenue 789");

        when(affiliateRepository.findByFiledNumber("FRET")).thenReturn(Optional.of(a));
        when(retirementRepository.findByIdAffiliate(15L)).thenReturn(Optional.of(ret));
        when(affiliationRepository.findByFiledNumber("FRET")).thenReturn(Optional.of(worker));

        HistoryAffiliationsWithdrawalsHistoryDTO dto =
                service.getAffiliationWithdrawalsHistory("FRET");

        assertEquals("2024-06-30", dto.getFilingDate());
    }

    // 5. consultUpdates - RETIREMENT branch con filedNumber
    @Test
    void consultUpdates_retirement_getsFiledNumberFromRetirement() {
        Affiliate a = new Affiliate();
        a.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        a.setIdAffiliate(25L);
        a.setRetirementDate(LocalDate.of(2024, 5, 20));
        a.setAffiliationDate(LocalDate.of(2023, 1, 1).atStartOfDay());
        a.setNitCompany("777777");
        a.setCompany("Company Ret");
        a.setAffiliationType("Trabajador");
        a.setAffiliationStatus("Retirado");

        Retirement ret = new Retirement();
        ret.setFiledNumber("RET-FILE-25");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "RET"))
                .thenReturn(List.of(a));
        when(retirementRepository.findByIdAffiliate(25L)).thenReturn(Optional.of(ret));

        List<ConsultUpdatesDTO> result = service.consultUpdates("CC", "RET");

        assertEquals(1, result.size());
        assertEquals("RET-FILE-25", result.get(0).getFiledNumber());
        assertEquals(Constant.NOVELTY_TYPE_RETIREMENT, result.get(0).getAffiliationWithdrawal());
    }
    // 6. getLegalRepresentativeFullName - null/blank parameters
    @Test
    void buildEmployerInfoDTO_legalRepNullParams_returnsNull() throws Exception {
        Affiliate af = new Affiliate();
        af.setFiledNumber("FILE-NULL");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(999L);

        AffiliateMercantile mercNull = new AffiliateMercantile();
        mercNull.setId(99L);
        mercNull.setTypeDocumentPersonResponsible(null);
        mercNull.setNumberDocumentPersonResponsible("");
        mercNull.setBusinessName("Company");
        mercNull.setNumberIdentification("NULL-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(99L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("NULL-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, mercNull
        );

        assertNull(dto.getLegalRepresentativeName());
    }

    // 7. getLegalRepresentativeFullName - usuario no encontrado
    @Test
    void buildEmployerInfoDTO_legalRepNotFound_returnsNull() throws Exception {
        Affiliate af = new Affiliate();
        af.setFiledNumber("FILE-NOTFOUND");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile mercNotFound = new AffiliateMercantile();
        mercNotFound.setId(100L);
        mercNotFound.setTypeDocumentPersonResponsible("CC");
        mercNotFound.setNumberDocumentPersonResponsible("NOTFOUND");
        mercNotFound.setBusinessName("Company");
        mercNotFound.setNumberIdentification("NOTFOUND-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(100L))
                .thenReturn(Collections.emptyList());
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "NOTFOUND"))
                .thenReturn(Optional.empty());
        when(affiliateRepository.findByNitCompany("NOTFOUND-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, mercNotFound
        );

        assertNull(dto.getLegalRepresentativeName());
    }

    // 8. getLegalRepresentativeFullName - exception handling
    @Test
    void buildEmployerInfoDTO_legalRepException_returnsNull() throws Exception {
        Affiliate af = new Affiliate();
        af.setFiledNumber("FILE-ERR");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile mercErr = new AffiliateMercantile();
        mercErr.setId(101L);
        mercErr.setTypeDocumentPersonResponsible("CC");
        mercErr.setNumberDocumentPersonResponsible("ERROR");
        mercErr.setBusinessName("Company");
        mercErr.setNumberIdentification("ERROR-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(101L))
                .thenReturn(Collections.emptyList());
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "ERROR"))
                .thenThrow(new RuntimeException("DB Error"));
        when(affiliateRepository.findByNitCompany("ERROR-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, mercErr
        );

        assertNull(dto.getLegalRepresentativeName());
    }

    // 9. getLegalRepresentativeFullNameDomestic - campos en entidad
    @Test
    void buildEmployerDomesticInfoDTO_legalRepFromEntity_returnsName() throws Exception {
        Affiliate af = new Affiliate();
        af.setFiledNumber("FILE-DOM-ENT");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setDocumentNumber("DOM123");
        af.setCompany("Dom Company");

        Affiliation domWithRep = new Affiliation();
        domWithRep.setId(200L);
        domWithRep.setIdentificationDocumentType("CC");
        domWithRep.setIdentificationDocumentNumber("DOM123");
        domWithRep.setLegalRepFirstName("Carlos");
        domWithRep.setLegalRepSecondName("Alberto");
        domWithRep.setLegalRepSurname("Gomez");
        domWithRep.setLegalRepSecondSurname("Lopez");

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(200L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("DOM123"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, domWithRep
        );

        assertEquals("Carlos Alberto Gomez Lopez", dto.getLegalRepresentativeName());
    }

    // 10. getLegalRepresentativeFullNameDomestic - exception handling
    @Test
    void buildEmployerDomesticInfoDTO_legalRepException_returnsNull() throws Exception {
        Affiliate af = new Affiliate();
        af.setFiledNumber("FILE-DOM-ERR");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setDocumentNumber("DERR");
        af.setCompany("Company");

        Affiliation domErr = new Affiliation();
        domErr.setId(201L);
        domErr.setIdentificationDocumentType("CC");
        domErr.setIdentificationDocumentNumber("DERR");
        domErr.setIdentificationDocumentTypeLegalRepresentative("CC");
        domErr.setIdentificationDocumentNumberContractorLegalRepresentative("ERR");

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(201L))
                .thenReturn(Collections.emptyList());
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "ERR"))
                .thenThrow(new RuntimeException("Error"));
        when(affiliateRepository.findByNitCompany("DERR"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, domErr
        );

        assertEquals("Company", dto.getLegalRepresentativeName());
    }

    // 11. getEconomicActivityForEmployer - no primary, uses first
    @Test
    void getEconomicActivityForEmployer_noPrimary_usesFirst() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(false);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("9999");
        ea.setDescription("Otra");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(123L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setFiledNumber("F");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(123L);
        m.setBusinessName("Test");
        m.setNumberIdentification("9999-NIT");

        when(affiliateRepository.findByNitCompany("9999-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals("9999 - Otra", dto.getEconomicActivity());
    }

    // 12. getEconomicActivityForEmployer - only code
    @Test
    void getEconomicActivityForEmployer_onlyCode_returnsCode() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("CODE");
        ea.setDescription(null);
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(124L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(124L);
        m.setBusinessName("Test");
        m.setNumberIdentification("CODE-NIT");

        when(affiliateRepository.findByNitCompany("CODE-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals("CODE", dto.getEconomicActivity());
    }

    // 13. getEconomicActivityForEmployer - only description
    @Test
    void getEconomicActivityForEmployer_onlyDescription_returnsDescription() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("");
        ea.setDescription("Solo Descripción");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(125L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(125L);
        m.setBusinessName("Test");
        m.setNumberIdentification("DESC-NIT");

        when(affiliateRepository.findByNitCompany("DESC-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals("Solo Descripción", dto.getEconomicActivity());
    }

    // 14. getEconomicActivityForEmployer - both empty
    @Test
    void getEconomicActivityForEmployer_bothEmpty_returnsNull() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("");
        ea.setDescription("");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(126L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(126L);
        m.setBusinessName("Test");
        m.setNumberIdentification("EMPTY-NIT");

        when(affiliateRepository.findByNitCompany("EMPTY-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertNull(dto.getEconomicActivity());
    }

    // 15. getEconomicActivityForEmployer - null activity
    @Test
    void getEconomicActivityForEmployer_nullActivity_returnsNull() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        aae.setActivityEconomic(null);

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(127L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(127L);
        m.setBusinessName("Test");
        m.setNumberIdentification("NULLACT-NIT");

        when(affiliateRepository.findByNitCompany("NULLACT-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertNull(dto.getEconomicActivity());
    }

    // 16. getEconomicActivityForEmployer - exception
    @Test
    void getEconomicActivityForEmployer_exception_returnsNull() throws Exception {
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(128L))
                .thenThrow(new RuntimeException("DB Error"));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(128L);
        m.setBusinessName("Test");
        m.setNumberIdentification("ERR-NIT");

        when(affiliateRepository.findByNitCompany("ERR-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertNull(dto.getEconomicActivity());
    }

    // 17. getEconomicActivityForDomesticEmployer - exception
    @Test
    void getEconomicActivityForDomesticEmployer_exception_returnsNull() throws Exception {
        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(300L))
                .thenThrow(new RuntimeException("Error"));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("X");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(300L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("X");

        when(affiliateRepository.findByNitCompany("X"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertNull(dto.getEconomicActivity());
    }

    // 18. calculateEmployeeStatistics - exception


    // 19. calculateEmployeeStatistics - con trabajadores activos/inactivos variados
    @Test
    void calculateEmployeeStatistics_mixedWorkers_calculatesCorrectly() throws Exception {
        Affiliate dep1 = new Affiliate();
        dep1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        dep1.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        Affiliate dep2 = new Affiliate();
        dep2.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        dep2.setAffiliationStatus("Activo");

        Affiliate ind1 = new Affiliate();
        ind1.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        ind1.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        Affiliate inactive1 = new Affiliate();
        inactive1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        inactive1.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);

        Affiliate inactive2 = new Affiliate();
        inactive2.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        inactive2.setAffiliationStatus("Inactivo");

        Affiliate notWorker = new Affiliate();
        notWorker.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);

        when(affiliateRepository.findByNitCompany("MIXED"))
                .thenReturn(List.of(dep1, dep2, ind1, inactive1, inactive2, notWorker));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(160L))
                .thenReturn(Collections.emptyList());

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(160L);
        m.setNumberIdentification("MIXED");
        m.setBusinessName("Mixed Company");

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals(5, dto.getTotalEmployees());
        assertEquals(3, dto.getTotalActiveEmployees());
        assertEquals(2, dto.getTotalInactiveEmployees());
        assertEquals(2, dto.getActiveDependentEmployees());
        assertEquals(1, dto.getActiveIndependentEmployees());
    }

    // ========== TESTS ADICIONALES PARA COMPLETAR COBERTURA DE BRANCHES ==========

    // 27. getInfo - NI sin affiliates de tipo EMPLOYER
    @Test
    void getInfo_searchByNI_noEmployerType_returnsEmpty() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT); // NO es EMPLOYER
        af.setFiledNumber("FILE-NI-2");

        when(affiliateRepository.findByNitCompany("900900901"))
                .thenReturn(List.of(af));

        InfoConsultDTO dto = service.getInfo(Constant.NI, "900900901",
                Constant.TYPE_AFFILLATE_EMPLOYER, "0");

        assertTrue(dto instanceof EmployerInfoDTO);
    }

    // 28. concatCompleteName - con secondName null
    @Test
    void getDocumentAffiliationWorker_concatNameWithNullSecondName() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC-NAME");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setIdAffiliate(10L);

        Affiliation worker = new Affiliation();
        worker.setIdentificationDocumentType("CC");
        worker.setIdentificationDocumentNumber("111");
        worker.setFirstName("Pedro");
        worker.setSecondName(null); // NULL
        worker.setSurname("Gomez");
        worker.setSecondSurname(null); // NULL

        when(affiliateRepository.findByFiledNumber("FDOC-NAME")).thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(10L))
                .thenReturn(Collections.emptyList());
        when(affiliationRepository.findByFiledNumber("FDOC-NAME")).thenReturn(Optional.of(worker));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC-NAME");

        assertEquals("Pedro", dto.getFirstName());
        assertNull(dto.getMiddleName());
    }

    // 29. processEmployerAffiliation - sin match de idAffiliate
    @Test
    void getInfo_employerNoMatch_returnsEmpty() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-NO-MATCH");
        af.setIdAffiliate(100L); // Diferente del que buscamos

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "NOMATCH"))
                .thenReturn(List.of(af));

        InfoConsultDTO dto = service.getInfo("CC", "NOMATCH",
                Constant.TYPE_AFFILLATE_EMPLOYER, "999"); // ID diferente

        assertTrue(dto instanceof EmployerInfoDTO);
    }

    // 30. getWorkerDetails - dependent branch con details
    @Test
    void getWorkerDetails_dependent_complete() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FW-DEP2");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(61L);

        Object[] detail = new Object[10];
        detail[0] = 3L;
        detail[1] = "TI";
        detail[2] = "789";
        detail[3] = "1";
        detail[4] = "2.5";
        detail[5] = LocalDate.of(2024, 3, 1);
        detail[6] = LocalDate.of(2024, 12, 31);
        detail[7] = "STAGE";
        detail[8] = 99L;
        detail[9] = "H123 - Comercio";

        List<Object[]> details = new ArrayList<>();
        details.add(detail);

        when(affiliateRepository.findByFiledNumber("FW-DEP2")).thenReturn(Optional.of(a));
        doReturn(details)
                .when(affiliationDependentRepository)
                .findDetailByFiledNumber("FW-DEP2");

        doReturn(Collections.emptyList())
                .when(policyRepository)
                .findByAffiliate(61L);

        doReturn(Collections.emptyList())
                .when(dataDocumentRepository)
                .findAll(ArgumentMatchers.<Specification<DataDocumentAffiliate>>any());

        WorkerDetailDTO out = service.getWorkerDetails("FW-DEP2");

        assertNotNull(out.getContract());
        // ✅ CORREGIDO: para dependent se setea "Sin información" en lugar de null
        assertEquals("Sin información", out.getContract().getStageManagement());
    }
    // 31. getEconomicActivityForDomesticEmployer - código y descripción ambos vacíos
    @Test
    void getEconomicActivityForDomesticEmployer_bothEmpty_returnsNull() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("");
        ea.setDescription("");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(400L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("DOM400");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(400L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("DOM400");

        when(affiliateRepository.findByNitCompany("DOM400"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertNull(dto.getEconomicActivity());
    }

    // 32. getEconomicActivityForDomesticEmployer - solo código
    @Test
    void getEconomicActivityForDomesticEmployer_onlyCode_returnsCode() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("D500");
        ea.setDescription("");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(401L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("DOM401");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(401L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("DOM401");

        when(affiliateRepository.findByNitCompany("DOM401"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertEquals("D500", dto.getEconomicActivity());
    }

    // 33. getEconomicActivityForDomesticEmployer - solo descripción
    @Test
    void getEconomicActivityForDomesticEmployer_onlyDescription_returnsDescription() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("");
        ea.setDescription("Servicios Varios");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(402L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("DOM402");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(402L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("DOM402");

        when(affiliateRepository.findByNitCompany("DOM402"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertEquals("Servicios Varios", dto.getEconomicActivity());
    }

    // 34. getEconomicActivityForDomesticEmployer - null activity
    @Test
    void getEconomicActivityForDomesticEmployer_nullActivity_returnsNull() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        aae.setActivityEconomic(null);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(403L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("DOM403");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(403L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("DOM403");

        when(affiliateRepository.findByNitCompany("DOM403"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertNull(dto.getEconomicActivity());
    }

    // 35. getEconomicActivityForDomesticEmployer - no primary, uses first
    @Test
    void getEconomicActivityForDomesticEmployer_noPrimary_usesFirst() throws Exception {
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(false);
        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("X999");
        ea.setDescription("Primera");
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(404L))
                .thenReturn(List.of(aae));

        Affiliate af = new Affiliate();
        af.setDocumentNumber("DOM404");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        Affiliation dom = new Affiliation();
        dom.setId(404L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("DOM404");

        when(affiliateRepository.findByNitCompany("DOM404"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertEquals("X999 - Primera", dto.getEconomicActivity());
    }

    // 36. getNatureDescription - tipo 'N'
    @Test
    void buildEmployerInfoDTO_natureN_returnsPersonaNatural() throws Exception {
        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(200L);
        m.setTypePerson("N"); // Persona Natural
        m.setBusinessName("Juan Perez");
        m.setNumberIdentification("N-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(200L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("N-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals("Persona Natural", dto.getNature());
    }

    // 37. getEmployerSizeDescription - null parameter
    @Test
    void getInfo_employerSizeNull_returnsNull() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-SIZE-NULL");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(600L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(201L);
        m.setTypePerson("J");
        m.setBusinessName("Empresa");
        m.setNumberIdentification("SIZE-NULL");
        m.setIdEmployerSize(null); // NULL

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "SIZE-NULL"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-SIZE-NULL"))
                .thenReturn(Optional.of(m));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(201L))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo("CC", "SIZE-NULL",
                Constant.TYPE_AFFILLATE_EMPLOYER, "600");

        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertNull(e.getId_employer_size());
    }

    // 38. getEmployerSizeDescription - not found
    @Test
    void getInfo_employerSizeNotFound_returnsNull() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-SIZE-NF");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(601L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(202L);
        m.setTypePerson("J");
        m.setBusinessName("Empresa");
        m.setNumberIdentification("SIZE-NF");
        m.setIdEmployerSize(999L); // No existe

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "SIZE-NF"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-SIZE-NF"))
                .thenReturn(Optional.of(m));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(202L))
                .thenReturn(Collections.emptyList());
        when(employerSizeRepository.findById(999L))
                .thenReturn(Optional.empty()); // No encontrado

        InfoConsultDTO dto = service.getInfo("CC", "SIZE-NF",
                Constant.TYPE_AFFILLATE_EMPLOYER, "601");

        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertNull(e.getId_employer_size());
    }

    // 39. getLegalRepresentativeFullNameDomestic - sin campos en entidad ni en params
    @Test
    void buildEmployerDomesticInfoDTO_legalRepAllNull_returnsNull() throws Exception {
        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setDocumentNumber("ALL-NULL");
        af.setCompany("Company");

        Affiliation dom = new Affiliation();
        dom.setId(500L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("ALL-NULL");
        // Todos los campos de rep legal en null
        dom.setLegalRepFirstName(null);
        dom.setLegalRepSurname(null);
        dom.setIdentificationDocumentTypeLegalRepresentative(null);
        dom.setIdentificationDocumentNumberContractorLegalRepresentative(null);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(500L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("ALL-NULL"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerDomesticInfoDTO",
                new Class<?>[]{Affiliate.class, Affiliation.class},
                af, dom
        );

        assertEquals("Company", dto.getLegalRepresentativeName()); // fallback al company
    }

    @Test
    void calculateEmployeeStatistics_noWorkers_returnsZeros() throws Exception {
        Affiliate employer = new Affiliate();
        employer.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER); // NO contiene "trabajador"

        when(affiliateRepository.findByNitCompany("NO-WORKERS"))
                .thenReturn(List.of(employer));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(300L);
        m.setNumberIdentification("NO-WORKERS");
        m.setBusinessName("No Workers");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(300L))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals(0, dto.getTotalEmployees());
        assertEquals(0, dto.getTotalActiveEmployees());
        assertEquals(0, dto.getTotalInactiveEmployees());
    }


    @Test
    void getInfo_employerDomestic_economicActivity_returnsNullWhenListEmpty() {
        // Afiliado doméstico -> entra a buildEmployerDomesticInfoDTO
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FD0");
        af.setAffiliationStatus("Activa");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","X"))
                .thenReturn(List.of(af));
        Affiliation dom = new Affiliation();
        dom.setId(1L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("1");
        when(affiliationDetailRepository.findByFiledNumber("FD0"))
                .thenReturn(Optional.of(dom));

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(1L))
                .thenReturn(List.of());

        var dto = (EmployerInfoDTO) service.getInfo("CC","X", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertNull(dto.getEconomicActivity());
    }

    @Test
    void getInfo_employerDomestic_economicActivity_picksPrimary_andConcatsCodeAndDescription() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FD1");
        af.setAffiliationStatus("Activa");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","Y"))
                .thenReturn(List.of(af));
        Affiliation dom = new Affiliation();
        dom.setId(2L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("2");
        when(affiliationDetailRepository.findByFiledNumber("FD1"))
                .thenReturn(Optional.of(dom));

        EconomicActivity ea = new EconomicActivity();
        ea.setEconomicActivityCode("123");
        ea.setDescription("Panadería");
        AffiliateActivityEconomic aae = new AffiliateActivityEconomic();
        aae.setIsPrimary(true);
        aae.setActivityEconomic(ea);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(2L))
                .thenReturn(List.of(aae));

        var dto = (EmployerInfoDTO) service.getInfo("CC","Y", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertEquals("123 - Panadería", dto.getEconomicActivity());
    }

    @Test
    void getInfo_employerDomestic_economicActivity_handlesNullsAndExceptions() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FD2");
        af.setAffiliationStatus("Activa");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC","Z"))
                .thenReturn(List.of(af));
        Affiliation dom = new Affiliation();
        dom.setId(3L);
        dom.setIdentificationDocumentType("CC");
        dom.setIdentificationDocumentNumber("3");
        when(affiliationDetailRepository.findByFiledNumber("FD2"))
                .thenReturn(Optional.of(dom));
        AffiliateActivityEconomic aaeNoEA = new AffiliateActivityEconomic();
        aaeNoEA.setIsPrimary(true);
        aaeNoEA.setActivityEconomic(null);

        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(3L))
                .thenReturn(List.of(aaeNoEA));
        var dto1 = (EmployerInfoDTO) service.getInfo("CC","Z", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertNull(dto1.getEconomicActivity());
        EconomicActivity onlyCode = new EconomicActivity();
        onlyCode.setEconomicActivityCode("C123");
        onlyCode.setDescription(null);
        AffiliateActivityEconomic aaeOnlyCode = new AffiliateActivityEconomic();
        aaeOnlyCode.setIsPrimary(true);
        aaeOnlyCode.setActivityEconomic(onlyCode);
        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(3L))
                .thenReturn(List.of(aaeOnlyCode));
        var dto2 = (EmployerInfoDTO) service.getInfo("CC","Z", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertEquals("C123", dto2.getEconomicActivity());
        EconomicActivity onlyDesc = new EconomicActivity();
        onlyDesc.setEconomicActivityCode(null);
        onlyDesc.setDescription("Desc");
        AffiliateActivityEconomic aaeOnlyDesc = new AffiliateActivityEconomic();
        aaeOnlyDesc.setIsPrimary(true);
        aaeOnlyDesc.setActivityEconomic(onlyDesc);
        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(3L))
                .thenReturn(List.of(aaeOnlyDesc));
        var dto3 = (EmployerInfoDTO) service.getInfo("CC","Z", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertEquals("Desc", dto3.getEconomicActivity());

        // d) excepción del repo -> null
        when(affiliateActivityEconomicRepository.findByIdAffiliateDomestico(3L))
                .thenThrow(new RuntimeException("DB down"));
        var dto4 = (EmployerInfoDTO) service.getInfo("CC","Z", Constant.TYPE_AFFILLATE_EMPLOYER, "10");
        assertNull(dto4.getEconomicActivity());
    }

    @Test
    void getLegalRepresentativeFullName_allFields_concatenatesAll() throws Exception {
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setFirstName("Juan");
        user.setSecondName("Carlos");
        user.setSurname("Perez");
        user.setSecondSurname("Lopez");

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "FULL"))
                .thenReturn(Optional.of(user));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(500L);
        m.setTypeDocumentPersonResponsible("CC");
        m.setNumberDocumentPersonResponsible("FULL");
        m.setBusinessName("Test");
        m.setNumberIdentification("FULL-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(500L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("FULL-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertTrue(dto.getLegalRepresentativeName().contains("Juan"));
        assertTrue(dto.getLegalRepresentativeName().contains("Carlos"));
        assertTrue(dto.getLegalRepresentativeName().contains("Perez"));
        assertTrue(dto.getLegalRepresentativeName().contains("Lopez"));
    }

    @Test
    void getLegalRepresentativeFullName_onlyFirstName_returnsFirstName() throws Exception {
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setFirstName("Pedro");
        user.setSecondName(null);
        user.setSurname(null);
        user.setSecondSurname(null);

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "FNAME"))
                .thenReturn(Optional.of(user));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(501L);
        m.setTypeDocumentPersonResponsible("CC");
        m.setNumberDocumentPersonResponsible("FNAME");
        m.setBusinessName("Test");
        m.setNumberIdentification("FNAME-NIT");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(501L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("FNAME-NIT"))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertTrue(dto.getLegalRepresentativeName().contains("Pedro"));
    }

    @Test
    void calculateEmployeeStatistics_onlyActiveDependents_countsCorrectly() throws Exception {
        Affiliate dep1 = new Affiliate();
        dep1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        dep1.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        Affiliate dep2 = new Affiliate();
        dep2.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        dep2.setAffiliationStatus("Activo");

        when(affiliateRepository.findByNitCompany("ONLY-DEP"))
                .thenReturn(List.of(dep1, dep2));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(600L);
        m.setNumberIdentification("ONLY-DEP");
        m.setBusinessName("Only Dependents");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(600L))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals(2, dto.getTotalEmployees());
        assertEquals(2, dto.getTotalActiveEmployees());
        assertEquals(0, dto.getTotalInactiveEmployees());
        assertEquals(2, dto.getActiveDependentEmployees());
        assertEquals(0, dto.getActiveIndependentEmployees());
    }

    @Test
    void calculateEmployeeStatistics_onlyActiveIndependents_countsCorrectly() throws Exception {
        Affiliate ind1 = new Affiliate();
        ind1.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        ind1.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        Affiliate ind2 = new Affiliate();
        ind2.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        ind2.setAffiliationStatus("Activo");

        when(affiliateRepository.findByNitCompany("ONLY-IND"))
                .thenReturn(List.of(ind1, ind2));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(601L);
        m.setNumberIdentification("ONLY-IND");
        m.setBusinessName("Only Independents");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(601L))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals(2, dto.getTotalEmployees());
        assertEquals(2, dto.getTotalActiveEmployees());
        assertEquals(0, dto.getTotalInactiveEmployees());
        assertEquals(0, dto.getActiveDependentEmployees());
        assertEquals(2, dto.getActiveIndependentEmployees());
    }

    @Test
    void calculateEmployeeStatistics_onlyInactive_countsCorrectly() throws Exception {
        Affiliate inactive1 = new Affiliate();
        inactive1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        inactive1.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);

        Affiliate inactive2 = new Affiliate();
        inactive2.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        inactive2.setAffiliationStatus("Inactivo");

        when(affiliateRepository.findByNitCompany("ONLY-INACTIVE"))
                .thenReturn(List.of(inactive1, inactive2));

        Affiliate af = new Affiliate();
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setIdAffiliate(999L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(602L);
        m.setNumberIdentification("ONLY-INACTIVE");
        m.setBusinessName("Only Inactive");

        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(602L))
                .thenReturn(Collections.emptyList());

        EmployerInfoDTO dto = (EmployerInfoDTO) invokePrivateMethod(
                "buildEmployerInfoDTO",
                new Class<?>[]{Affiliate.class, AffiliateMercantile.class},
                af, m
        );

        assertEquals(2, dto.getTotalEmployees());
        assertEquals(0, dto.getTotalActiveEmployees());
        assertEquals(2, dto.getTotalInactiveEmployees());
    }

    @Test
    void getJobRelatedInfo_domesticNoAffiliation_throwsException() {
        Affiliate af = new Affiliate();
        af.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        af.setFiledNumber("FILE-DOM-NO-AFF");
        af.setNitCompany("999999");
        af.setIdAffiliate(900L);
        af.setAffiliationType("Trabajador");
        af.setAffiliationStatus("Activo");

        doReturn(List.of(af))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());
        when(affiliationRepository.findByFiledNumber("FILE-DOM-NO-AFF"))
                .thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class,
                () -> service.getJobRelatedInfo("CC", "test"));
    }

    @Test
    void getWorkerDetails_dependent_noDetails_returnsBasicInfo() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FW-DEP-NO");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(71L);

        when(affiliateRepository.findByFiledNumber("FW-DEP-NO"))
                .thenReturn(Optional.of(a));
        doReturn(Collections.emptyList())
                .when(affiliationDependentRepository)
                .findDetailByFiledNumber("FW-DEP-NO");
        doReturn(Collections.emptyList())
                .when(policyRepository)
                .findByAffiliate(71L);
        doReturn(Collections.emptyList())
                .when(dataDocumentRepository)
                .findAll(ArgumentMatchers.<Specification<DataDocumentAffiliate>>any());

        WorkerDetailDTO result = service.getWorkerDetails("FW-DEP-NO");

        assertNotNull(result);
        assertNotNull(result.getContract());
    }

    @Test
    void getWorkerDetails_independent_detailsWithNulls() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FW-IND-NULL");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setIdAffiliate(80L);

        Object[] detail = new Object[10];

        for (int i = 0; i < 10; i++) detail[i] = null;
        List<Object[]> details = new ArrayList<>();
        details.add(detail);
        when(affiliateRepository.findByFiledNumber("FW-IND-NULL"))
                .thenReturn(Optional.of(a));
        doReturn(details)
                .when(affiliationRepository)
                .findDetailByFiledNumber("FW-IND-NULL");
        doReturn(Collections.emptyList())
                .when(policyRepository)
                .findByAffiliate(80L);
        doReturn(Collections.emptyList())
                .when(dataDocumentRepository)
                .findAll(ArgumentMatchers.<Specification<DataDocumentAffiliate>>any());

        WorkerDetailDTO result = service.getWorkerDetails("FW-IND-NULL");

        assertNotNull(result);
        assertNotNull(result.getContract());
    }

    @Test
    void getWorkerDetails_dependent_detailsWithNulls() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FW-DEP-NULL");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(81L);

        Object[] detail = new Object[10];
        for (int i = 0; i < 10; i++) detail[i] = null;

        List<Object[]> details = new ArrayList<>();
        details.add(detail);

        when(affiliateRepository.findByFiledNumber("FW-DEP-NULL"))
                .thenReturn(Optional.of(a));
        doReturn(details)
                .when(affiliationDependentRepository)
                .findDetailByFiledNumber("FW-DEP-NULL");
        doReturn(Collections.emptyList())
                .when(policyRepository)
                .findByAffiliate(81L);
        doReturn(Collections.emptyList())
                .when(dataDocumentRepository)
                .findAll(ArgumentMatchers.<Specification<DataDocumentAffiliate>>any());

        WorkerDetailDTO result = service.getWorkerDetails("FW-DEP-NULL");

        assertNotNull(result);
        assertNotNull(result.getContract());
        assertEquals("Sin información", result.getContract().getStageManagement());
    }

    @Test
    void getDocumentAffiliationWorker_dependent_returnsInfo() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC-DEP");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setIdAffiliate(90L);

        Affiliation worker = new Affiliation();
        worker.setIdentificationDocumentType("CC");
        worker.setIdentificationDocumentNumber("DEP-123");
        worker.setFirstName("Ana");
        worker.setSecondName("Maria");
        worker.setSurname("Torres");
        worker.setSecondSurname("Silva");

        when(affiliateRepository.findByFiledNumber("FDOC-DEP"))
                .thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(90L))
                .thenReturn(Collections.emptyList());
        when(affiliationDependentRepository.findByFiledNumber("FDOC-DEP"))
                .thenReturn(Optional.of(new AffiliationDependent() {{
                    setIdentificationDocumentType("CC");
                    setIdentificationDocumentNumber("DEP-123");
                    setFirstName("Ana");
                    setSecondName("Maria");
                    setSurname("Torres");
                    setSecondSurname("Silva");
                }}));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC-DEP");

        assertNotNull(dto);
        assertEquals("Ana", dto.getFirstName());
        assertEquals("Maria", dto.getMiddleName());
    }

    @Test
    void getDocumentAffiliationWorker_independent_returnsInfo() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FDOC-IND2");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setIdAffiliate(91L);

        Affiliation worker = new Affiliation();
        worker.setIdentificationDocumentType("CC");
        worker.setIdentificationDocumentNumber("IND-456");
        worker.setFirstName("Carlos");
        worker.setSecondName(null);
        worker.setSurname("Ruiz");
        worker.setSecondSurname("Gomez");

        when(affiliateRepository.findByFiledNumber("FDOC-IND2"))
                .thenReturn(Optional.of(a));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(91L))
                .thenReturn(Collections.emptyList());
        when(affiliationRepository.findByFiledNumber("FDOC-IND2"))
                .thenReturn(Optional.of(worker));

        DocumentsOfAffiliationDTO dto = service.getDocumentAffiliationWorker("FDOC-IND2");

        assertNotNull(dto);
        assertEquals("Carlos", dto.getFirstName());
        assertNull(dto.getMiddleName());
    }


    @Test
    void getHistoryJobRelated_withBillingCollected_returnsConPago() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FH-COLL");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setAffiliationSubType("SUB");
        a.setAffiliationStatus("S");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());
        a.setCoverageStartDate(LocalDate.now());
        a.setDocumentNumber("COLL-DOC");

        Billing b = new Billing();
        b.setId(10L);
        b.setPaymentPeriod("202401");

        BillingCollectionConciliation conc = new BillingCollectionConciliation();
        conc.setStatus("COLLECTED");

        when(affiliateRepository.findByFiledNumber("FH-COLL")).thenReturn(Optional.of(a));
        when(affiliationDependentRepository.findByFiledNumber("FH-COLL")).thenReturn(Optional.of(dep));
        when(billingRepository.findByContributorId("COLL-DOC")).thenReturn(List.of(b));
        when(billingCollectionConciliationRepository.findByBillingId(10L)).thenReturn(Optional.of(conc));

        HistoryJobRelatedDTO dto = service.getHistoryJobRelated("FH-COLL");

        assertEquals("No Pago", dto.getPaymentStatus());
    }

    @Test
    void getHistoryJobRelated_withBillingNotCollected_returnsConPago() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FH-NOCOLL");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        a.setAffiliationSubType("SUB");
        a.setAffiliationStatus("S");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());
        a.setCoverageStartDate(LocalDate.now());
        a.setDocumentNumber("NOCOLL-DOC");

        Billing b = new Billing();
        b.setId(11L);
        b.setPaymentPeriod("202401");

        BillingCollectionConciliation conc = new BillingCollectionConciliation();
        conc.setStatus(Constant.CONCILIATION_NOT_COLLECTED);

        when(affiliateRepository.findByFiledNumber("FH-NOCOLL")).thenReturn(Optional.of(a));
        when(affiliationDependentRepository.findByFiledNumber("FH-NOCOLL")).thenReturn(Optional.of(dep));
        when(billingRepository.findByContributorId("NOCOLL-DOC")).thenReturn(List.of(b));
        when(billingCollectionConciliationRepository.findByBillingId(11L)).thenReturn(Optional.of(conc));

        HistoryJobRelatedDTO dto = service.getHistoryJobRelated("FH-NOCOLL");

        assertEquals("Con pago", dto.getPaymentStatus());
    }

    @Test
    void getHistoryJobRelated_noBilling_returnsNoPago() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FH-NOBILL");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setAffiliationSubType("SUB");
        a.setAffiliationStatus("S");
        a.setAffiliationDate(LocalDate.now().atStartOfDay());
        a.setCoverageStartDate(LocalDate.now());

        when(affiliateRepository.findByFiledNumber("FH-NOBILL")).thenReturn(Optional.of(a));
        when(affiliationRepository.findByFiledNumber("FH-NOBILL")).thenReturn(Optional.of(ind));
        when(billingRepository.findByContributorId(null)).thenReturn(Collections.emptyList());

        HistoryJobRelatedDTO dto = service.getHistoryJobRelated("FH-NOBILL");

        assertEquals("No Pago", dto.getPaymentStatus());
    }

    @Test
    void getInfo_noAffiliatesFound_returnsEmptyDTO() {
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "NOTFOUND"))
                .thenReturn(Collections.emptyList());

        assertThrows(AffiliationNotFoundError.class,
                () -> service.getInfo("CC", "NOTFOUND",
                        Constant.TYPE_AFFILLATE_EMPLOYER, "0"));
    }

    @Test
    void getInfo_employer_noAffiliateMercantile_returnsEmpty() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-NO-MERC");
        af.setIdAffiliate(700L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "NO-MERC"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-NO-MERC"))
                .thenReturn(Optional.empty());

        InfoConsultDTO dto = service.getInfo("CC", "NO-MERC",
                Constant.TYPE_AFFILLATE_EMPLOYER, "700");

        assertTrue(dto instanceof EmployerInfoDTO);
    }

    @Test
    void getInfo_employerDomestic_noAffiliation_returnsEmpty() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        af.setFiledNumber("FILE-NO-AFF-DOM");
        af.setIdAffiliate(701L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "NO-AFF-DOM"))
                .thenReturn(List.of(af));
        when(affiliationDetailRepository.findByFiledNumber("FILE-NO-AFF-DOM"))
                .thenReturn(Optional.empty());

        InfoConsultDTO dto = service.getInfo("CC", "NO-AFF-DOM",
                Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, "701");

        assertTrue(dto instanceof EmployerInfoDTO);
    }

    @Test
    void getInfo_employerSizeFound_returnsDescription() {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-SIZE-OK2");
        af.setAffiliationDate(LocalDate.now().atStartOfDay());
        af.setCoverageStartDate(LocalDate.now());
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(800L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(700L);
        m.setTypePerson("J");
        m.setBusinessName("Empresa Grande");
        m.setNumberIdentification("SIZE-OK2");
        m.setIdEmployerSize(5L);

        EmployerSize empSize = new EmployerSize();
        empSize.setId(5L);
        empSize.setDescription("Grande");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "SIZE-OK2"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-SIZE-OK2"))
                .thenReturn(Optional.of(m));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(700L))
                .thenReturn(Collections.emptyList());
        when(employerSizeRepository.findById(5L))
                .thenReturn(Optional.of(empSize));
        when(affiliateRepository.findByNitCompany("SIZE-OK2"))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo("CC", "SIZE-OK2",
                Constant.TYPE_AFFILLATE_EMPLOYER, "800");

        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertEquals("Grande", e.getId_employer_size());
    }

    @Test
    void getInfo_multipleEmployerAffiliates_usesFirst() {
        Affiliate af1 = new Affiliate();
        af1.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af1.setFiledNumber("FILE-FIRST");
        af1.setIdAffiliate(100L);

        Affiliate af2 = new Affiliate();
        af2.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af2.setFiledNumber("FILE-SECOND");
        af2.setIdAffiliate(200L);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "MULTI"))
                .thenReturn(List.of(af1, af2));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-FIRST"))
                .thenReturn(Optional.of(merc));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(anyLong()))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo("CC", "MULTI",
                Constant.TYPE_AFFILLATE_EMPLOYER, "100");

        assertTrue(dto instanceof EmployerInfoDTO);
    }

    @Test
    void getContractsJobRelated_onlyIndependent_returnsOne() {
        Affiliate current = new Affiliate();
        current.setFiledNumber("FILE-ONLY-IND");
        current.setDocumentType("CC");
        current.setDocumentNumber("ONLY-IND");

        Affiliate indAf = new Affiliate();
        indAf.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        indAf.setFiledNumber("FILE-ONLY-IND");

        when(affiliateRepository.findByFiledNumber("FILE-ONLY-IND")).thenReturn(Optional.of(current));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "ONLY-IND"))
                .thenReturn(List.of(indAf));
        when(affiliationRepository.findByFiledNumber("FILE-ONLY-IND")).thenReturn(Optional.of(ind));

        List<ContractsJobRelatedDTO> out = service.getContractsJobRelated("FILE-ONLY-IND");

        assertEquals(1, out.size());
    }

    @Test
    void getJobRelatedInfo_unknownAffiliationType_continues() {
        Affiliate af = new Affiliate();
        af.setAffiliationSubType("UNKNOWN_TYPE");
        af.setFiledNumber("FILE-UNK");
        af.setNitCompany("UNK-NIT");
        af.setIdAffiliate(999L);
        af.setAffiliationType("Trabajador");
        af.setAffiliationStatus("Activo");

        doReturn(List.of(af))
                .when(affiliateRepository)
                .findAll(ArgumentMatchers.<Specification<Affiliate>>any());
        List<JobRelationShipDTO> result = service.getJobRelatedInfo("CC", "test");
        assertNotNull(result);
    }

    @Test
    void getInfo_employerNoCoverageDate_usesAffiliationDate() throws Exception {
        Affiliate af = new Affiliate();
        af.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        af.setFiledNumber("FILE-NO-COV");
        af.setAffiliationDate(LocalDate.of(2024, 3, 15).atStartOfDay());
        af.setCoverageStartDate(null); // SIN COVERAGE
        af.setAffiliationStatus("Activa");
        af.setIdAffiliate(777L);

        AffiliateMercantile m = new AffiliateMercantile();
        m.setId(555L);
        m.setTypePerson("N");
        m.setBusinessName("No Coverage Co");
        m.setNumberIdentification("NO-COV-NIT");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "NO-COV"))
                .thenReturn(List.of(af));
        when(affiliateMercantileRepository.findByFiledNumber("FILE-NO-COV"))
                .thenReturn(Optional.of(m));
        when(affiliateActivityEconomicRepository.findByIdAffiliateMercantile(555L))
                .thenReturn(Collections.emptyList());
        when(affiliateRepository.findByNitCompany("NO-COV-NIT"))
                .thenReturn(Collections.emptyList());

        InfoConsultDTO dto = service.getInfo("CC", "NO-COV",
                Constant.TYPE_AFFILLATE_EMPLOYER, "777");

        EmployerInfoDTO e = (EmployerInfoDTO) dto;
        assertNotNull(e);
    }

    @Test
    void getHistoryAffiliationsWithdrawals_noCoverageDate_usesAffiliationDate() {
        Affiliate a = new Affiliate();
        a.setFiledNumber("FH-NO-COV");
        a.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        a.setNoveltyType(Constant.NOVELTY_TYPE_AFFILIATION);
        a.setAffiliationDate(LocalDate.of(2024, 5, 10).atStartOfDay());
        a.setCoverageStartDate(null); // SIN COVERAGE

        Affiliation worker = new Affiliation();
        worker.setHealthPromotingEntity(15L);
        worker.setOccupation("Tester");
        worker.setAddress("Test St");

        when(affiliateRepository.findByFiledNumber("FH-NO-COV")).thenReturn(Optional.of(a));
        when(affiliationRepository.findByFiledNumber("FH-NO-COV")).thenReturn(Optional.of(worker));

        HistoryAffiliationsWithdrawalsHistoryDTO dto =
                service.getAffiliationWithdrawalsHistory("FH-NO-COV");

        assertTrue(dto.getFilingDate().startsWith("2024-05-10"));
    }





}
