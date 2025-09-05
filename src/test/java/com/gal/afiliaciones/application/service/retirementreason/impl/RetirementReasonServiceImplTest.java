package com.gal.afiliaciones.application.service.retirementreason.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.ExistsRetirementAffiliationException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerDao;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DataUpload;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RetirementEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;

@ExtendWith(MockitoExtension.class)
class RetirementReasonServiceImplTest {

    @Mock
    private RetirementReasonDao dao;
    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;
    @Mock
    private AffiliateRepository affiliationRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private CollectProperties properties;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationDetailRepository;
    @Mock
    private WorkCenterService workCenterService;
    @Mock
    private IEconomicActivityService economicActivityService;
    @Mock
    private FiledService filedService;
    @Mock
    private EmailService emailService;
    @Mock
    private RetirementReasonWorkerDao retirementReasonWorkerDao;
    @Mock
    private RetirementRepository retirementRepository;

    @InjectMocks
    private RetirementReasonServiceImpl retirementReasonService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void findAll() {
        List<RetirementReason> retirementReasons = Collections.singletonList(new RetirementReason());
        when(dao.getAllRetriementReason()).thenReturn(retirementReasons);

        List<RetirementReason> result = retirementReasonService.findAll();

        assertEquals(retirementReasons, result);
        verify(dao, times(1)).getAllRetriementReason();
    }

    @Test
    void getCompanyInfo_AffiliateNotFound() {
        String identificationType = "NI";
        String identification = "123456789";
        when(affiliationRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        assertThrows(AffiliateNotFound.class, () -> retirementReasonService.getCompanyInfo(identificationType, identification));
    }

    @Test
    void getCompanyInfo_Mercantile_ExistsRetirementAffiliationException() {
        String identificationType = "NI";
        String identification = "123456789";

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliate.setFiledNumber("123");
        affiliate.setNitCompany("12345");
        affiliate.setCompany("company");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setFiledNumber("123");

        when(affiliationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(affiliate));
        when(affiliateMercantileRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliateMercantile));
        when(retirementRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(new Retirement()));

        assertThrows(ExistsRetirementAffiliationException.class, () -> retirementReasonService.getCompanyInfo(identificationType, identification));
    }

    @Test
    void getCompanyInfo_Domestic_ExistsRetirementAffiliationException() {
        String identificationType = "CC";
        String identification = "123456789";

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliate.setFiledNumber("123");
        affiliate.setNitCompany("12345");
        affiliate.setCompany("company");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123");

        Affiliation affiliationDomestic = new Affiliation();
        affiliationDomestic.setFiledNumber("123");
        affiliationDomestic.setIdentificationDocumentType("CC");
        affiliationDomestic.setIdentificationDocumentNumber("123");
        affiliationDomestic.setFirstName("test");
        affiliationDomestic.setSecondSurname("test2");

        when(affiliationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(affiliate));
        when(affiliationDetailRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliationDomestic));
        when(retirementRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(new Retirement()));

