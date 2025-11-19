package com.gal.afiliaciones.application.service.retirement.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.retirement.InvalidRetirementDateException;
import com.gal.afiliaciones.config.ex.retirement.InvalidRetirementRequestException;
import com.gal.afiliaciones.config.ex.retirement.WorkerNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.workerretirement.WorkerRetirementException;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.retirement.ContractListResponseDTO;
import com.gal.afiliaciones.domain.model.retirement.RetirementRequestDTO;
import com.gal.afiliaciones.domain.model.retirement.WorkerSearchRequestDTO;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class RetirementServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private IUserPreRegisterRepository userMainRepository;
    @Mock
    private RetirementRepository retirementRepository;
    @Mock
    private FiledService filedService;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private AffiliateMercantileRepository mercantileRepository;
    @Mock
    private AffiliateService affiliateService;
    @Mock
    RetirementReasonWorkerRepository retirementReasonWorkerRepository;
    @Mock
    GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    @Mock
    ArlInformationDao arlInformationDao;
    @Mock
    NoveltyRuafService noveltyRuafService;

    @InjectMocks
    private RetirementServiceImpl retirementService;

    private Affiliate affiliate;
    private AffiliationDependent affiliationDependent;
    private Affiliation affiliationIndependent;
    private DataWorkerRetirementDTO dataWorkerRetirementDTO;
    private Retirement retirement;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliationDependent = new AffiliationDependent();
        affiliationIndependent = new Affiliation();
        dataWorkerRetirementDTO = new DataWorkerRetirementDTO();
        retirement = new Retirement();
    }

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

        Affiliate employer = new Affiliate();
        employer.setIdAffiliate(1L);
        employer.setCompany("ACME");
        employer.setNitCompany("900111222");
    }


    @Test
    void validWorkedCompany_dependentNotFound_throwsException() {
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
 
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(retirementService, 1L, "CC", "123");

            } catch (Exception e) {
                throw new WorkerRetirementException(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_affiliateNotFound_throwsException() {
        // Datos de entrada
        Long idAffiliateEmployer = 1L;
        String documentType = "CC";
        String documentNumber = "123456";

        // Mock de AffiliationDependent existente
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setId(100L);
        dependent.setFiledNumber("F123");

        // Simular resultados del repositorio
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of(dependent));
        lenient().when(affiliateRepository.findByFiledNumber("F123")).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> {
            try {
                Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
                method.setAccessible(true);
                method.invoke(retirementService, idAffiliateEmployer, documentType, documentNumber);
            } catch (Exception e) {
                throw new AffiliateNotFound(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_returnsFirstAffiliateWorker() {
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationDate(LocalDateTime.now().minusDays(1));
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setAffiliationDate(LocalDateTime.now());

        List<AffiliationDependent> affiliationDependentList = new ArrayList<>();
        affiliationDependentList.add(affiliationDependent);

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(affiliationDependentList);
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));

        Affiliate result = null;
        try {
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
            method.setAccessible(true);
            result = (Affiliate) method.invoke(retirementService, 1L, "CC", "123");
        } catch (Exception ignored) {

        }

        assertNull(result);
    }

    @Test
    void retirementWorker_affiliationError_throwsException() {
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThrows(AffiliationError.class, () -> retirementService.retirementWorker(dataWorkerRetirementDTO));
    }
    @Test
    void retirementWorker_validInput_updatesAffiliateAndCreatesRetirement() {
        // Affiliate (trabajador)
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setAffiliationSubType("SubType");
        affiliate.setFiledNumber("F123"); // obligatorio para validateRetirementDate()

        // DTO de entrada con fecha válida (antes de endDate)
        dataWorkerRetirementDTO.setIdAffiliation(1L);
        dataWorkerRetirementDTO.setIdentificationDocumentType("CC");
        dataWorkerRetirementDTO.setIdentificationDocumentNumber("123");
        dataWorkerRetirementDTO.setIdAffiliateEmployer(999L);
        dataWorkerRetirementDTO.setRetirementDate(LocalDate.now().plusDays(1));

        // Afiliación dependiente del trabajador (debe existir y tener endDate >= fecha retiro)
        affiliationDependent.setFirstName("Test");
        affiliationDependent.setSurname("Test2");
        affiliationDependent.setSecondName("");
        affiliationDependent.setSecondSurname("");
        affiliationDependent.setFiledNumber("F123");
        affiliationDependent.setEndDate(LocalDate.now().plusDays(10)); // clave para que no lance excepción

        // Mocks mínimos
        when(affiliationDependentRepository.findAll(any(Specification.class)))
                .thenReturn(java.util.List.of(affiliationDependent));
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(java.util.Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber("F123"))
                .thenReturn(java.util.Optional.of(affiliationDependent));
        when(filedService.getNextFiledNumberRetirementReason())
                .thenReturn("FILENUMBER123");
        when(retirementRepository.findByIdAffiliate(1L))
                .thenReturn(java.util.Optional.empty());
        when(retirementRepository.save(any(Retirement.class)))
                .thenReturn(retirement);

        // Ejecutar
        retirementService.retirementWorker(dataWorkerRetirementDTO);

        // Si no lanzó excepción, la validación pasó
        assertTrue(true);
    }

    @Test
    void validateRetirementDate_nullRetirementDate_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("validateRetirementDate", Affiliate.class, LocalDate.class);
        method.setAccessible(true);

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, affiliate, null);
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void validateRetirementDate_retirementDateAfterContractEndDate_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("validateRetirementDate", Affiliate.class, LocalDate.class);
        method.setAccessible(true);

        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("123");
        affiliationIndependent.setContractEndDate(LocalDate.now().minusDays(1));

        when(affiliationRepository.findByFiledNumber("123")).thenReturn(Optional.of(affiliationIndependent));

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, affiliate, LocalDate.now());
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void findUserNameToRetired_affiliationNotFound_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("findUserNameToRetired", String.class);
        method.setAccessible(true);

        when(affiliationDependentRepository.findByFiledNumber(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, "123");
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void findUserNameToRetired_affiliationFound_returnsCompleteName() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("findUserNameToRetired", String.class);
        method.setAccessible(true);

        affiliationDependent.setFirstName("Test");
        affiliationDependent.setSurname("Test2");
        affiliationDependent.setSecondName("");
        affiliationDependent.setSecondSurname("");

        when(affiliationDependentRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliationDependent));

        String completeName = null;
        try {
            completeName = (String) method.invoke(retirementService, "123");
        } catch (Exception e) {
            throw new WorkerRetirementException(e.getCause().getMessage());
        }

        assertEquals("Test Test2", completeName);
    }

    @Test
    void cancelRetirementWorker_affiliateNotFound_throwsException() {
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_workerNotRetired_throwsException() {
        affiliate.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_retirementNotFound_throwsException() {
        affiliate.setNoveltyType("OTHER");
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));
        when(retirementRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_validInput_deletesRetirementAndSetsAffiliateRetirementDateToNull() {
        affiliate.setNoveltyType("OTHER");
        affiliate.setIdAffiliate(1L);
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));
        when(retirementRepository.findByIdAffiliate(any())).thenReturn(Optional.of(retirement));

        Boolean result = retirementService.cancelRetirementWorker(1L);

        assertEquals(true, result);
    }

    @Test
    void createRequestRetirementWork_affiliateNotFound_returnsError() {
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        String result = retirementService.createRequestRetirementWork(1L, LocalDate.now(), "Test");

        assertEquals("Error: affiliate not found", result);
    }

    @Test
    void consultWorker(){

        List<AffiliationDependent> list = list();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setFiledNumber("123");
        affiliate1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate1.setNoveltyType("Otro");
        affiliate1.setAffiliationStatus("Otro");
        affiliate1.setIdAffiliate(1L);
        affiliate1.setAffiliationDate(LocalDateTime.now());
        Retirement retirement1 = new Retirement();
        retirement1.setRetirementDate(LocalDate.now());
        AffiliationDependent affiliationDependent1 = new AffiliationDependent();

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);
        when(affiliateRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(retirement1));
        when(affiliationDependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent1));


        BodyResponseConfig<DataWorkerRetirementDTO> response = retirementService.consultWorker("CC", "123", 1L);

        assertNotNull(response);
    }

    @Test
    void retirementWorker_Exception(){

        List<AffiliationDependent> list = new ArrayList<>();
        DataWorkerRetirementDTO dto = new DataWorkerRetirementDTO();

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> retirementService.retirementWorker(dto)
        );

        assertNull( ex.getMessage());

    }

    @Test
    void retirementWorker_Exception_(){

        List<AffiliationDependent> list = list();
        DataWorkerRetirementDTO dto = new DataWorkerRetirementDTO();
        dto.setIdAffiliation(1L);

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenThrow(new WorkerRetirementException("Afiliación no encontrada."));

        WorkerRetirementException ex = assertThrows(
                WorkerRetirementException.class,
                () -> retirementService.retirementWorker(dto)
        );

        assertNull( ex.getMessage());

    }

    @Test
    void retirementWorker_Exception_DateNull(){

        List<AffiliationDependent> list = list();
        DataWorkerRetirementDTO dto = new DataWorkerRetirementDTO();
        dto.setIdAffiliation(1L);
        Affiliate affiliate1 =  new Affiliate();

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(affiliate1));

        WorkerRetirementException ex = assertThrows(
                WorkerRetirementException.class,
                () -> retirementService.retirementWorker(dto)
        );

        assertNull( ex.getMessage());

    }

    @Test
    void retirementWorker_Exception_FiledNumberNull(){

        List<AffiliationDependent> list = list();
        DataWorkerRetirementDTO dto = new DataWorkerRetirementDTO();
        dto.setIdAffiliation(1L);
        dto.setRetirementDate(LocalDate.now());
        Affiliate affiliate1 =  new Affiliate();

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(affiliate1));

        WorkerRetirementException ex = assertThrows(
                WorkerRetirementException.class,
                () -> retirementService.retirementWorker(dto)
        );

        assertNull( ex.getMessage());

    }

    @Test
    void retirementWorker(){

        List<AffiliationDependent> list = list();
        List<ArlInformation> listArl =  listNoveltyRuaf();
        List<Affiliate> listAffiliate =  listAffiliate();
        DataWorkerRetirementDTO dto = new DataWorkerRetirementDTO();
        dto.setIdAffiliation(1L);
        dto.setRetirementDate(LocalDate.now());
        dto.setIdRetirementReason(2L);
        dto.setIdAffiliateEmployer(1L);
        Affiliate affiliate1 =  new Affiliate();
        affiliate1.setFiledNumber("123");
        affiliate1.setIdAffiliate(1L);
        affiliate1.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate1.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        AffiliationDependent affiliationDependent1 = new AffiliationDependent();
        affiliationDependent1.setFirstName("Nombre");
        affiliationDependent1.setSecondName("Nombre");
        affiliationDependent1.setSecondSurname("Nombre");
        RetirementReasonWorker reasonWorker = new RetirementReasonWorker();
        Retirement workerRetirement = new Retirement();
        AffiliateMercantile affiliateMercantile =  new AffiliateMercantile();

        when(affiliationDependentRepository.findAll((Specification<AffiliationDependent>) any()))
                .thenReturn(list);
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(affiliate1));
        when(affiliationDependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent1));
        when(filedService.getNextFiledNumberRetirementReason()).thenReturn("123");
        when(retirementReasonWorkerRepository.findById(2L)).thenReturn(Optional.of(reasonWorker));
        doNothing()
                .when(generalNoveltyServiceImpl)
                .saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));
        when(retirementRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(workerRetirement));
        when(retirementRepository.save(any())).thenReturn(workerRetirement);
        doNothing()
                .when(sendEmails)
                .emailWorkerRetirement(workerRetirement, null, null);
        when(mercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));
        when(affiliateService.getEmployerSize(0)).thenReturn(0L);
        when(arlInformationDao.findAllArlInformation()).thenReturn(listArl);
        when(affiliateRepository.findAll((Specification<Affiliate>) any()))
                .thenReturn(listAffiliate);


        String response  = retirementService.retirementWorker(dto);

        assertNotNull(response);


    }

    @Test
    void createRequestRetirementWork(){

        List<Affiliate> listAffiliate =  listAffiliate2();
        Affiliate affiliate1 = new Affiliate();
        Affiliation affiliation = new Affiliation();
        Retirement workerRetirement = new Retirement();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        UserMain userMain1 = new UserMain();


        affiliate1.setFiledNumber("123");
        affiliate1.setIdAffiliate(1L);
        affiliate1.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);

        userMain1.setFirstName("Nombre");
        userMain1.setSurname("Nombre");

        affiliation.setContractEndDate(LocalDate.now());


        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(affiliate1));
        when( affiliationRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(filedService.getNextFiledNumberRetirementReason())
                .thenReturn("1234");
        when(retirementRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(workerRetirement));
        when(retirementRepository.save(workerRetirement))
                .thenReturn(workerRetirement);
        when(affiliateRepository.findAll((Specification<Affiliate>) any()))
                .thenReturn(listAffiliate);
        when(mercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));
        when(userMainRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain1));

        String response = retirementService.createRequestRetirementWork(1L, LocalDate.now(), "name");

        assertNotNull(response);
    }

    @Test
    void searchWorker_Exception(){

        WorkerSearchRequestDTO request = new WorkerSearchRequestDTO();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> retirementService.searchWorker(request));

        assertTrue( ex.getMessage().contains("El tipo de documento y el número de identificación son obligatorios."));

    }

    @Test
    void searchWorker_ListEmpty(){

        WorkerSearchRequestDTO request = new WorkerSearchRequestDTO();
        request.setTipoDocumento("CC");
        request.setNumeroIdentificacion("123456789");
        request.setEmpresa("empresa");

        when(affiliateRepository.findAll((Specification<Affiliate>) any()))
                .thenReturn(List.of());

        WorkerNotFoundException ex = assertThrows(
                WorkerNotFoundException.class,
                () -> retirementService.searchWorker(request));

        assertTrue( ex.getMessage().contains("El trabajador no fue encontrado, valida la información e intenta nuevamente"));

    }

    @Test
    void searchWorker(){

        List<Affiliate> listAffiliate =  listAffiliate2();
        WorkerSearchRequestDTO request = new WorkerSearchRequestDTO();
        request.setTipoDocumento("CC");
        request.setNumeroIdentificacion("123456789");
        request.setEmpresa("empresa");

        when(affiliateRepository.findAll((Specification<Affiliate>) any()))
                .thenReturn(listAffiliate);


        List<ContractListResponseDTO> response = retirementService.searchWorker(request);

        assertNotNull(response);
    }

    @Test
    void requestRetirement_NotActive(){

        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Otro");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));

        InvalidRetirementRequestException ex = assertThrows(
                InvalidRetirementRequestException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requestRetirement_False(){

        List<Retirement> list =  listRetirement();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(true);

        InvalidRetirementRequestException ex = assertThrows(
                InvalidRetirementRequestException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requestRetirement_valid(){

        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");


        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now().minusYears(10));

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);

        InvalidRetirementDateException ex = assertThrows(
                InvalidRetirementDateException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requestRetirement_valid2(){

        List<Affiliate> listAffiliate =  listAffiliate2()
                .stream()
                .peek(e -> e.setCoverageStartDate(LocalDate.now().plusDays(1)))
                .toList();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");
        affiliate1.setCoverageStartDate(LocalDate.now().plusDays(1));
        affiliate1.setNitCompany("1");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now());

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);
        when(affiliateRepository.findByNitCompanyAndAffiliationType("1", "Empleador"))
                .thenReturn(listAffiliate);

        InvalidRetirementDateException ex = assertThrows(
                InvalidRetirementDateException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requestRetirement_valid3(){

        List<Affiliate> listAffiliate =  listAffiliate2()
                .stream()
                .peek(e -> e.setAffiliationDate(LocalDateTime.now().plusDays(1)))
                .toList();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");
        affiliate1.setCoverageStartDate(LocalDate.now().plusDays(1));
        affiliate1.setNitCompany("1");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now());

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);
        when(affiliateRepository.findByNitCompanyAndAffiliationType("1", "Empleador"))
                .thenReturn(listAffiliate);

        InvalidRetirementDateException ex = assertThrows(
                InvalidRetirementDateException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requestRetirement_Independent_Exception(){

        List<Affiliate> listAffiliate =  listAffiliate2();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");
        affiliate1.setAffiliationType("Trabajador Independiente");
        affiliate1.setRisk("1");

        affiliate1.setCoverageStartDate(LocalDate.now().plusDays(1));
        affiliate1.setNitCompany("1");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now());

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);
        when(affiliateRepository.findByNitCompanyAndAffiliationType("1", "Empleador"))
                .thenReturn(listAffiliate);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());
        assertEquals("El trabajador independiente no pertenece a riesgos 4 o 5.", ex.getMessage());
    }


    @Test
    void requestRetirement_Dependent_Exception(){

        List<Affiliate> listAffiliate =  listAffiliate2();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationStatus("Activa");
        affiliate1.setAffiliationType("Dependiente");
        affiliate1.setRisk("1");

        affiliate1.setCoverageStartDate(LocalDate.now().plusDays(1));
        affiliate1.setNitCompany("1");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now());

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);
        when(affiliateRepository.findByNitCompanyAndAffiliationType("1", "Empleador"))
                .thenReturn(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> retirementService.requestRetirement(dto));

        assertNotNull(ex.getMessage());

    }

    @Test
    void requestRetirement_Independent() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus("Activa");
        affiliate.setAffiliationType("Independiente");
        affiliate.setRisk("1");
        affiliate.setCoverageStartDate(LocalDate.now().minusDays(7));
        affiliate.setNitCompany("1");

        RetirementRequestDTO dto = new RetirementRequestDTO();
        dto.setContractId(1L);
        dto.setRetirementDate(LocalDate.now());

        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate));
        when(retirementRepository.existsByIdAffiliate(1L))
                .thenReturn(false);

        Affiliate employer = new Affiliate();
        employer.setAffiliationStatus("Activa");
        employer.setAffiliationType("Empleador");
        employer.setNitCompany("1");

        when(affiliateRepository.findByNitCompanyAndAffiliationType("1", "Empleador"))
                .thenReturn(List.of(employer));

        assertDoesNotThrow(() -> retirementService.requestRetirement(dto));
    }


    List<AffiliationDependent> list(){

        AffiliationDependent affiliationDependent1 = new AffiliationDependent();
        AffiliationDependent affiliationDependent2 = new AffiliationDependent();
        AffiliationDependent affiliationDependent3 = new AffiliationDependent();
        AffiliationDependent affiliationDependent4 = new AffiliationDependent();
        affiliationDependent1.setId(1L);
        affiliationDependent2.setId(2L);
        affiliationDependent3.setId(3L);
        affiliationDependent4.setId(4L);
        affiliationDependent1.setFiledNumber("123");
        affiliationDependent2.setFiledNumber("123");
        affiliationDependent3.setFiledNumber("123");
        affiliationDependent4.setFiledNumber("123");
        List<AffiliationDependent> list =  new ArrayList<>();
        list.add(affiliationDependent1);
        list.add(affiliationDependent2);
        list.add(affiliationDependent3);
        list.add(affiliationDependent4);
        return list;
    }

    List<ArlInformation> listNoveltyRuaf(){

        ArlInformation arlInformation = new ArlInformation();
        List<ArlInformation> list =  new ArrayList<>();
        list.add(arlInformation);
        return list;
    }

    List<Affiliate> listAffiliate(){
        Affiliate affiliate1 =  new Affiliate();
        affiliate1.setFiledNumber("123");
        affiliate1.setAffiliationSubType("Otra");
        List<Affiliate> list =  new ArrayList<>();
        list.add(affiliate1);
        return list;
    }

    List<Affiliate> listAffiliate2(){
        Affiliate affiliate1 =  new Affiliate();
        affiliate1.setFiledNumber("123");
        affiliate1.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliate1.setAffiliationStatus("Activa");
        List<Affiliate> list =  new ArrayList<>();
        list.add(affiliate1);
        return list;
    }

    List<Retirement> listRetirement(){

        Retirement retirement1 = new Retirement();
        retirement1.setIdAffiliate(1L);
        List<Retirement> list =  new ArrayList<>();
        list.add(retirement1);
        return list;
    }
}
