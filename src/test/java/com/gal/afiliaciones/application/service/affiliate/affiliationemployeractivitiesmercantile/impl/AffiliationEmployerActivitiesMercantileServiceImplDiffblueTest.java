package com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.documentnamestandardization.impl.DocumentNameStandardizationServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorDocumentConditions;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataContactCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DataUpload;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;

import reactor.core.publisher.Flux;

@ContextConfiguration(classes = {AffiliationEmployerActivitiesMercantileServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class AffiliationEmployerActivitiesMercantileServiceImplDiffblueTest {
    @MockBean
    private ArlInformationDao arlInformationDao;
    @MockBean
    private ArlRepository arlRepository;
    @MockBean
    private ConsultEmployerClient consultEmployerClient;
    @MockBean private PolicyService policyService;
    @MockBean
    private IAffiliationCancellationTimerRepository timerRepository;

    @MockBean
    private RolesUserService rolesUserService;

    @MockBean
    private ScheduleInterviewWebService scheduleInterviewWebService;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository iAffiliateRepository;

    @Autowired
    private AffiliationEmployerActivitiesMercantileServiceImpl affiliationEmployerActivitiesMercantileServiceImpl;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private CollectProperties properties;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private IDataDocumentRepository dataDocumentRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private IUserRegisterService iUserRegisterService;

    @MockBean
    private WebClient webClient;

    @MockBean
    private FiledService filedService;

    @MockBean
    private MainOfficeService mainOfficeService;

    @MockBean
    private MainOfficeRepository mainOfficeRepository;

    @MockBean
    private WorkCenterRepository workCenterRepository;

    @MockBean
    private IEconomicActivityRepository economicActivityRepository;

    @MockBean
    private TypeEmployerDocumentService typeEmployerDocumentService;

    @MockBean
    private ObservationsAffiliationService observationsAffiliationService;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private MunicipalityRepository municipalityRepository;

    @MockBean
    private IdentificationLegalNatureService identificationLegalNatureService;

    @MockBean
    private MessageErrorAge messageError;

    @Mock
    private AlfrescoUploadResponse alfrescoUploadResponse;

    @Mock
    private DataUpload alfrescoDataUpload;

    @Mock
    private Entry alfrescoEntity;

    @MockBean
    private DocumentNameStandardizationServiceImpl documentNameStandardizationService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private AffiliateService affiliateService;

    @MockBean
    private SatConsultTransferableEmployerClient satConsultTransferableEmployerClient;

    @MockBean
    private RegistraduriaUnifiedService registraduriaUnifiedService;

    @BeforeEach
    void setUp() {
        when(alfrescoUploadResponse.getData()).thenReturn(alfrescoDataUpload);
        when(alfrescoDataUpload.getEntry()).thenReturn(alfrescoEntity);
        when(alfrescoEntity.getId()).thenReturn("001");
        when(satConsultTransferableEmployerClient.consult(Mockito.any()))
                .thenReturn(TransferableEmployerResponse.builder().causal(3).build());
        when(registraduriaUnifiedService.searchUserInNationalRegistry("12345"))
                .thenReturn(List.of(new com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    void testValidationsStepOne2() {
        // Arrange
        when(iUserRegisterService.calculateModulo11DV(Mockito.<String>any())).thenReturn(1);

        // Act and Assert
        assertThrows(ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("42", "NI", "Dv"));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    void testValidationsStepOne3() {
        // Arrange
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Flux<RecordResponseDTO> fromIterableResult = Flux.fromIterable(new ArrayList<>());
        when(responseSpec.bodyToFlux(Mockito.<Class<RecordResponseDTO>>any())).thenReturn(fromIterableResult);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec = mock(
                WebClient.RequestHeadersUriSpec.class);
        when(requestHeadersUriSpec.uri(Mockito.<String>any(), isA(Object[].class))).thenReturn(requestBodySpec);
        Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(properties.getUrlTransversal()).thenReturn("https://example.org/example");
        when(iUserRegisterService.isEmployerPersonJuridica(anyLong())).thenReturn(true);
        when(iUserRegisterService.calculateModulo11DV(Mockito.<String>any())).thenReturn(1);

        // Act and Assert
        assertThrows(ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("42", "NI", "1"));
        
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    void testValidationsStepOne4() {
        // Arrange
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToFlux(Mockito.<Class<RecordResponseDTO>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec = mock(
                WebClient.RequestHeadersUriSpec.class);
        when(requestHeadersUriSpec.uri(Mockito.<String>any(), isA(Object[].class))).thenReturn(requestBodySpec);
        Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(properties.getUrlTransversal()).thenReturn("https://example.org/example");
        when(iUserRegisterService.isEmployerPersonJuridica(anyLong())).thenReturn(true);
        when(iUserRegisterService.calculateModulo11DV(Mockito.<String>any())).thenReturn(1);

        // Act and Assert
        assertThrows(ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("42", "NI", "1"));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    void testValidationsStepOne5() {
        // Arrange
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToFlux(Mockito.<Class<RecordResponseDTO>>any()))
                .thenThrow(new ErrorFindDocumentsAlfresco("Not all who wander are lost"));
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec = mock(
                WebClient.RequestHeadersUriSpec.class);
        when(requestHeadersUriSpec.uri(Mockito.<String>any(), isA(Object[].class))).thenReturn(requestBodySpec);
        Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(properties.getUrlTransversal()).thenReturn("https://example.org/example");
        when(iUserRegisterService.isEmployerPersonJuridica(anyLong())).thenReturn(true);
        when(iUserRegisterService.calculateModulo11DV(Mockito.<String>any())).thenReturn(1);

        // Act and Assert
        assertThrows(ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("42", "NI", "1"));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    void testValidationsStepOne6() {
        // Arrange
        ArrayList<RecordResponseDTO> it = new ArrayList<>();
        it.add(new RecordResponseDTO());
        Flux<RecordResponseDTO> fromIterableResult = Flux.fromIterable(it);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToFlux(Mockito.<Class<RecordResponseDTO>>any())).thenReturn(fromIterableResult);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        WebClient.RequestHeadersUriSpec<WebClient.RequestBodySpec> requestHeadersUriSpec = mock(
                WebClient.RequestHeadersUriSpec.class);
        when(requestHeadersUriSpec.uri(Mockito.<String>any(), isA(Object[].class))).thenReturn(requestBodySpec);
        Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(properties.getUrlTransversal()).thenReturn("https://example.org/example");
        when(iUserRegisterService.isEmployerPersonJuridica(anyLong())).thenReturn(true);
        when(iUserRegisterService.calculateModulo11DV(Mockito.<String>any())).thenReturn(1);

        // Act and Assert
        assertThrows(ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("42", "NI", "1"));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    void testStepOne() {
        // Arrange
        DataBasicCompanyDTO validDto = new DataBasicCompanyDTO();
        DataBasicCompanyDTO nullDto = null;

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class, () -> {
            // Single invocation throwing runtime exception
            affiliationEmployerActivitiesMercantileServiceImpl.stepOne(validDto);
        });

        assertThrows(NullPointerException.class, () -> {
            // Single invocation throwing runtime exception
            affiliationEmployerActivitiesMercantileServiceImpl.stepOne(nullDto);
        });
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    void testStepOne2() {
    // Arrange
    AddressDTO addressDTO = AddressDTO.builder()
        .idCardinalPoint2(1L)
        .idCardinalPointMainStreet(1L)
        .idCity(1L)
        .idDepartment(1L)
        .idHorizontalProperty1(1L)
        .idHorizontalProperty2(1L)
        .idHorizontalProperty3(1L)
        .idHorizontalProperty4(1L)
        .idLetter1MainStreet(1L)
        .idLetter2MainStreet(1L)
        .idLetterSecondStreet(1L)
        .idMainStreet(1L)
        .idNum1SecondStreet(1L)
        .idNum2SecondStreet(1L)
        .idNumHorizontalProperty1(1L)
        .idNumHorizontalProperty2(1L)
        .idNumHorizontalProperty3(1L)
        .idNumHorizontalProperty4(1L)
        .idNumberMainStreet(1L)
        .build();

    DataBasicCompanyDTO dataBasicCompanyDTO = new DataBasicCompanyDTO(
        1L, 
        "Error, datos incompletos!!", 
        "42", 
        1L, 
        "Error, datos incompletos!!",
        "Error, datos incompletos!!", 
        1L, 
        "Error, datos incompletos!!", 
        5L, 
        1L,
        "6625550144", 
        "6625550144", 
        "jane.doe@example.org", 
        "42", 
        "Error, datos incompletos!!", 
        addressDTO,
        true, 
        new DataContactCompanyDTO(), 
        null, 
        null
    );

    // Act and Assert
    assertThrows(UserNotFoundInDataBase.class, 
        () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    void testStepOne3() {
        // Arrange
        DataBasicCompanyDTO dataBasicCompanyDTO = mock(DataBasicCompanyDTO.class);
        when(dataBasicCompanyDTO.getAddressDTO()).thenThrow(new AffiliationError("Not all who wander are lost"));
        when(dataBasicCompanyDTO.getDataContactCompanyDTO()).thenReturn(new DataContactCompanyDTO());
        when(dataBasicCompanyDTO.getNumberWorkers()).thenReturn(1L);
        when(dataBasicCompanyDTO.getBusinessName()).thenReturn("Business Name");
        when(dataBasicCompanyDTO.getCityMunicipality()).thenReturn(1L);
        when(dataBasicCompanyDTO.getDepartment()).thenReturn(5L);
        when(dataBasicCompanyDTO.getEmail()).thenReturn("jane.doe@example.org");
        when(dataBasicCompanyDTO.getNumberDocumentPersonResponsible()).thenReturn("42");
        when(dataBasicCompanyDTO.getNumberIdentification()).thenReturn("42");
        when(dataBasicCompanyDTO.getPhoneOne()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getPhoneTwo()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getTypeDocumentIdentification()).thenReturn("Type Document Identification");
        when(dataBasicCompanyDTO.getTypeDocumentPersonResponsible()).thenReturn("Type Document Person Responsible");
        when(dataBasicCompanyDTO.getTypePerson()).thenReturn("Type Person");
        when(dataBasicCompanyDTO.getZoneLocationEmployer()).thenReturn("Zone Location Employer");

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    void testStepOne4() {
        // Arrange
        DataBasicCompanyDTO dataBasicCompanyDTO = mock(DataBasicCompanyDTO.class);
        when(dataBasicCompanyDTO.getAddressDTO()).thenThrow(new ErrorFindDocumentsAlfresco("Not all who wander are lost"));
        when(dataBasicCompanyDTO.getDataContactCompanyDTO()).thenReturn(new DataContactCompanyDTO());
        when(dataBasicCompanyDTO.getNumberWorkers()).thenReturn(1L);
        when(dataBasicCompanyDTO.getBusinessName()).thenReturn("Business Name");
        when(dataBasicCompanyDTO.getCityMunicipality()).thenReturn(1L);
        when(dataBasicCompanyDTO.getDepartment()).thenReturn(5L);
        when(dataBasicCompanyDTO.getEmail()).thenReturn("jane.doe@example.org");
        when(dataBasicCompanyDTO.getNumberDocumentPersonResponsible()).thenReturn("42");
        when(dataBasicCompanyDTO.getNumberIdentification()).thenReturn("42");
        when(dataBasicCompanyDTO.getPhoneOne()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getPhoneTwo()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getTypeDocumentIdentification()).thenReturn("Type Document Identification");
        when(dataBasicCompanyDTO.getTypeDocumentPersonResponsible()).thenReturn("Type Document Person Responsible");
        when(dataBasicCompanyDTO.getTypePerson()).thenReturn("Type Person");
        when(dataBasicCompanyDTO.getZoneLocationEmployer()).thenReturn("Zone Location Employer");

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    void testStepOne5() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");

        AffiliateMercantile affiliateMercantile2 = new AffiliateMercantile();
        affiliateMercantile2.setAddress("42 Main St");
        affiliateMercantile2.setAddressContactCompany("42 Main St");
        affiliateMercantile2.setAffiliationCancelled(true);
        affiliateMercantile2.setAffiliationStatus("Affiliation Status");
        affiliateMercantile2.setAfp(2L);
        affiliateMercantile2.setBusinessName("Business Name");
        affiliateMercantile2.setCityMunicipality(1L);
        affiliateMercantile2.setCityMunicipalityContactCompany(1L);
        affiliateMercantile2.setDateRequest("2020-03-01");
        affiliateMercantile2.setDepartment(5L);
        affiliateMercantile2.setDepartmentContactCompany(5L);
        affiliateMercantile2.setDigitVerificationDV(1);
        affiliateMercantile2.setEmail("jane.doe@example.org");
        affiliateMercantile2.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile2.setEps(27L);
        affiliateMercantile2.setFiledNumber("42");
        affiliateMercantile2.setId(1L);
        affiliateMercantile2.setIdCardinalPoint2(1L);
        affiliateMercantile2.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile2.setIdCardinalPointMainStreet(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile2.setIdCity(1L);
        affiliateMercantile2.setIdCityContactCompany(1L);
        affiliateMercantile2.setIdDepartment(1L);
        affiliateMercantile2.setIdDepartmentContactCompany(1L);
        affiliateMercantile2.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile2.setIdHorizontalProperty1(1L);
        affiliateMercantile2.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty2(1L);
        affiliateMercantile2.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty3(1L);
        affiliateMercantile2.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty4(1L);
        affiliateMercantile2.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdLetter1MainStreet(1L);
        affiliateMercantile2.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter2MainStreet(1L);
        affiliateMercantile2.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetterSecondStreet(1L);
        affiliateMercantile2.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile2.setIdMainStreet(1L);
        affiliateMercantile2.setIdMainStreetContactCompany(1L);
        affiliateMercantile2.setIdNum1SecondStreet(1L);
        affiliateMercantile2.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum2SecondStreet(1L);
        affiliateMercantile2.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdNumberMainStreet(1L);
        affiliateMercantile2.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile2.setIdUserPreRegister(1L);
        affiliateMercantile2.setIsBis(true);
        affiliateMercantile2.setIsBisContactCompany(true);
        affiliateMercantile2.setNumberDocumentPersonResponsible("42");
        affiliateMercantile2.setNumberIdentification("42");
        affiliateMercantile2.setNumberWorkers(1L);
        affiliateMercantile2.setPhoneOne("6625550144");
        affiliateMercantile2.setPhoneOneContactCompany("6625550144");
        affiliateMercantile2.setPhoneTwo("6625550144");
        affiliateMercantile2.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile2.setStageManagement("Stage Management");
        affiliateMercantile2.setStatusDocument(true);
        affiliateMercantile2.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile2.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile2.setTypePerson("Type Person");
        affiliateMercantile2.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile2);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any())).thenReturn(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        DataContactCompanyDTO dataContactCompanyDTO = new DataContactCompanyDTO();
        AddressDTO addressDTO = AddressDTO.builder()
                .idCardinalPoint2(1L)
                .idCardinalPointMainStreet(1L)
                .idCity(1L)
                .idDepartment(1L)
                .idHorizontalProperty1(1L)
                .idHorizontalProperty2(1L)
                .idHorizontalProperty3(1L)
                .idHorizontalProperty4(1L)
                .idLetter1MainStreet(1L)
                .idLetter2MainStreet(1L)
                .idLetterSecondStreet(1L)
                .idMainStreet(1L)
                .idNum1SecondStreet(1L)
                .idNum2SecondStreet(1L)
                .idNumHorizontalProperty1(1L)
                .idNumHorizontalProperty2(1L)
                .idNumHorizontalProperty3(1L)
                .idNumHorizontalProperty4(1L)
                .idNumberMainStreet(1L)
                .build();
        dataContactCompanyDTO.setAddressDTO(addressDTO);
        DataBasicCompanyDTO dataBasicCompanyDTO = mock(DataBasicCompanyDTO.class);
        AddressDTO buildResult = AddressDTO.builder()
                .idCardinalPoint2(1L)
                .idCardinalPointMainStreet(1L)
                .idCity(1L)
                .idDepartment(1L)
                .idHorizontalProperty1(1L)
                .idHorizontalProperty2(1L)
                .idHorizontalProperty3(1L)
                .idHorizontalProperty4(1L)
                .idLetter1MainStreet(1L)
                .idLetter2MainStreet(1L)
                .idLetterSecondStreet(1L)
                .idMainStreet(1L)
                .idNum1SecondStreet(1L)
                .idNum2SecondStreet(1L)
                .idNumHorizontalProperty1(1L)
                .idNumHorizontalProperty2(1L)
                .idNumHorizontalProperty3(1L)
                .idNumHorizontalProperty4(1L)
                .idNumberMainStreet(1L)
                .build();
        when(dataBasicCompanyDTO.getAddressDTO()).thenReturn(buildResult);
        when(dataBasicCompanyDTO.getDataContactCompanyDTO()).thenReturn(dataContactCompanyDTO);
        when(dataBasicCompanyDTO.getNumberWorkers()).thenReturn(1L);
        when(dataBasicCompanyDTO.getBusinessName()).thenReturn("Business Name");
        when(dataBasicCompanyDTO.getCityMunicipality()).thenReturn(1L);
        when(dataBasicCompanyDTO.getDepartment()).thenReturn(5L);
        when(dataBasicCompanyDTO.getEmail()).thenReturn("jane.doe@example.org");
        when(dataBasicCompanyDTO.getNumberDocumentPersonResponsible()).thenReturn("42");
        when(dataBasicCompanyDTO.getNumberIdentification()).thenReturn("42");
        when(dataBasicCompanyDTO.getPhoneOne()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getPhoneTwo()).thenReturn("6625550144");
        when(dataBasicCompanyDTO.getTypeDocumentIdentification()).thenReturn("Type Document Identification");
        when(dataBasicCompanyDTO.getTypeDocumentPersonResponsible()).thenReturn("Type Document Person Responsible");
        when(dataBasicCompanyDTO.getTypePerson()).thenReturn("Type Person");
        when(dataBasicCompanyDTO.getZoneLocationEmployer()).thenReturn("Zone Location Employer");

        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(100);
        UserMain userRegister = new UserMain();
        userRegister.setDateBirth(LocalDate.of(1970, 1, 1));
        userRegister.setPensionFundAdministrator(1L);
        userRegister.setHealthPromotingEntity(1L);
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(Mockito.<String>any(), Mockito.<String>any())).thenReturn(Optional.of(userRegister));

        // Act
        AffiliateMercantile actualStepOneResult = affiliationEmployerActivitiesMercantileServiceImpl
                .stepOne(dataBasicCompanyDTO);

        // Assert
        verify(dataBasicCompanyDTO).getBusinessName();
        verify(dataBasicCompanyDTO, atLeast(1)).getDataContactCompanyDTO();
        verify(dataBasicCompanyDTO).getEmail();
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
        assertSame(affiliateMercantile, actualStepOneResult);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#findUser(AffiliateMercantile)}
     */
    @Test
    void testFindUser() {
        // Arrange
        UserMain userMain = new UserMain();
        userMain.setAcceptNotification(true);
        userMain.setAddress("42 Main St");
        userMain.setAge(1);
        userMain.setCompanyName("Company Name");
        userMain.setCreateDate(mock(Timestamp.class));
        LocalDate dateBirth = LocalDate.of(1970, 1, 1);
        userMain.setDateBirth(dateBirth);
        userMain.setEmail("jane.doe@example.org");
        userMain.setFirstName("Jane");
        userMain.setGenerateAttempts(1);
        userMain.setGenerateOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setId(1L);
        userMain.setIdCardinalPoint2(1L);
        userMain.setIdCardinalPointMainStreet(1L);
        userMain.setIdCity(1L);
        userMain.setIdDepartment(1L);
        userMain.setIdHorizontalProperty1(1L);
        userMain.setIdHorizontalProperty2(1L);
        userMain.setIdHorizontalProperty3(1L);
        userMain.setIdHorizontalProperty4(1L);
        userMain.setIdLetter1MainStreet(1L);
        userMain.setIdLetter2MainStreet(1L);
        userMain.setIdLetterSecondStreet(1L);
        userMain.setIdMainStreet(1L);
        userMain.setIdNum1SecondStreet(1L);
        userMain.setIdNum2SecondStreet(1L);
        userMain.setIdNumHorizontalProperty1(1L);
        userMain.setIdNumHorizontalProperty2(1L);
        userMain.setIdNumHorizontalProperty3(1L);
        userMain.setIdNumHorizontalProperty4(1L);
        userMain.setIdNumberMainStreet(1L);
        userMain.setIdentification("Identification");
        userMain.setIdentificationType("Identification Type");
        userMain.setIsBis(true);
        userMain.setLastAffiliationAttempt(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLastUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLockoutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLoginAttempts(1);
        userMain.setNationality(1L);
        userMain.setOtherSex("Other Sex");
        userMain.setPhoneNumber("6625550144");
        userMain.setPhoneNumber2("6625550144");
        userMain.setPin("Pin");
        userMain.setProfile("Profile");
        userMain.setSecondName("Second Name");
        userMain.setSecondSurname("Doe");
        userMain.setSex("Sex");
        userMain.setStatus(1L);
        userMain.setStatusActive(true);
        userMain.setStatusInactiveSince(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setStatusPreRegister(true);
        userMain.setStatusStartAfiiliate(true);
        userMain.setSurname("Doe");
        userMain.setUserType(1L);
        userMain.setValidAttempts(1);
        userMain.setValidOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setVerificationDigit(1);
        Optional<UserMain> ofResult = Optional.of(userMain);
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");

        // Act
        DataLegalRepresentativeDTO actualFindUserResult = affiliationEmployerActivitiesMercantileServiceImpl
                .findUser(affiliateMercantile);

        // Assert
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
        LocalDate dateBirth2 = actualFindUserResult.getDateBirth();
        assertEquals("1970-01-01", dateBirth2.toString());
        AddressDTO addressDTO = actualFindUserResult.getAddressDTO();
        assertEquals("Jane", actualFindUserResult.getFirstName());
        assertEquals("Second Name", actualFindUserResult.getSecondName());
        assertEquals("Sex", actualFindUserResult.getSex());
        assertEquals("Type Person", actualFindUserResult.getTypePerson());
        assertEquals("jane.doe@example.org", actualFindUserResult.getEmail());
        assertEquals(1, actualFindUserResult.getAge());
        assertEquals(1L, addressDTO.getIdNumHorizontalProperty4().longValue());
        assertEquals(1L, addressDTO.getIdNumberMainStreet().longValue());
        assertEquals(1L, actualFindUserResult.getIdAffiliationMercantile().longValue());
        assertEquals(1L, actualFindUserResult.getIdCity().longValue());
        assertEquals(1L, actualFindUserResult.getIdDepartment().longValue());
        assertTrue(addressDTO.getIsBis());
        assertSame(dateBirth, dateBirth2);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO, boolean)} (DataLegalRepresentativeDTO)}
     */
    @Test
    void testStepTwo() {
        // Arrange
        UserMain userMain = new UserMain();
        userMain.setAcceptNotification(true);
        userMain.setAddress("42 Main St");
        userMain.setAge(1);
        userMain.setCompanyName("Company Name");
        userMain.setCreateDate(mock(Timestamp.class));
        userMain.setDateBirth(LocalDate.of(1970, 1, 1));
        userMain.setEmail("jane.doe@example.org");
        userMain.setFirstName("Jane");
        userMain.setGenerateAttempts(1);
        userMain.setGenerateOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setId(1L);
        userMain.setIdCardinalPoint2(1L);
        userMain.setIdCardinalPointMainStreet(1L);
        userMain.setIdCity(1L);
        userMain.setIdDepartment(1L);
        userMain.setIdHorizontalProperty1(1L);
        userMain.setIdHorizontalProperty2(1L);
        userMain.setIdHorizontalProperty3(1L);
        userMain.setIdHorizontalProperty4(1L);
        userMain.setIdLetter1MainStreet(1L);
        userMain.setIdLetter2MainStreet(1L);
        userMain.setIdLetterSecondStreet(1L);
        userMain.setIdMainStreet(1L);
        userMain.setIdNum1SecondStreet(1L);
        userMain.setIdNum2SecondStreet(1L);
        userMain.setIdNumHorizontalProperty1(1L);
        userMain.setIdNumHorizontalProperty2(1L);
        userMain.setIdNumHorizontalProperty3(1L);
        userMain.setIdNumHorizontalProperty4(1L);
        userMain.setIdNumberMainStreet(1L);
        userMain.setIdentification("Identification");
        userMain.setIdentificationType("Identification Type");
        userMain.setIsBis(true);
        userMain.setLastAffiliationAttempt(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLastUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLockoutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLoginAttempts(1);
        userMain.setNationality(1L);
        userMain.setOtherSex("Other Sex");
        userMain.setPhoneNumber("6625550144");
        userMain.setPhoneNumber2("6625550144");
        userMain.setPin("Pin");
        userMain.setProfile("Profile");
        userMain.setSecondName("Second Name");
        userMain.setSecondSurname("Doe");
        userMain.setSex("Sex");
        userMain.setStatus(1L);
        userMain.setStatusActive(true);
        userMain.setStatusInactiveSince(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setStatusPreRegister(true);
        userMain.setStatusStartAfiiliate(true);
        userMain.setSurname("Doe");
        userMain.setUserType(1L);
        userMain.setValidAttempts(1);
        userMain.setValidOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setVerificationDigit(1);
        Optional<UserMain> ofResult = Optional.of(userMain);
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult2);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(new DataLegalRepresentativeDTO(), false));
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findById(isNull());
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO, boolean)} (DataLegalRepresentativeDTO)}
     */
    @Test
    void testStepTwo2() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = new DataLegalRepresentativeDTO();

        // Act and Assert
        assertThrows(AffiliationError.class, 
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(dataLegalRepresentativeDTO, false));
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findById(isNull());
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,Long,Long, List)}
     */
    @Test
    void testStepThree() {

        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(alfrescoService.createFolder(Mockito.<String>any(), Mockito.<String>any())).thenReturn(alfrescoUploadResponse);

        // Act
        AffiliateMercantileDTO actualStepThreeResult = affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L,1L,1L,
                new ArrayList<>());

        // Assert

        verify(affiliateMercantileRepository).findById(1L);
        assertEquals("42 Main St", actualStepThreeResult.getAddress());
        assertEquals("42 Main St", actualStepThreeResult.getAddressContactCompany());
        assertEquals("42", actualStepThreeResult.getNumberIdentification());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOne());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOneContactCompany());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwo());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwoContactCompany());
        assertEquals(2L, actualStepThreeResult.getAfp());
        assertEquals("Business Name", actualStepThreeResult.getBusinessName());
        assertEquals(5L, actualStepThreeResult.getDepartmentContactCompany());
        assertEquals(5L, actualStepThreeResult.getDepartment());
        assertEquals(27L, actualStepThreeResult.getEps());
        assertEquals(1L, actualStepThreeResult.getCityMunicipality());
        assertEquals(1L, actualStepThreeResult.getCityMunicipalityContactCompany());
        assertEquals("Stage Management", actualStepThreeResult.getStageManagement());
        assertEquals("Type Document Identification", actualStepThreeResult.getTypeDocumentIdentification());
        assertEquals("Type Person", actualStepThreeResult.getTypePerson());
        assertEquals("Zone Location Employer", actualStepThreeResult.getZoneLocationEmployer());
        assertEquals("jane.doe@example.org", actualStepThreeResult.getEmail());
        assertEquals("jane.doe@example.org", actualStepThreeResult.getEmailContactCompany());
        assertNull(actualStepThreeResult.getActivityEconomicPrimary());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryFour());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryOne());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryThree());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryTwo());
        assertEquals(1, actualStepThreeResult.getDigitVerificationDV().intValue());
        assertEquals(1L, actualStepThreeResult.getId().longValue());
        assertEquals(1L, actualStepThreeResult.getIdUserPreRegister().longValue());
        assertEquals(1L, actualStepThreeResult.getNumberWorkers().longValue());
        assertTrue(actualStepThreeResult.getAffiliationCancelled());
        assertTrue(actualStepThreeResult.getStatusDocument());
        assertTrue(actualStepThreeResult.getDocuments().isEmpty());
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long, List)}
     */
    @Test
    void testStepThree2() {

        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAffiliationCancelled()).thenReturn(true);
        when(affiliateMercantile.getStatusDocument()).thenReturn(true);
        when(affiliateMercantile.getDigitVerificationDV()).thenReturn(1);
        when(affiliateMercantile.getId()).thenReturn(1L);
        when(affiliateMercantile.getIdUserPreRegister()).thenReturn(1L);
        when(affiliateMercantile.getAddress()).thenReturn("42 Main St");
        when(affiliateMercantile.getAddressContactCompany()).thenReturn("42 Main St");
        when(affiliateMercantile.getAfp()).thenReturn(2L);
        when(affiliateMercantile.getBusinessName()).thenReturn("Business Name");
        when(affiliateMercantile.getCityMunicipality()).thenReturn(1L);
        when(affiliateMercantile.getCityMunicipalityContactCompany()).thenReturn(1L);
        when(affiliateMercantile.getDateRequest()).thenReturn("2020-03-01");
        when(affiliateMercantile.getDepartment()).thenReturn(5L);
        when(affiliateMercantile.getDepartmentContactCompany()).thenReturn(5L);
        when(affiliateMercantile.getEmail()).thenReturn("jane.doe@example.org");
        when(affiliateMercantile.getEmailContactCompany()).thenReturn("jane.doe@example.org");
        when(affiliateMercantile.getEps()).thenReturn(27L);
        when(affiliateMercantile.getIdFolderAlfresco()).thenReturn("Id Folder Alfresco");
        when(affiliateMercantile.getPhoneOne()).thenReturn("6625550144");
        when(affiliateMercantile.getPhoneOneContactCompany()).thenReturn("6625550144");
        when(affiliateMercantile.getPhoneTwo()).thenReturn("6625550144");
        when(affiliateMercantile.getPhoneTwoContactCompany()).thenReturn("6625550144");
        when(affiliateMercantile.getTypeDocumentIdentification()).thenReturn("Type Document Identification");
        when(affiliateMercantile.getTypePerson()).thenReturn("Type Person");
        when(affiliateMercantile.getZoneLocationEmployer()).thenReturn("Zone Location Employer");
        when(affiliateMercantile.getFiledNumber()).thenReturn("42");
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn("Stage Management");
        when(affiliateMercantile.getIdEmployerSize()).thenReturn(1L);
        when(affiliateMercantile.getRealNumberWorkers()).thenReturn(0L);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(alfrescoService.createFolder(Mockito.<String>any(), Mockito.<String>any())).thenReturn(alfrescoUploadResponse);

        // Act
        AffiliateMercantileDTO actualStepThreeResult = affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L,
                new ArrayList<>());

        // Assert
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getAddress();
        verify(affiliateMercantile).getAddressContactCompany();
        verify(affiliateMercantile).getAfp();
        verify(affiliateMercantile).getCityMunicipality();
        verify(affiliateMercantile).getCityMunicipalityContactCompany();
        verify(affiliateMercantile).getDateRequest();
        verify(affiliateMercantile).getDepartment();
        verify(affiliateMercantile).getDepartmentContactCompany();
        verify(affiliateMercantile).getDigitVerificationDV();
        verify(affiliateMercantile).getEmail();
        verify(affiliateMercantile).getEmailContactCompany();
        verify(affiliateMercantile).getEps();
        verify(affiliateMercantile, atLeast(1)).getFiledNumber();
        verify(affiliateMercantile).getId();
        verify(affiliateMercantile).getIdFolderAlfresco();
        verify(affiliateMercantile, atLeast(1)).getNumberIdentification();
        verify(affiliateMercantile, atLeastOnce()).getNumberWorkers();
        verify(affiliateMercantile).getPhoneOne();
        verify(affiliateMercantile).getPhoneOneContactCompany();
        verify(affiliateMercantile).getPhoneTwo();
        verify(affiliateMercantile).getPhoneTwoContactCompany();
        verify(affiliateMercantile, atLeast(1)).getStageManagement();
        verify(affiliateMercantile, atLeastOnce()).getTypeDocumentIdentification();
        verify(affiliateMercantile).getTypePerson();
        verify(affiliateMercantile).getZoneLocationEmployer();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantileRepository).findById(1L);
        assertEquals("2020-03-01", actualStepThreeResult.getDateRequest());
        assertEquals("42 Main St", actualStepThreeResult.getAddress());
        assertEquals("42 Main St", actualStepThreeResult.getAddressContactCompany());
        assertEquals("42", actualStepThreeResult.getFiledNumber());
        assertEquals("42", actualStepThreeResult.getNumberIdentification());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOne());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOneContactCompany());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwo());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwoContactCompany());
        assertEquals(2L, actualStepThreeResult.getAfp());
        assertEquals("Business Name", actualStepThreeResult.getBusinessName());
        assertEquals(5L, actualStepThreeResult.getDepartmentContactCompany());
        assertEquals(5L, actualStepThreeResult.getDepartment());
        assertEquals(27L, actualStepThreeResult.getEps());
        assertEquals("Id Folder Alfresco", actualStepThreeResult.getIdFolderAlfresco());
        assertEquals(1L, actualStepThreeResult.getCityMunicipality());
        assertEquals(1L, actualStepThreeResult.getCityMunicipalityContactCompany());
        assertEquals("Stage Management", actualStepThreeResult.getStageManagement());
        assertEquals("Type Document Identification", actualStepThreeResult.getTypeDocumentIdentification());
        assertEquals("Type Person", actualStepThreeResult.getTypePerson());
        assertEquals("Zone Location Employer", actualStepThreeResult.getZoneLocationEmployer());
        assertEquals("jane.doe@example.org", actualStepThreeResult.getEmail());
        assertEquals("jane.doe@example.org", actualStepThreeResult.getEmailContactCompany());
        assertNull(actualStepThreeResult.getActivityEconomicPrimary());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryFour());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryOne());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryThree());
        assertNull(actualStepThreeResult.getActivityEconomicSecondaryTwo());
        assertEquals(1, actualStepThreeResult.getDigitVerificationDV().intValue());
        assertEquals(1L, actualStepThreeResult.getId().longValue());
        assertEquals(1L, actualStepThreeResult.getIdUserPreRegister().longValue());
        assertEquals(1L, actualStepThreeResult.getNumberWorkers().longValue());
        assertTrue(actualStepThreeResult.getAffiliationCancelled());
        assertTrue(actualStepThreeResult.getDocuments().isEmpty());
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long, List)}
     */
    @Test
    void testStepThree3() {

        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAddress()).thenThrow(new IllegalArgumentException("firma"));
        when(affiliateMercantile.getFiledNumber()).thenReturn("42");
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn("Stage Management");
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L, new ArrayList<>()));
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getNumberIdentification();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantile).setRealNumberWorkers(0L);
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long, List)}
     */
    @Test
    void testStepThree4() {

        when(affiliateService.getEmployerSize(1)).thenReturn(1L);

        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getFiledNumber()).thenReturn(null);
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn("Stage Management");
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L, new ArrayList<>()));
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getNumberIdentification();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile, atLeast(1)).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantile).setRealNumberWorkers(0L);
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,  List)}
     */
    @Test
    void testStepThree5() {

        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn("firma");
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L, new ArrayList<>()));
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getNumberIdentification();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantile).setRealNumberWorkers(0L);
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,  List)}
     */
    @Test
    void testStepThree6() {

        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn("entrevista web");
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L,  new ArrayList<>()));
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getNumberIdentification();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantile).setRealNumberWorkers(0L);
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long, List)}
     */
    @Test
    void testStepThree7() {

        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getNumberIdentification()).thenReturn("42");
        when(affiliateMercantile.getStageManagement()).thenReturn(null);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdEmployerSize(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setRealNumberWorkers(Mockito.<Long>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setRealNumberWorkers(0L);
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantile.getNumberWorkers()).thenReturn(1L);
        when(affiliateService.getEmployerSize(1)).thenReturn(1L);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L,  new ArrayList<>()));
        verify(affiliateMercantile, atLeastOnce()).setIdEmployerSize(1L);
        verify(affiliateMercantile).getNumberIdentification();
        verify(affiliateMercantile).setAddress("42 Main St");
        verify(affiliateMercantile).setAddressContactCompany("42 Main St");
        verify(affiliateMercantile).setAffiliationCancelled(true);
        verify(affiliateMercantile).setAffiliationStatus("Affiliation Status");
        verify(affiliateMercantile).setAfp(2L);
        verify(affiliateMercantile).setBusinessName("Business Name");
        verify(affiliateMercantile).setCityMunicipality(1L);
        verify(affiliateMercantile).setCityMunicipalityContactCompany(1L);
        verify(affiliateMercantile).setDateRequest("2020-03-01");
        verify(affiliateMercantile).setDepartment(5L);
        verify(affiliateMercantile).setDepartmentContactCompany(5L);
        verify(affiliateMercantile).setDigitVerificationDV(1);
        verify(affiliateMercantile).setEmail("jane.doe@example.org");
        verify(affiliateMercantile).setEmailContactCompany("jane.doe@example.org");
        verify(affiliateMercantile).setEps(27L);
        verify(affiliateMercantile).setFiledNumber("42");
        verify(affiliateMercantile).setId(1L);
        verify(affiliateMercantile).setIdCardinalPoint2(1L);
        verify(affiliateMercantile).setIdCardinalPoint2ContactCompany(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreet(1L);
        verify(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdCity(1L);
        verify(affiliateMercantile).setIdCityContactCompany(1L);
        verify(affiliateMercantile).setIdDepartment(1L);
        verify(affiliateMercantile).setIdDepartmentContactCompany(1L);
        verify(affiliateMercantile).setIdFolderAlfresco("Id Folder Alfresco");
        verify(affiliateMercantile).setIdHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdLetter1MainStreet(1L);
        verify(affiliateMercantile).setIdLetter1MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetter2MainStreet(1L);
        verify(affiliateMercantile).setIdLetter2MainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdLetterSecondStreet(1L);
        verify(affiliateMercantile).setIdLetterSecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdMainStreet(1L);
        verify(affiliateMercantile).setIdMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum1SecondStreet(1L);
        verify(affiliateMercantile).setIdNum1SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNum2SecondStreet(1L);
        verify(affiliateMercantile).setIdNum2SecondStreetContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4(1L);
        verify(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(1L);
        verify(affiliateMercantile).setIdNumberMainStreet(1L);
        verify(affiliateMercantile).setIdNumberMainStreetContactCompany(1L);
        verify(affiliateMercantile).setIdUserPreRegister(1L);
        verify(affiliateMercantile).setIsBis(true);
        verify(affiliateMercantile).setIsBisContactCompany(true);
        verify(affiliateMercantile).setNumberDocumentPersonResponsible("42");
        verify(affiliateMercantile).setNumberIdentification("42");
        verify(affiliateMercantile).setNumberWorkers(1L);
        verify(affiliateMercantile).setPhoneOne("6625550144");
        verify(affiliateMercantile).setPhoneOneContactCompany("6625550144");
        verify(affiliateMercantile).setPhoneTwo("6625550144");
        verify(affiliateMercantile).setPhoneTwoContactCompany("6625550144");
        verify(affiliateMercantile).setStageManagement("Stage Management");
        verify(affiliateMercantile).setStatusDocument(true);
        verify(affiliateMercantile).setTypeDocumentIdentification("Type Document Identification");
        verify(affiliateMercantile).setTypeDocumentPersonResponsible("Type Document Person Responsible");
        verify(affiliateMercantile).setTypePerson("Type Person");
        verify(affiliateMercantile).setZoneLocationEmployer("Zone Location Employer");
        verify(affiliateMercantile).setRealNumberWorkers(0L);
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    void testStateDocuments() {
        // Arrange
        DataDocumentAffiliate dataDocumentAffiliate = new DataDocumentAffiliate();
        dataDocumentAffiliate.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate.setId(1L);
        dataDocumentAffiliate.setIdAffiliate(1L);
        dataDocumentAffiliate.setIdAlfresco("Id Alfresco");
        dataDocumentAffiliate.setName("Name");
        dataDocumentAffiliate.setRevised(true);
        dataDocumentAffiliate.setState(true);
        Optional<DataDocumentAffiliate> ofResult = Optional.of(dataDocumentAffiliate);

        DataDocumentAffiliate dataDocumentAffiliate2 = new DataDocumentAffiliate();
        dataDocumentAffiliate2.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate2.setId(1L);
        dataDocumentAffiliate2.setIdAffiliate(1L);
        dataDocumentAffiliate2.setIdAlfresco("Id Alfresco");
        dataDocumentAffiliate2.setName("Name");
        dataDocumentAffiliate2.setRevised(true);
        dataDocumentAffiliate2.setState(true);
        when(dataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any())).thenReturn(dataDocumentAffiliate2);
        when(dataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        DocumentsDTO buildResult = DocumentsDTO.builder()
                .dateTime("2020-03-01")
                .id(1L)
                .idDocument("Id Document")
                .name("Name")
                .reject(true)
                .revised(true)
                .build();
        listDocumentsDTOS.add(buildResult);

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L);

        // Assert
        verify(dataDocumentRepository).findById(1L);
        verify(dataDocumentRepository).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    void testStateDocuments2() {
        // Arrange
        DataDocumentAffiliate dataDocumentAffiliate = new DataDocumentAffiliate();
        dataDocumentAffiliate.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate.setId(1L);
        dataDocumentAffiliate.setIdAffiliate(1L);
        dataDocumentAffiliate.setIdAlfresco("Id Alfresco");
        dataDocumentAffiliate.setName("Name");
        dataDocumentAffiliate.setRevised(true);
        dataDocumentAffiliate.setState(true);
        Optional<DataDocumentAffiliate> ofResult = Optional.of(dataDocumentAffiliate);
        when(dataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(dataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        DocumentsDTO buildResult = DocumentsDTO.builder()
                .dateTime("2020-03-01")
                .id(1L)
                .idDocument("Id Document")
                .name("Name")
                .reject(true)
                .revised(true)
                .build();
        listDocumentsDTOS.add(buildResult);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L));
        verify(dataDocumentRepository).findById(1L);
        verify(dataDocumentRepository).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    void testStateDocuments3() {
        // Arrange
        Optional<DataDocumentAffiliate> emptyResult = Optional.empty();
        when(dataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(emptyResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        DocumentsDTO buildResult = DocumentsDTO.builder()
                .dateTime("2020-03-01")
                .id(1L)
                .idDocument("Id Document")
                .name("Name")
                .reject(true)
                .revised(true)
                .build();
        listDocumentsDTOS.add(buildResult);

        // Act and Assert
        assertThrows(ErrorFindDocumentsAlfresco.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L));
        verify(dataDocumentRepository).findById(1L);
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    void testStateDocuments4() {
        // Arrange
        DataDocumentAffiliate dataDocumentAffiliate = new DataDocumentAffiliate();
        dataDocumentAffiliate.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate.setId(1L);
        dataDocumentAffiliate.setIdAffiliate(1L);
        dataDocumentAffiliate.setIdAlfresco("Id Alfresco");
        dataDocumentAffiliate.setName("Name");
        dataDocumentAffiliate.setRevised(true);
        dataDocumentAffiliate.setState(true);
        Optional<DataDocumentAffiliate> ofResult = Optional.of(dataDocumentAffiliate);

        DataDocumentAffiliate dataDocumentAffiliate2 = new DataDocumentAffiliate();
        dataDocumentAffiliate2.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate2.setId(1L);
        dataDocumentAffiliate2.setIdAffiliate(1L);
        dataDocumentAffiliate2.setIdAlfresco("Id Alfresco");
        dataDocumentAffiliate2.setName("Name");
        dataDocumentAffiliate2.setRevised(true);
        dataDocumentAffiliate2.setState(true);
        when(dataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any())).thenReturn(dataDocumentAffiliate2);
        when(dataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        DocumentsDTO buildResult = DocumentsDTO.builder()
                .dateTime("2020-03-01")
                .id(1L)
                .idDocument("Id Document")
                .name("Name")
                .reject(true)
                .revised(true)
                .build();
        listDocumentsDTOS.add(buildResult);
        DocumentsDTO buildResult2 = DocumentsDTO.builder()
                .dateTime("2020-03-01")
                .id(1L)
                .idDocument("Id Document")
                .name("Name")
                .reject(true)
                .revised(true)
                .build();
        listDocumentsDTOS.add(buildResult2);

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L);

        // Assert
        verify(dataDocumentRepository, atLeast(1)).findById(1L);
        verify(dataDocumentRepository, atLeast(1)).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateAffiliation(AffiliateMercantile, StateAffiliation)}
     */
    @Test
    void testStateAffiliation() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stateAffiliation(affiliateMercantile, new StateAffiliation()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateAffiliation(AffiliateMercantile, StateAffiliation)}
     */
    @Test
    void testStateAffiliation2() {
        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAffiliationCancelled()).thenReturn(true);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stateAffiliation(affiliateMercantile, new StateAffiliation()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateAffiliation(AffiliateMercantile, StateAffiliation)}
     */
    @Test
    void testStateAffiliation3() {
        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAffiliationCancelled()).thenReturn(false);
        when(affiliateMercantile.getStatusDocument()).thenReturn(true);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stateAffiliation(affiliateMercantile, new StateAffiliation()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testScheduleInterviewWeb() {
        // Arrange
        when(scheduleInterviewWebService.createScheduleInterviewWeb(Mockito.<DateInterviewWebDTO>any())).thenReturn(new HashMap<>());

        // Act
        Map<String, Object> actualScheduleInterviewWebResult = affiliationEmployerActivitiesMercantileServiceImpl
                .scheduleInterviewWeb(new DateInterviewWebDTO());

        // Assert
        verify(scheduleInterviewWebService).createScheduleInterviewWeb(isA(DateInterviewWebDTO.class));
        assertTrue(actualScheduleInterviewWebResult.isEmpty());
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testScheduleInterviewWeb2() {
        // Arrange
        when(scheduleInterviewWebService.createScheduleInterviewWeb(Mockito.<DateInterviewWebDTO>any())).thenReturn(new HashMap<>());

        // Act
        Map<String, Object> actualScheduleInterviewWebResult = affiliationEmployerActivitiesMercantileServiceImpl
                .scheduleInterviewWeb(new DateInterviewWebDTO());

        // Assert
        verify(scheduleInterviewWebService).createScheduleInterviewWeb(isA(DateInterviewWebDTO.class));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testScheduleInterviewWeb3() {
        // Arrange
        when(scheduleInterviewWebService.createScheduleInterviewWeb(Mockito.<DateInterviewWebDTO>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.scheduleInterviewWeb(new DateInterviewWebDTO()));
        verify(scheduleInterviewWebService).createScheduleInterviewWeb(isA(DateInterviewWebDTO.class));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}
     */
    @Test
    void testInterviewWeb() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.interviewWeb(new StateAffiliation()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}
     */
    @Test
    void testInterviewWeb2() {
        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAffiliationCancelled()).thenReturn(true);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.interviewWeb(new StateAffiliation()));
    }

    /**
     * Method under test:
     * {@link AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}
     */
    @Test
    void testInterviewWeb3() {
        // Arrange
        AffiliateMercantile affiliateMercantile = mock(AffiliateMercantile.class);
        when(affiliateMercantile.getAffiliationCancelled()).thenReturn(false);
        when(affiliateMercantile.getStatusDocument()).thenReturn(true);
        doNothing().when(affiliateMercantile).setAddress(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAddressContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAffiliationCancelled(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setAffiliationStatus(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setAfp(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setBusinessName(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setCityMunicipality(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setCityMunicipalityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDateRequest(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setDigitVerificationDV(Mockito.<Integer>any());
        doNothing().when(affiliateMercantile).setEmail(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEmailContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setEps(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setFiledNumber(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setId(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPoint2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCardinalPointMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCity(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdCityContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartment(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdDepartmentContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdFolderAlfresco(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter1MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetter2MainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdLetterSecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum1SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNum2SecondStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty1ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty2ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty3ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumHorizontalProperty4ContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreet(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdNumberMainStreetContactCompany(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIdUserPreRegister(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setIsBis(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setIsBisContactCompany(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setNumberDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setNumberWorkers(Mockito.<Long>any());
        doNothing().when(affiliateMercantile).setPhoneOne(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneOneContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwo(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setPhoneTwoContactCompany(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStageManagement(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setStatusDocument(Mockito.<Boolean>any());
        doNothing().when(affiliateMercantile).setTypeDocumentIdentification(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypeDocumentPersonResponsible(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setTypePerson(Mockito.<String>any());
        doNothing().when(affiliateMercantile).setZoneLocationEmployer(Mockito.<String>any());
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(2L);
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDepartment(5L);
        affiliateMercantile.setDepartmentContactCompany(5L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(27L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.interviewWeb(new StateAffiliation()));
    }
}