        assertThrows(ExistsRetirementAffiliationException.class, () -> retirementReasonService.getCompanyInfo(identificationType, identification));
    }

    @Test
    void retirementEmployer_UserNotFoundInDataBase() throws Exception {
        RetirementEmployerDTO request = new RetirementEmployerDTO();
        request.setIdUser(1L);
        when(userPreRegisterRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> retirementReasonService.retirementEmployer(request));
    }

    @Test
    void findAllRetirementReasonWorker() {
        List<RetirementReasonWorker> retirementReasonWorkers = Arrays.asList(
                new RetirementReasonWorker(2L, "reason2"),
                new RetirementReasonWorker(1L, "reason1")
        );
        when(retirementReasonWorkerDao.findAllRetirementReasonWorker()).thenReturn(retirementReasonWorkers);

        List<RetirementReasonWorker> result = retirementReasonService.findAllRetirementReasonWorker();

        assertEquals(2, result.size());
        assertEquals("reason1", result.get(0).getReason());
        assertEquals("reason2", result.get(1).getReason());

        verify(retirementReasonWorkerDao, times(1)).findAllRetirementReasonWorker();
    }


    @Test
    void retirementEmployer_Domestic_Success() throws Exception {
        RetirementEmployerDTO request = new RetirementEmployerDTO();
        request.setIdUser(1L);
        request.setIdentificationType("CC");
        request.setIdentification("12345");
        request.setTypeOfAffiliate(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        request.setReasonId(1L);
        request.setBase64File("dGVzdA==");
        request.setFileName("test.pdf");

        UserMain user = new UserMain();
        user.setIdentification("user123");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(10L);

        Affiliation affiliationDetail = new Affiliation();
        affiliationDetail.setFirstName("John");
        affiliationDetail.setSurname("Doe");
        affiliationDetail.setSecondSurname("Smith");
        affiliationDetail.setEmail("test@test.com");
        affiliationDetail.setSecondName("");
        Entry entry1 = new Entry();
        entry1.setId("folderId");

        Entry entry2 = new Entry();
        entry2.setId("docId");

        when(userPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(properties.getRetirementFolder()).thenReturn("retirementFolderId");
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.empty());
        when(alfrescoService.createFolder(anyString(), anyString())).thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry1)).build());
        when(alfrescoService.uploadFileAlfresco(any(com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest.class)))
                .thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry2)).build());
        when(filedService.getNextFiledNumberRetirementReason()).thenReturn("R-001");
        when(affiliationRepository.findByNitCompany(anyString())).thenReturn(Collections.singletonList(affiliate));
        when(affiliationDetailRepository.findByIdentificationDocumentTypeAndIdentificationDocumentNumber(anyString(), anyString()))
                .thenReturn(Optional.of(affiliationDetail));

        String result = retirementReasonService.retirementEmployer(request);

        assertEquals("se ha radicado solicitud  R-001", result);
        verify(retirementRepository, times(1)).save(any(Retirement.class));
        verify(emailService, times(1)).sendSimpleMessage(any(), anyString());
    }

    @Test
    void retirementEmployer_Mercantile_Success() throws Exception {
        RetirementEmployerDTO request = new RetirementEmployerDTO();
        request.setIdUser(1L);
        request.setIdentificationType("NI");
        request.setIdentification("12345");
        request.setTypeOfAffiliate(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        request.setReasonId(1L);
        request.setBase64File("dGVzdA==");
        request.setFileName("test.pdf");

        UserMain user = new UserMain();
        user.setIdentification("user123");

        UserMain legalRep = new UserMain();
        legalRep.setFirstName("Legal");
        legalRep.setSurname("Rep");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(10L);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEmail("test@test.com");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("54321");

        Entry entry1 = new Entry();
        entry1.setId("folderId");

        Entry entry2 = new Entry();
        entry2.setId("docId");

        when(userPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(properties.getRetirementFolder()).thenReturn("retirementFolderId");
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.empty());
        when(alfrescoService.createFolder(anyString(), anyString())).thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry1)).build());
        when(alfrescoService.uploadFileAlfresco(any(com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest.class)))
                .thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry2)).build());
        when(filedService.getNextFiledNumberRetirementReason()).thenReturn("R-002");
        when(affiliationRepository.findByNitCompany(anyString())).thenReturn(Collections.singletonList(affiliate));
        when(affiliateMercantileRepository.findByTypeDocumentIdentificationAndNumberIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(legalRep));

        String result = retirementReasonService.retirementEmployer(request);

        assertEquals("se ha radicado solicitud  R-002", result);
        verify(retirementRepository, times(1)).save(any(Retirement.class));
        verify(emailService, times(1)).sendSimpleMessage(any(), anyString());
    }

    @Test
    void retirementEmployer_Mercantile_LegalRepNotFound() throws Exception {
        RetirementEmployerDTO request = new RetirementEmployerDTO();
        request.setIdUser(1L);
        request.setIdentificationType("NI");
        request.setIdentification("12345");
        request.setTypeOfAffiliate(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        request.setBase64File("dGVzdA==");
        request.setFileName("test.pdf");

        UserMain user = new UserMain();
        user.setIdentification("user123");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(10L);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("54321");

        Entry entry1 = new Entry();
        entry1.setId("folderId");

        Entry entry2 = new Entry();
        entry2.setId("docId");

        when(userPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(properties.getRetirementFolder()).thenReturn("retirementFolderId");
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.empty());
        when(alfrescoService.createFolder(anyString(), anyString())).thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry1)).build());
        when(alfrescoService.uploadFileAlfresco(any(com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest.class)))
                .thenReturn(AlfrescoUploadResponse.builder().data(new DataUpload(entry2)).build());
        when(filedService.getNextFiledNumberRetirementReason()).thenReturn("R-003");
        when(affiliationRepository.findByNitCompany(anyString())).thenReturn(Collections.singletonList(affiliate));
        when(affiliateMercantileRepository.findByTypeDocumentIdentificationAndNumberIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> retirementReasonService.retirementEmployer(request));
    }

    @Test
    void findEconomicActivitiesDomestic_Success() {
        // Arrange
        Affiliation affiliation = new Affiliation();
        AffiliateActivityEconomic activity1 = new AffiliateActivityEconomic();
        activity1.setIdWorkCenter(1L);
        AffiliateActivityEconomic activity2 = new AffiliateActivityEconomic();
        activity2.setIdWorkCenter(2L);
        affiliation.setEconomicActivity(Arrays.asList(activity1, activity2));

        WorkCenter workCenter1 = new WorkCenter();
        workCenter1.setEconomicActivityCode("101");
        WorkCenter workCenter2 = new WorkCenter();
        workCenter2.setEconomicActivityCode("102");

        EconomicActivityDTO economicActivityDTO1 = new EconomicActivityDTO();
        economicActivityDTO1.setDescription("Activity 1");
        EconomicActivityDTO economicActivityDTO2 = new EconomicActivityDTO();
        economicActivityDTO2.setDescription("Activity 2");

        when(workCenterService.getWorkCenterById(1L)).thenReturn(workCenter1);
        when(workCenterService.getWorkCenterById(2L)).thenReturn(workCenter2);
        when(economicActivityService.getEconomicActivityByCode("101")).thenReturn(economicActivityDTO1);
        when(economicActivityService.getEconomicActivityByCode("102")).thenReturn(economicActivityDTO2);

        // Act
        List<com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO> result = retirementReasonService.findEconomicActivitiesDomestic(affiliation);

        // Assert
        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getEconomicActivityCode());
        assertEquals("Activity 1", result.get(0).getDescription());
        assertEquals(Boolean.TRUE, result.get(0).getTypeActivity());
        assertEquals("102", result.get(1).getEconomicActivityCode());
        assertEquals("Activity 2", result.get(1).getDescription());
        assertEquals(Boolean.TRUE, result.get(1).getTypeActivity());

        verify(workCenterService, times(2)).getWorkCenterById(anyLong());
        verify(economicActivityService, times(2)).getEconomicActivityByCode(anyString());
    }

    @Test
    void findEconomicActivitiesDomestic_WithNullWorkCenterId() {
        // Arrange
        Affiliation affiliation = new Affiliation();
        AffiliateActivityEconomic activity1 = new AffiliateActivityEconomic();
        activity1.setIdWorkCenter(1L);
        AffiliateActivityEconomic activity2WithNullId = new AffiliateActivityEconomic();
        activity2WithNullId.setIdWorkCenter(null);
        affiliation.setEconomicActivity(Arrays.asList(activity1, activity2WithNullId));

        WorkCenter workCenter1 = new WorkCenter();
        workCenter1.setEconomicActivityCode("101");

        EconomicActivityDTO economicActivityDTO1 = new EconomicActivityDTO();
        economicActivityDTO1.setDescription("Activity 1");

        when(workCenterService.getWorkCenterById(1L)).thenReturn(workCenter1);
        when(economicActivityService.getEconomicActivityByCode("101")).thenReturn(economicActivityDTO1);

        // Act
        List<com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO> result = retirementReasonService.findEconomicActivitiesDomestic(affiliation);

        // Assert
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).getEconomicActivityCode());
        assertEquals("Activity 1", result.get(0).getDescription());
        assertEquals(Boolean.TRUE, result.get(0).getTypeActivity());

        verify(workCenterService, times(1)).getWorkCenterById(1L);
        verify(economicActivityService, times(1)).getEconomicActivityByCode("101");
    }

    @Test
    void findEconomicActivitiesDomestic_EmptyList() {
        // Arrange
        Affiliation affiliation = new Affiliation();
        affiliation.setEconomicActivity(Collections.emptyList());

        // Act
        List<com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO> result = retirementReasonService.findEconomicActivitiesDomestic(affiliation);

        // Assert
        assertEquals(0, result.size());
        verify(workCenterService, times(0)).getWorkCenterById(anyLong());
        verify(economicActivityService, times(0)).getEconomicActivityByCode(anyString());
    }

    @Test
    void concatName_AllNamesPresent() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("Juan");
        affiliation.setSecondName("Carlos");
        affiliation.setSurname("Perez");
        affiliation.setSecondSurname("Gomez");

        java.lang.reflect.Method method = RetirementReasonServiceImpl.class.getDeclaredMethod("concatName", Affiliation.class);
        method.setAccessible(true);
        String result = (String) method.invoke(retirementReasonService, affiliation);

        assertEquals("Juan Carlos Perez Gomez", result);
    }

    @Test
    void concatName_NoSecondName() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("Juan");
        affiliation.setSecondName("");
        affiliation.setSurname("Perez");
        affiliation.setSecondSurname("Gomez");

        java.lang.reflect.Method method = RetirementReasonServiceImpl.class.getDeclaredMethod("concatName", Affiliation.class);
        method.setAccessible(true);
        String result = (String) method.invoke(retirementReasonService, affiliation);

        assertEquals("Juan Perez Gomez", result);
    }

    @Test
    void concatName_NoSecondSurname() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("Juan");
        affiliation.setSecondName("Carlos");
        affiliation.setSurname("Perez");
        affiliation.setSecondSurname("");

        java.lang.reflect.Method method = RetirementReasonServiceImpl.class.getDeclaredMethod("concatName", Affiliation.class);
        method.setAccessible(true);
        String result = (String) method.invoke(retirementReasonService, affiliation);

        assertEquals("Juan Carlos Perez", result);
    }

    @Test
    void concatName_OnlyFirstAndSurname() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("Juan");
        affiliation.setSecondName("");
        affiliation.setSurname("Perez");
        affiliation.setSecondSurname("");

        java.lang.reflect.Method method = RetirementReasonServiceImpl.class.getDeclaredMethod("concatName", Affiliation.class);
        method.setAccessible(true);
        String result = (String) method.invoke(retirementReasonService, affiliation);

        assertEquals("Juan Perez", result);
    }
}

    