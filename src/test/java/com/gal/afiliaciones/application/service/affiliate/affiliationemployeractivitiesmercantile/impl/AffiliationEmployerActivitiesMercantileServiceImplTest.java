package com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.MercantileFormService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.impl.MainOfficeServiceImpl;
import com.gal.afiliaciones.application.service.affiliate.impl.ScheduleInterviewWebServiceImpl;
import com.gal.afiliaciones.application.service.affiliate.impl.WorkCenterServiceImpl;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.DomesticServiceIndependentServiceReportService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl.SendEmailImpl;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoServiceImpl;
import com.gal.afiliaciones.application.service.consecutive.ConsecutiveService;
import com.gal.afiliaciones.application.service.daily.impl.DailyServiceImpl;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.economicactivity.impl.EconomicActivityServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.filed.FiledServiceImpl;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.application.service.identificationlegalnature.impl.IdentificationLegalNatureServiceImpl;
import com.gal.afiliaciones.application.service.impl.CodeValidCertificationServiceImpl;
import com.gal.afiliaciones.application.service.impl.KeycloakServiceImpl;
import com.gal.afiliaciones.application.service.impl.UserPreRegisterServiceImpl;
import com.gal.afiliaciones.application.service.impl.UserStatusUpdateService;
import com.gal.afiliaciones.application.service.impl.certicate.CertificateServiceImpl;
import com.gal.afiliaciones.application.service.impl.otp.OtpImpl;
import com.gal.afiliaciones.application.service.individualindependentaffiliation.IndividualIndependentAffiliationService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.application.service.observationsaffiliation.impl.ObservationsAffiliationServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.policy.impl.PolicyServiceImpl;
import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.application.service.typeemployerdocument.impl.TypeEmployerDocumentServiceImpl;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.sat.SatError;
import com.gal.afiliaciones.config.ex.sat.SatUpstreamError;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorDocumentConditions;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.mapper.UpdatePreRegisterMapperImpl;
import com.gal.afiliaciones.config.mapper.UserMapper;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.confecamaras.ConfecamarasClient;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.InsertHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.UpdateHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.client.registraduria.RegistraduriaClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.GenderRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateAffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICodeValidCertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IdentificationLegalNatureRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.LegalStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.form.impl.ApplicationFormDaoImpl;
import com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation.ObservationsAffiliationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.otp.OtpCodeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.impl.PolicyDaoImpl;
import com.gal.afiliaciones.infrastructure.dao.repository.systemparam.SystemParamRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.DocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.SubTypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument.TypeEmployerRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataContactCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.InterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DataUpload;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.ContentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.CreatedByUserDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDetailsDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.ListDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.ModifiedByUserDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.PaginationDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import com.gal.afiliaciones.infrastructure.service.ConfecamarasConsultationService;
import com.gal.afiliaciones.infrastructure.service.IdentityCardConsultationService;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaKeycloakTokenService;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {AffiliationEmployerActivitiesMercantileServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class AffiliationEmployerActivitiesMercantileServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @Autowired
    private AffiliationEmployerActivitiesMercantileServiceImpl
            affiliationEmployerActivitiesMercantileServiceImpl;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private ArlInformationDao arlInformationDao;

    @MockBean
    private ArlRepository arlRepository;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private ConfecamarasConsultationService confecamarasConsultationService;

    @MockBean
    private ConsultEmployerClient consultEmployerClient;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private DocumentNameStandardizationService documentNameStandardizationService;

    @MockBean
    private FiledService filedService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IAffiliationCancellationTimerRepository iAffiliationCancellationTimerRepository;

    @MockBean
    private IDataDocumentRepository iDataDocumentRepository;

    @MockBean
    private IEconomicActivityRepository iEconomicActivityRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private IUserRegisterService iUserRegisterService;

    @MockBean
    private IdentificationLegalNatureService identificationLegalNatureService;

    @MockBean
    private MainOfficeRepository mainOfficeRepository;

    @MockBean
    private MainOfficeService mainOfficeService;

    @MockBean
    private MessageErrorAge messageErrorAge;

    @MockBean
    private MunicipalityRepository municipalityRepository;

    @MockBean
    private ObservationsAffiliationService observationsAffiliationService;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private RegistraduriaUnifiedService registraduriaUnifiedService;

    @MockBean
    private SatConsultTransferableEmployerClient satConsultTransferableEmployerClient;

    @MockBean
    private ScheduleInterviewWebService scheduleInterviewWebService;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private TypeEmployerDocumentService typeEmployerDocumentService;

    @MockBean
    private WebClient webClient;

    @MockBean
    private WorkCenterRepository workCenterRepository;

    @MockBean
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;

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


    private DataBasicCompanyDTO basicDTO(String typeResp, String numResp) {
        DataBasicCompanyDTO dto = new DataBasicCompanyDTO();
        dto.setTypeDocumentPersonResponsible(typeResp);
        dto.setNumberDocumentPersonResponsible(numResp);
        dto.setBusinessName("EMPRESA");
        dto.setTypeDocumentIdentification(Constant.CC);
        dto.setNumberIdentification("123");
        return dto; // NO seteamos AddressDTO ni DataContactCompanyDTO para evitar incompatibilidades
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult);
        when(arlRepository.findByCodeARL(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(satConsultTransferableEmployerClient.consult(Mockito.<TransferableEmployerRequest>any()))
                .thenReturn(
                        TransferableEmployerResponse.builder()
                                .arlAfiliacion("Arl Afiliacion")
                                .causal(1)
                                .codigoArl("Codigo Arl")
                                .consecutivoNITEmpleador("Consecutivo NITEmpleador")
                                .empresaTrasladable("Empresa Trasladable")
                                .build());

        // Act and Assert
        assertThrows(
                SatUpstreamError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(satConsultTransferableEmployerClient).consult(isA(TransferableEmployerRequest.class));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
        verify(arlRepository).findByCodeARL("Arl Afiliacion");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne2() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult);
        when(satConsultTransferableEmployerClient.consult(Mockito.<TransferableEmployerRequest>any()))
                .thenThrow(new SatUpstreamError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                SatUpstreamError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(satConsultTransferableEmployerClient).consult(isA(TransferableEmployerRequest.class));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne3() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult);
        when(satConsultTransferableEmployerClient.consult(Mockito.<TransferableEmployerRequest>any()))
                .thenThrow(new SatError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                SatError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(satConsultTransferableEmployerClient).consult(isA(TransferableEmployerRequest.class));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne4() {
        // Arrange
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                SatUpstreamError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne5() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult);
        when(satConsultTransferableEmployerClient.consult(Mockito.<TransferableEmployerRequest>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                SatUpstreamError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(satConsultTransferableEmployerClient).consult(isA(TransferableEmployerRequest.class));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String)")
    
    void testValidationsStepOne6() {
        // Arrange
        AffiliateMercantileRepository affiliateMercantileRepository =
                mock(AffiliateMercantileRepository.class);
        when(affiliateMercantileRepository.findAll(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        WebClient webClient = mock(WebClient.class);
        EmailService emailService = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository = mock(CertificateRepository.class);
        CodeValidCertificationServiceImpl codeValidCertificationService =
                new CodeValidCertificationServiceImpl(
                        mock(ICodeValidCertificateRepository.class), mock(IUserPreRegisterRepository.class));
        AffiliateRepository affiliateRepository = mock(AffiliateRepository.class);
        WebClient webClientBuilder = mock(WebClient.class);
        GenericWebClient genericWebClient =
                new GenericWebClient(webClientBuilder, new CollectProperties(), mock(Builder.class));
        CertificateServiceHelper certificateServiceHelper = new CertificateServiceHelper();
        IQrRepository iQrRepository = mock(IQrRepository.class);
        ICardRepository iCardRepository = mock(ICardRepository.class);
        ArlInformationDao arlInformationDao = mock(ArlInformationDao.class);
        EconomicActivityServiceImpl economicActivityService =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository =
                mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        IEconomicActivityRepository economicActivityRepository =
                mock(IEconomicActivityRepository.class);
        RequestCorrectionRepository requestCorrectionRepository =
                mock(RequestCorrectionRepository.class);
        RequestCollectionRequestRepository requestCollectionRepository =
                mock(RequestCollectionRequestRepository.class);
        FiledServiceImpl filedService =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));

        CertificateServiceImpl certificateService =
                new CertificateServiceImpl(
                        certificateRepository,
                        codeValidCertificationService,
                        affiliateRepository,
                        genericWebClient,
                        certificateServiceHelper,
                        iQrRepository,
                        iCardRepository,
                        arlInformationDao,
                        economicActivityService,
                        affiliationRepository,
                        economicActivityRepository,
                        requestCorrectionRepository,
                        requestCollectionRepository,
                        filedService,
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder2 = mock(WebClient.class);
        GenericWebClient webClient2 =
                new GenericWebClient(webClientBuilder2, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao2 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository = mock(DangerRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository2 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService2 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService = mock(MercantileFormService.class);
        CollectProperties properties = new CollectProperties();
        AffiliateRepository affiliateRepository2 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService2 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository = mock(FundPensionRepository.class);
        WebClient webClient3 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService =
                new AlfrescoServiceImpl(webClient3, new CollectProperties());
        WebClient webClientBuilder3 = mock(WebClient.class);
        GenericWebClient genericWebClient2 =
                new GenericWebClient(webClientBuilder3, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails =
                new SendEmailImpl(
                        emailService,
                        certificateService,
                        webClient2,
                        arlInformationDao2,
                        formIndependentService,
                        dangerRepository,
                        departmentRepository,
                        municipalityRepository,
                        economicActivityRepository2,
                        genderRepository,
                        economicActivityService2,
                        applicationFormDao,
                        mercantileFormService,
                        properties,
                        affiliateRepository2,
                        domesticServiceIndependentServiceReportService,
                        filedService2,
                        healthRepository,
                        pensionRepository,
                        alfrescoService,
                        genericWebClient2);
        FiledServiceImpl filedService3 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        CollectProperties properties2 = new CollectProperties();
        WebClient webClient4 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService2 =
                new AlfrescoServiceImpl(webClient4, new CollectProperties());
        EmailService emailService2 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository2 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository3 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService2 =
                new CertificateServiceImpl(
                        certificateRepository2,
                        mock(CodeValidCertificationService.class),
                        affiliateRepository3,
                        mock(GenericWebClient.class),
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        mock(EconomicActivityServiceImpl.class),
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        mock(FiledService.class),
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder4 = mock(WebClient.class);
        GenericWebClient webClient5 =
                new GenericWebClient(webClientBuilder4, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao3 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService2 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository2 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository2 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository2 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository3 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository2 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService3 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao2 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService2 = mock(MercantileFormService.class);
        CollectProperties properties3 = new CollectProperties();
        AffiliateRepository affiliateRepository4 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService2 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService4 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository2 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository2 = mock(FundPensionRepository.class);
        WebClient webClient6 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService3 =
                new AlfrescoServiceImpl(webClient6, new CollectProperties());
        WebClient webClientBuilder5 = mock(WebClient.class);
        GenericWebClient genericWebClient3 =
                new GenericWebClient(webClientBuilder5, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails2 =
                new SendEmailImpl(
                        emailService2,
                        certificateService2,
                        webClient5,
                        arlInformationDao3,
                        formIndependentService2,
                        dangerRepository2,
                        departmentRepository2,
                        municipalityRepository2,
                        economicActivityRepository3,
                        genderRepository2,
                        economicActivityService3,
                        applicationFormDao2,
                        mercantileFormService2,
                        properties3,
                        affiliateRepository4,
                        domesticServiceIndependentServiceReportService2,
                        filedService4,
                        healthRepository2,
                        pensionRepository2,
                        alfrescoService3,
                        genericWebClient3);
        MainOfficeRepository repository = mock(MainOfficeRepository.class);
        WorkCenterServiceImpl workCenterService =
                new WorkCenterServiceImpl(mock(WorkCenterRepository.class));
        AffiliateRepository affiliateRepository5 = mock(AffiliateRepository.class);
        IUserPreRegisterRepository iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        IEconomicActivityRepository economicActivityRepository4 =
                mock(IEconomicActivityRepository.class);
        AffiliateMercantileRepository affiliateMercantileRepository2 =
                mock(AffiliateMercantileRepository.class);
        AffiliationDependentRepository affiliationDependentRepository =
                mock(AffiliationDependentRepository.class);
        IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository =
                mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        ArlInformationDao arlInformationDao4 = mock(ArlInformationDao.class);
        BusTokenService busTokenService = new BusTokenService(mock(WebClient.class));
        InsertHeadquartersClient insertHeadquartersClient =
                new InsertHeadquartersClient(busTokenService, new AffiliationProperties());
        BusTokenService busTokenService2 = new BusTokenService(mock(WebClient.class));
        UpdateHeadquartersClient updateHeadquartersClient =
                new UpdateHeadquartersClient(busTokenService2, new AffiliationProperties());
        BusTokenService busTokenService3 = new BusTokenService(mock(WebClient.class));
        InsertWorkCenterClient insertWorkCenterClient =
                new InsertWorkCenterClient(busTokenService3, new AffiliationProperties());

        MainOfficeServiceImpl mainOfficeService =
                new MainOfficeServiceImpl(
                        sendEmails2,
                        repository,
                        workCenterService,
                        affiliateRepository5,
                        iUserPreRegisterRepository,
                        economicActivityRepository4,
                        affiliateMercantileRepository2,
                        affiliationDependentRepository,
                        domesticServiceIndependentRepository,
                        arlInformationDao4,
                        insertHeadquartersClient,
                        updateHeadquartersClient,
                        insertWorkCenterClient,
                        mock(MunicipalityRepository.class),
                        affiliationDetailRepository
                );

        AffiliateRepository iAffiliateRepository = mock(AffiliateRepository.class);
        MainOfficeRepository mainOfficeRepository = mock(MainOfficeRepository.class);
        WebClient webClientBuilder6 = mock(WebClient.class);
        GenericWebClient webClient7 =
                new GenericWebClient(webClientBuilder6, new CollectProperties(), mock(Builder.class));
        IUserPreRegisterRepository iUserPreRegisterRepository2 = mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakServiceImpl = new KeycloakServiceImpl(new KeyCloakProvider());
        UserStatusUpdateService userStatusUpdateService =
                new UserStatusUpdateService(
                        mock(IUserPreRegisterRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        mock(AffiliateRepository.class),
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(AffiliateMercantileRepository.class));
        RestTemplate restTemplate = mock(RestTemplate.class);
        AffiliationProperties affiliationProperties = new AffiliationProperties();
        GenderRepository genderRepository3 = mock(GenderRepository.class);
        SystemParamRepository paramRepository = mock(SystemParamRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        CollectProperties properties4 = new CollectProperties();
        EmailService emailService3 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository3 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository6 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService3 =
                new CertificateServiceImpl(
                        certificateRepository3,
                        mock(CodeValidCertificationService.class),
                        affiliateRepository6,
                        mock(GenericWebClient.class),
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        mock(EconomicActivityServiceImpl.class),
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        mock(FiledService.class),
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder7 = mock(WebClient.class);
        GenericWebClient webClient8 =
                new GenericWebClient(webClientBuilder7, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao5 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService3 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository3 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository3 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository3 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository5 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository4 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService4 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao3 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService3 = mock(MercantileFormService.class);
        CollectProperties properties5 = new CollectProperties();
        AffiliateRepository affiliateRepository7 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService3 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService5 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository3 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository3 = mock(FundPensionRepository.class);
        WebClient webClient9 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService4 =
                new AlfrescoServiceImpl(webClient9, new CollectProperties());
        WebClient webClientBuilder8 = mock(WebClient.class);
        GenericWebClient genericWebClient4 =
                new GenericWebClient(webClientBuilder8, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails3 =
                new SendEmailImpl(
                        emailService3,
                        certificateService3,
                        webClient8,
                        arlInformationDao5,
                        formIndependentService3,
                        dangerRepository3,
                        departmentRepository3,
                        municipalityRepository3,
                        economicActivityRepository5,
                        genderRepository4,
                        economicActivityService4,
                        applicationFormDao3,
                        mercantileFormService3,
                        properties5,
                        affiliateRepository7,
                        domesticServiceIndependentServiceReportService3,
                        filedService5,
                        healthRepository3,
                        pensionRepository3,
                        alfrescoService4,
                        genericWebClient4);
        OtpCodeRepository otpCodeRepository = mock(OtpCodeRepository.class);
        OtpImpl otpService =
                new OtpImpl(
                        otpCodeRepository,
                        new EmailService(new JavaMailSenderImpl()),
                        mock(IUserPreRegisterRepository.class),
                        mock(ArlInformationDao.class),
                        mock(AffiliationDependentRepository.class));
        KeycloakServiceImpl keycloakService = new KeycloakServiceImpl(new KeyCloakProvider());
        AffiliateMercantileRepository affiliateMercantileRepository3 =
                mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepository =
                mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepository8 = mock(AffiliateRepository.class);
        UpdatePreRegisterMapperImpl updatePreRegisterMapper = new UpdatePreRegisterMapperImpl();
        UserMapper userMapper = mock(UserMapper.class);
        ArlRepository arlRepository = mock(ArlRepository.class);
        IdentityCardConsultationService identityCardConsultationService =
                new IdentityCardConsultationService(null);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        RegistraduriaUnifiedService registraduriaUnifiedService =
                new RegistraduriaUnifiedService(
                        identityCardConsultationService, registraduriaKeycloakTokenService);

        UserPreRegisterServiceImpl iUserRegisterService =
                new UserPreRegisterServiceImpl(
                        webClient7,
                        iUserPreRegisterRepository2,
                        keycloakServiceImpl,
                        userStatusUpdateService,
                        restTemplate,
                        affiliationProperties,
                        genderRepository3,
                        paramRepository,
                        request,
                        properties4,
                        sendEmails3,
                        otpService,
                        keycloakService,
                        affiliateMercantileRepository3,
                        affiliationDetailRepository,
                        affiliateRepository8,
                        updatePreRegisterMapper,
                        userMapper,
                        arlRepository,
                        registraduriaUnifiedService);
        WorkCenterRepository workCenterRepository = mock(WorkCenterRepository.class);
        IDataDocumentRepository dataDocumentRepository = mock(IDataDocumentRepository.class);
        IUserPreRegisterRepository iUserPreRegisterRepository3 = mock(IUserPreRegisterRepository.class);
        IEconomicActivityRepository economicActivityRepository6 =
                mock(IEconomicActivityRepository.class);
        IAffiliationCancellationTimerRepository timerRepository =
                mock(IAffiliationCancellationTimerRepository.class);
        EmailService emailService4 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository4 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository9 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService4 =
                new CertificateServiceImpl(
                        certificateRepository4,
                        mock(CodeValidCertificationService.class),
                        affiliateRepository9,
                        mock(GenericWebClient.class),
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        mock(EconomicActivityServiceImpl.class),
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        mock(FiledService.class),
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder9 = mock(WebClient.class);
        GenericWebClient webClient10 =
                new GenericWebClient(webClientBuilder9, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao6 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService4 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository4 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository4 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository4 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository7 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository5 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService5 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao4 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService4 = mock(MercantileFormService.class);
        CollectProperties properties6 = new CollectProperties();
        AffiliateRepository affiliateRepository10 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService4 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService6 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository4 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository4 = mock(FundPensionRepository.class);
        WebClient webClient11 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService5 =
                new AlfrescoServiceImpl(webClient11, new CollectProperties());
        WebClient webClientBuilder10 = mock(WebClient.class);
        GenericWebClient genericWebClient5 =
                new GenericWebClient(webClientBuilder10, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails4 =
                new SendEmailImpl(
                        emailService4,
                        certificateService4,
                        webClient10,
                        arlInformationDao6,
                        formIndependentService4,
                        dangerRepository4,
                        departmentRepository4,
                        municipalityRepository4,
                        economicActivityRepository7,
                        genderRepository5,
                        economicActivityService5,
                        applicationFormDao4,
                        mercantileFormService4,
                        properties6,
                        affiliateRepository10,
                        domesticServiceIndependentServiceReportService4,
                        filedService6,
                        healthRepository4,
                        pensionRepository4,
                        alfrescoService5,
                        genericWebClient5);
        WebClient webClient12 = mock(WebClient.class);
        DailyServiceImpl dailyService =
                new DailyServiceImpl(
                        webClient12,
                        new CollectProperties(),
                        mock(DateInterviewWebRepository.class),
                        mock(IUserPreRegisterRepository.class));

        ScheduleInterviewWebServiceImpl scheduleInterviewWebService =
                new ScheduleInterviewWebServiceImpl(
                        sendEmails4,
                        dailyService,
                        new CollectProperties(),
                        mock(WebClient.class),
                        mock(AffiliateRepository.class),
                        mock(DateInterviewWebRepository.class),
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class));
        TypeEmployerDocumentServiceImpl typeEmployerDocumentService =
                new TypeEmployerDocumentServiceImpl(
                        mock(TypeEmployerRepository.class),
                        mock(SubTypeEmployerRepository.class),
                        mock(DocumentRepository.class),
                        mock(LegalStatusRepository.class));
        ObservationsAffiliationServiceImpl observationsAffiliationService =
                new ObservationsAffiliationServiceImpl(
                        mock(IUserPreRegisterRepository.class), mock(ObservationsAffiliationRepository.class));
        DepartmentRepository departmentRepository5 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository5 = mock(MunicipalityRepository.class);
        IdentificationLegalNatureServiceImpl identificationLegalNatureService =
                new IdentificationLegalNatureServiceImpl(mock(IdentificationLegalNatureRepository.class));
        MessageErrorAge messageError = new MessageErrorAge(mock(ArlInformationDao.class));
        DocumentNameStandardizationService documentNameStandardizationService =
                mock(DocumentNameStandardizationService.class);
        BusTokenService genericWebClient6 = new BusTokenService(mock(WebClient.class));
        ConsultEmployerClient consultEmployerClient =
                new ConsultEmployerClient(genericWebClient6, new AffiliationProperties());
        ArlInformationDao arlInformationDao7 = mock(ArlInformationDao.class);
        ArlRepository arlRepository2 = mock(ArlRepository.class);
        PolicyDaoImpl policyDao = new PolicyDaoImpl(mock(PolicyRepository.class));
        PolicyServiceImpl policyService = new PolicyServiceImpl(policyDao);
        RegistraduriaClient registraduriaClient = new RegistraduriaClient(mock(WebClient.class), null);
        IdentityCardConsultationService identityCardConsultationService2 =
                new IdentityCardConsultationService(registraduriaClient);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService2 =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        RegistraduriaUnifiedService registraduriaUnifiedService2 =
                new RegistraduriaUnifiedService(
                        identityCardConsultationService2, registraduriaKeycloakTokenService2);
        BusTokenService busTokenService4 = new BusTokenService(mock(WebClient.class));
        AffiliationProperties properties7 = new AffiliationProperties();
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

        SatConsultTransferableEmployerClient satConsultTransferableEmployerClient =
                new SatConsultTransferableEmployerClient(busTokenService4, properties7, objectMapper);
        WebClient webClient13 = mock(WebClient.class);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService3 =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        ConfecamarasClient confecamarasClient =
                new ConfecamarasClient(webClient13, registraduriaKeycloakTokenService3);
        ConfecamarasConsultationService confecamarasConsultationService =
                new ConfecamarasConsultationService(confecamarasClient);
        WebClient webClientBuilder11 = mock(WebClient.class);
        GenericWebClient genericWebClient7 =
                new GenericWebClient(webClientBuilder11, new CollectProperties(), mock(Builder.class));

        AffiliationEmployerActivitiesMercantileServiceImpl
                affiliationEmployerActivitiesMercantileServiceImpl =
                new AffiliationEmployerActivitiesMercantileServiceImpl(
                        webClient,
                        sendEmails,
                        filedService3,
                        properties2,
                        alfrescoService2,
                        mainOfficeService,
                        iAffiliateRepository,
                        mainOfficeRepository,
                        iUserRegisterService,
                        workCenterRepository,
                        dataDocumentRepository,
                        iUserPreRegisterRepository3,
                        economicActivityRepository6,
                        timerRepository,
                        scheduleInterviewWebService,
                        typeEmployerDocumentService,
                        affiliateMercantileRepository,
                        observationsAffiliationService,
                        departmentRepository5,
                        municipalityRepository5,
                        identificationLegalNatureService,
                        messageError,
                        documentNameStandardizationService,
                        consultEmployerClient,
                        arlInformationDao7,
                        arlRepository2,
                        policyService,
                        registraduriaUnifiedService2,
                        satConsultTransferableEmployerClient,
                        confecamarasConsultationService,
                        genericWebClient7);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(affiliateMercantileRepository).findAll(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>Given {@link Arl#Arl()} Administrator is {@code Administrator}.
     *   <li>Then throw {@link SatError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); given Arl() Administrator is 'Administrator'; then throw SatError")
    
    void testValidationsStepOne_givenArlAdministratorIsAdministrator_thenThrowSatError() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult);

        Arl arl = new Arl();
        arl.setAdministrator("Administrator");
        arl.setCodeARL("Code ARL");
        arl.setId(1L);
        Optional<Arl> ofResult2 = Optional.of(arl);
        when(arlRepository.findByCodeARL(Mockito.<String>any())).thenReturn(ofResult2);
        when(satConsultTransferableEmployerClient.consult(Mockito.<TransferableEmployerRequest>any()))
                .thenReturn(
                        TransferableEmployerResponse.builder()
                                .arlAfiliacion("Arl Afiliacion")
                                .causal(1)
                                .codigoArl("Codigo Arl")
                                .consecutivoNITEmpleador("Consecutivo NITEmpleador")
                                .empresaTrasladable("Empresa Trasladable")
                                .build());

        // Act and Assert
        assertThrows(
                SatError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(satConsultTransferableEmployerClient).consult(isA(TransferableEmployerRequest.class));
        verify(affiliateMercantileRepository)
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "PT", "8");
        verify(arlRepository).findByCodeARL("Arl Afiliacion");
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#findAll(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName("Test validationsStepOne(String, String, String); then calls findAll(Specification)")
    
    void testValidationsStepOne_thenCallsFindAll() {
        // Arrange
        AffiliateMercantileRepository affiliateMercantileRepository =
                mock(AffiliateMercantileRepository.class);
        when(affiliateMercantileRepository.findAll(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(new ArrayList<>());
        WebClient webClient = mock(WebClient.class);
        EmailService emailService = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository = mock(CertificateRepository.class);
        CodeValidCertificationServiceImpl codeValidCertificationService =
                new CodeValidCertificationServiceImpl(
                        mock(ICodeValidCertificateRepository.class), mock(IUserPreRegisterRepository.class));
        AffiliateRepository affiliateRepository = mock(AffiliateRepository.class);
        WebClient webClientBuilder = mock(WebClient.class);
        GenericWebClient genericWebClient =
                new GenericWebClient(webClientBuilder, new CollectProperties(), mock(Builder.class));
        CertificateServiceHelper certificateServiceHelper = new CertificateServiceHelper();
        IQrRepository iQrRepository = mock(IQrRepository.class);
        ICardRepository iCardRepository = mock(ICardRepository.class);
        ArlInformationDao arlInformationDao = mock(ArlInformationDao.class);
        EconomicActivityServiceImpl economicActivityService =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository =
                mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        IEconomicActivityRepository economicActivityRepository =
                mock(IEconomicActivityRepository.class);
        RequestCorrectionRepository requestCorrectionRepository =
                mock(RequestCorrectionRepository.class);
        RequestCollectionRequestRepository requestCollectionRepository =
                mock(RequestCollectionRequestRepository.class);
        FiledServiceImpl filedService =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));

        CertificateServiceImpl certificateService =
                new CertificateServiceImpl(
                        certificateRepository,
                        codeValidCertificationService,
                        affiliateRepository,
                        genericWebClient,
                        certificateServiceHelper,
                        iQrRepository,
                        iCardRepository,
                        arlInformationDao,
                        economicActivityService,
                        affiliationRepository,
                        economicActivityRepository,
                        requestCorrectionRepository,
                        requestCollectionRepository,
                        filedService,
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder2 = mock(WebClient.class);
        GenericWebClient webClient2 =
                new GenericWebClient(webClientBuilder2, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao2 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository = mock(DangerRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository2 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService2 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService = mock(MercantileFormService.class);
        CollectProperties properties = new CollectProperties();
        AffiliateRepository affiliateRepository2 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService2 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository = mock(FundPensionRepository.class);
        WebClient webClient3 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService =
                new AlfrescoServiceImpl(webClient3, new CollectProperties());
        WebClient webClientBuilder3 = mock(WebClient.class);
        GenericWebClient genericWebClient2 =
                new GenericWebClient(webClientBuilder3, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails =
                new SendEmailImpl(
                        emailService,
                        certificateService,
                        webClient2,
                        arlInformationDao2,
                        formIndependentService,
                        dangerRepository,
                        departmentRepository,
                        municipalityRepository,
                        economicActivityRepository2,
                        genderRepository,
                        economicActivityService2,
                        applicationFormDao,
                        mercantileFormService,
                        properties,
                        affiliateRepository2,
                        domesticServiceIndependentServiceReportService,
                        filedService2,
                        healthRepository,
                        pensionRepository,
                        alfrescoService,
                        genericWebClient2);
        FiledServiceImpl filedService3 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        CollectProperties properties2 = new CollectProperties();
        WebClient webClient4 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService2 =
                new AlfrescoServiceImpl(webClient4, new CollectProperties());
        EmailService emailService2 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository2 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository3 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService2 =
                new CertificateServiceImpl(
                        certificateRepository2,
                        null,
                        affiliateRepository3,
                        null,
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        null,
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        null,
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder4 = mock(WebClient.class);
        GenericWebClient webClient5 =
                new GenericWebClient(webClientBuilder4, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao3 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService2 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository2 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository2 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository2 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository3 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository2 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService3 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao2 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService2 = mock(MercantileFormService.class);
        CollectProperties properties3 = new CollectProperties();
        AffiliateRepository affiliateRepository4 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService2 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService4 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository2 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository2 = mock(FundPensionRepository.class);
        WebClient webClient6 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService3 =
                new AlfrescoServiceImpl(webClient6, new CollectProperties());
        WebClient webClientBuilder5 = mock(WebClient.class);
        GenericWebClient genericWebClient3 =
                new GenericWebClient(webClientBuilder5, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails2 =
                new SendEmailImpl(
                        emailService2,
                        certificateService2,
                        webClient5,
                        arlInformationDao3,
                        formIndependentService2,
                        dangerRepository2,
                        departmentRepository2,
                        municipalityRepository2,
                        economicActivityRepository3,
                        genderRepository2,
                        economicActivityService3,
                        applicationFormDao2,
                        mercantileFormService2,
                        properties3,
                        affiliateRepository4,
                        domesticServiceIndependentServiceReportService2,
                        filedService4,
                        healthRepository2,
                        pensionRepository2,
                        alfrescoService3,
                        genericWebClient3);
        MainOfficeRepository repository = mock(MainOfficeRepository.class);
        WorkCenterServiceImpl workCenterService =
                new WorkCenterServiceImpl(mock(WorkCenterRepository.class));
        AffiliateRepository affiliateRepository5 = mock(AffiliateRepository.class);
        IUserPreRegisterRepository iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        IEconomicActivityRepository economicActivityRepository4 =
                mock(IEconomicActivityRepository.class);
        AffiliateMercantileRepository affiliateMercantileRepository2 =
                mock(AffiliateMercantileRepository.class);
        AffiliationDependentRepository affiliationDependentRepository =
                mock(AffiliationDependentRepository.class);
        IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository =
                mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        ArlInformationDao arlInformationDao4 = mock(ArlInformationDao.class);
        BusTokenService busTokenService = new BusTokenService(mock(WebClient.class));
        InsertHeadquartersClient insertHeadquartersClient =
                new InsertHeadquartersClient(busTokenService, new AffiliationProperties());
        BusTokenService busTokenService2 = new BusTokenService(mock(WebClient.class));
        UpdateHeadquartersClient updateHeadquartersClient =
                new UpdateHeadquartersClient(busTokenService2, new AffiliationProperties());
        BusTokenService busTokenService3 = new BusTokenService(mock(WebClient.class));
        InsertWorkCenterClient insertWorkCenterClient =
                new InsertWorkCenterClient(busTokenService3, new AffiliationProperties());

        MainOfficeServiceImpl mainOfficeService =
                new MainOfficeServiceImpl(
                        sendEmails2,
                        repository,
                        workCenterService,
                        affiliateRepository5,
                        iUserPreRegisterRepository,
                        economicActivityRepository4,
                        affiliateMercantileRepository2,
                        affiliationDependentRepository,
                        domesticServiceIndependentRepository,
                        arlInformationDao4,
                        insertHeadquartersClient,
                        updateHeadquartersClient,
                        insertWorkCenterClient,
                        mock(MunicipalityRepository.class),
                        affiliationDetailRepository
                );

        AffiliateRepository iAffiliateRepository = mock(AffiliateRepository.class);
        MainOfficeRepository mainOfficeRepository = mock(MainOfficeRepository.class);
        WebClient webClientBuilder6 = mock(WebClient.class);
        GenericWebClient webClient7 =
                new GenericWebClient(webClientBuilder6, new CollectProperties(), mock(Builder.class));
        IUserPreRegisterRepository iUserPreRegisterRepository2 = mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakServiceImpl = new KeycloakServiceImpl(new KeyCloakProvider());
        UserStatusUpdateService userStatusUpdateService =
                new UserStatusUpdateService(
                        mock(IUserPreRegisterRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        mock(AffiliateRepository.class),
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(AffiliateMercantileRepository.class));
        RestTemplate restTemplate = mock(RestTemplate.class);
        AffiliationProperties affiliationProperties = new AffiliationProperties();
        GenderRepository genderRepository3 = mock(GenderRepository.class);
        SystemParamRepository paramRepository = mock(SystemParamRepository.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        CollectProperties properties4 = new CollectProperties();
        EmailService emailService3 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository3 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository6 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService3 =
                new CertificateServiceImpl(
                        certificateRepository3,
                        null,
                        affiliateRepository6,
                        null,
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        null,
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        null,
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder7 = mock(WebClient.class);
        GenericWebClient webClient8 =
                new GenericWebClient(webClientBuilder7, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao5 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService3 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository3 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository3 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository3 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository5 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository4 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService4 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao3 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService3 = mock(MercantileFormService.class);
        CollectProperties properties5 = new CollectProperties();
        AffiliateRepository affiliateRepository7 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService3 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService5 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository3 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository3 = mock(FundPensionRepository.class);
        WebClient webClient9 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService4 =
                new AlfrescoServiceImpl(webClient9, new CollectProperties());
        WebClient webClientBuilder8 = mock(WebClient.class);
        GenericWebClient genericWebClient4 =
                new GenericWebClient(webClientBuilder8, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails3 =
                new SendEmailImpl(
                        emailService3,
                        certificateService3,
                        webClient8,
                        arlInformationDao5,
                        formIndependentService3,
                        dangerRepository3,
                        departmentRepository3,
                        municipalityRepository3,
                        economicActivityRepository5,
                        genderRepository4,
                        economicActivityService4,
                        applicationFormDao3,
                        mercantileFormService3,
                        properties5,
                        affiliateRepository7,
                        domesticServiceIndependentServiceReportService3,
                        filedService5,
                        healthRepository3,
                        pensionRepository3,
                        alfrescoService4,
                        genericWebClient4);
        OtpCodeRepository otpCodeRepository = mock(OtpCodeRepository.class);
        OtpImpl otpService =
                new OtpImpl(
                        otpCodeRepository,
                        new EmailService(new JavaMailSenderImpl()),
                        mock(IUserPreRegisterRepository.class),
                        mock(ArlInformationDao.class),
                        mock(AffiliationDependentRepository.class));
        KeycloakServiceImpl keycloakService = new KeycloakServiceImpl(new KeyCloakProvider());
        AffiliateMercantileRepository affiliateMercantileRepository3 =
                mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepository =
                mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepository8 = mock(AffiliateRepository.class);
        UpdatePreRegisterMapperImpl updatePreRegisterMapper = new UpdatePreRegisterMapperImpl();
        UserMapper userMapper = mock(UserMapper.class);
        ArlRepository arlRepository = mock(ArlRepository.class);
        IdentityCardConsultationService identityCardConsultationService =
                new IdentityCardConsultationService(null);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        RegistraduriaUnifiedService registraduriaUnifiedService =
                new RegistraduriaUnifiedService(
                        identityCardConsultationService, registraduriaKeycloakTokenService);

        UserPreRegisterServiceImpl iUserRegisterService =
                new UserPreRegisterServiceImpl(
                        webClient7,
                        iUserPreRegisterRepository2,
                        keycloakServiceImpl,
                        userStatusUpdateService,
                        restTemplate,
                        affiliationProperties,
                        genderRepository3,
                        paramRepository,
                        request,
                        properties4,
                        sendEmails3,
                        otpService,
                        keycloakService,
                        affiliateMercantileRepository3,
                        affiliationDetailRepository,
                        affiliateRepository8,
                        updatePreRegisterMapper,
                        userMapper,
                        arlRepository,
                        registraduriaUnifiedService);
        WorkCenterRepository workCenterRepository = mock(WorkCenterRepository.class);
        IDataDocumentRepository dataDocumentRepository = mock(IDataDocumentRepository.class);
        IUserPreRegisterRepository iUserPreRegisterRepository3 = mock(IUserPreRegisterRepository.class);
        IEconomicActivityRepository economicActivityRepository6 =
                mock(IEconomicActivityRepository.class);
        IAffiliationCancellationTimerRepository timerRepository =
                mock(IAffiliationCancellationTimerRepository.class);
        EmailService emailService4 = new EmailService(new JavaMailSenderImpl());
        CertificateRepository certificateRepository4 = mock(CertificateRepository.class);
        AffiliateRepository affiliateRepository9 = mock(AffiliateRepository.class);

        CertificateServiceImpl certificateService4 =
                new CertificateServiceImpl(
                        certificateRepository4,
                        null,
                        affiliateRepository9,
                        null,
                        new CertificateServiceHelper(),
                        mock(IQrRepository.class),
                        mock(ICardRepository.class),
                        mock(ArlInformationDao.class),
                        null,
                        mock(IAffiliationEmployerDomesticServiceIndependentRepository.class),
                        mock(IEconomicActivityRepository.class),
                        mock(RequestCorrectionRepository.class),
                        mock(RequestCollectionRequestRepository.class),
                        null,
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDependentRepository.class),
                        mock(OccupationRepository.class),
                        mock(ICertificateAffiliateRepository.class),
                        mock(OccupationDecree1563Repository.class));
        WebClient webClientBuilder9 = mock(WebClient.class);
        GenericWebClient webClient10 =
                new GenericWebClient(webClientBuilder9, new CollectProperties(), mock(Builder.class));
        ArlInformationDao arlInformationDao6 = mock(ArlInformationDao.class);
        IndividualIndependentAffiliationService formIndependentService4 =
                mock(IndividualIndependentAffiliationService.class);
        DangerRepository dangerRepository4 = mock(DangerRepository.class);
        DepartmentRepository departmentRepository4 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository4 = mock(MunicipalityRepository.class);
        IEconomicActivityRepository economicActivityRepository7 =
                mock(IEconomicActivityRepository.class);
        GenderRepository genderRepository5 = mock(GenderRepository.class);
        EconomicActivityServiceImpl economicActivityService5 =
                new EconomicActivityServiceImpl(
                        mock(IEconomicActivityRepository.class),
                        mock(AffiliateRepository.class),
                        mock(AffiliateMercantileRepository.class),
                        mock(AffiliationDetailRepository.class));
        ApplicationFormDaoImpl applicationFormDao4 =
                new ApplicationFormDaoImpl(mock(ApplicationFormRepository.class));
        MercantileFormService mercantileFormService4 = mock(MercantileFormService.class);
        CollectProperties properties6 = new CollectProperties();
        AffiliateRepository affiliateRepository10 = mock(AffiliateRepository.class);
        DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService4 =
                mock(DomesticServiceIndependentServiceReportService.class);
        FiledServiceImpl filedService6 =
                new FiledServiceImpl(mock(ConsecutiveService.class), mock(AffiliateRepository.class));
        HealthPromotingEntityRepository healthRepository4 = mock(HealthPromotingEntityRepository.class);
        FundPensionRepository pensionRepository4 = mock(FundPensionRepository.class);
        WebClient webClient11 = mock(WebClient.class);
        AlfrescoServiceImpl alfrescoService5 =
                new AlfrescoServiceImpl(webClient11, new CollectProperties());
        WebClient webClientBuilder10 = mock(WebClient.class);
        GenericWebClient genericWebClient5 =
                new GenericWebClient(webClientBuilder10, new CollectProperties(), mock(Builder.class));

        SendEmailImpl sendEmails4 =
                new SendEmailImpl(
                        emailService4,
                        certificateService4,
                        webClient10,
                        arlInformationDao6,
                        formIndependentService4,
                        dangerRepository4,
                        departmentRepository4,
                        municipalityRepository4,
                        economicActivityRepository7,
                        genderRepository5,
                        economicActivityService5,
                        applicationFormDao4,
                        mercantileFormService4,
                        properties6,
                        affiliateRepository10,
                        domesticServiceIndependentServiceReportService4,
                        filedService6,
                        healthRepository4,
                        pensionRepository4,
                        alfrescoService5,
                        genericWebClient5);
        WebClient webClient12 = mock(WebClient.class);
        DailyServiceImpl dailyService =
                new DailyServiceImpl(
                        webClient12,
                        new CollectProperties(),
                        mock(DateInterviewWebRepository.class),
                        mock(IUserPreRegisterRepository.class));

        ScheduleInterviewWebServiceImpl scheduleInterviewWebService =
                new ScheduleInterviewWebServiceImpl(
                        sendEmails4,
                        dailyService,
                        new CollectProperties(),
                        mock(WebClient.class),
                        mock(AffiliateRepository.class),
                        mock(DateInterviewWebRepository.class),
                        mock(IUserPreRegisterRepository.class),
                        mock(AffiliateMercantileRepository.class));
        TypeEmployerDocumentServiceImpl typeEmployerDocumentService =
                new TypeEmployerDocumentServiceImpl(
                        mock(TypeEmployerRepository.class),
                        mock(SubTypeEmployerRepository.class),
                        mock(DocumentRepository.class),
                        mock(LegalStatusRepository.class));
        ObservationsAffiliationServiceImpl observationsAffiliationService =
                new ObservationsAffiliationServiceImpl(
                        mock(IUserPreRegisterRepository.class), mock(ObservationsAffiliationRepository.class));
        DepartmentRepository departmentRepository5 = mock(DepartmentRepository.class);
        MunicipalityRepository municipalityRepository5 = mock(MunicipalityRepository.class);
        IdentificationLegalNatureServiceImpl identificationLegalNatureService =
                new IdentificationLegalNatureServiceImpl(mock(IdentificationLegalNatureRepository.class));
        MessageErrorAge messageError = new MessageErrorAge(mock(ArlInformationDao.class));
        DocumentNameStandardizationService documentNameStandardizationService =
                mock(DocumentNameStandardizationService.class);
        BusTokenService genericWebClient6 = new BusTokenService(mock(WebClient.class));
        ConsultEmployerClient consultEmployerClient =
                new ConsultEmployerClient(genericWebClient6, new AffiliationProperties());
        ArlInformationDao arlInformationDao7 = mock(ArlInformationDao.class);
        ArlRepository arlRepository2 = mock(ArlRepository.class);
        PolicyDaoImpl policyDao = new PolicyDaoImpl(mock(PolicyRepository.class));
        PolicyServiceImpl policyService = new PolicyServiceImpl(policyDao);
        RegistraduriaClient registraduriaClient = new RegistraduriaClient(mock(WebClient.class), null);
        IdentityCardConsultationService identityCardConsultationService2 =
                new IdentityCardConsultationService(registraduriaClient);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService2 =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        RegistraduriaUnifiedService registraduriaUnifiedService2 =
                new RegistraduriaUnifiedService(
                        identityCardConsultationService2, registraduriaKeycloakTokenService2);
        BusTokenService busTokenService4 = new BusTokenService(mock(WebClient.class));
        AffiliationProperties properties7 = new AffiliationProperties();
        JsonMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

        SatConsultTransferableEmployerClient satConsultTransferableEmployerClient =
                new SatConsultTransferableEmployerClient(busTokenService4, properties7, objectMapper);
        WebClient webClient13 = mock(WebClient.class);
        RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService3 =
                new RegistraduriaKeycloakTokenService(mock(Builder.class));

        ConfecamarasClient confecamarasClient =
                new ConfecamarasClient(webClient13, registraduriaKeycloakTokenService3);
        ConfecamarasConsultationService confecamarasConsultationService =
                new ConfecamarasConsultationService(confecamarasClient);
        WebClient webClientBuilder11 = mock(WebClient.class);
        GenericWebClient genericWebClient7 =
                new GenericWebClient(webClientBuilder11, new CollectProperties(), mock(Builder.class));

        AffiliationEmployerActivitiesMercantileServiceImpl
                affiliationEmployerActivitiesMercantileServiceImpl =
                new AffiliationEmployerActivitiesMercantileServiceImpl(
                        webClient,
                        sendEmails,
                        filedService3,
                        properties2,
                        alfrescoService2,
                        mainOfficeService,
                        iAffiliateRepository,
                        mainOfficeRepository,
                        iUserRegisterService,
                        workCenterRepository,
                        dataDocumentRepository,
                        iUserPreRegisterRepository3,
                        economicActivityRepository6,
                        timerRepository,
                        scheduleInterviewWebService,
                        typeEmployerDocumentService,
                        affiliateMercantileRepository,
                        observationsAffiliationService,
                        departmentRepository5,
                        municipalityRepository5,
                        identificationLegalNatureService,
                        messageError,
                        documentNameStandardizationService,
                        consultEmployerClient,
                        arlInformationDao7,
                        arlRepository2,
                        policyService,
                        registraduriaUnifiedService2,
                        satConsultTransferableEmployerClient,
                        confecamarasConsultationService,
                        genericWebClient7);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "PT", ""));
        verify(affiliateMercantileRepository).findAll(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code 42}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when '42'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_when42_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne(
                                "42", "Type Document", "Dv"));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code CC}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'CC'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_whenCc_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "CC", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code CD}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'CD'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_whenCd_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "CD", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code CE}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'CE'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_whenCe_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "CE", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code NI}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'NI'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_whenNi_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "NI", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code null}.
     *   <li>Then throw {@link IllegalArgumentException}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'null'; then throw AffiliationError")
    
    void testValidationsStepOne_whenNull_thenThrowIllegalArgumentException() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne(null, "TI", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code SC}.
     *   <li>Then throw {@link ErrorDocumentConditions}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'SC'; then throw ErrorDocumentConditions")
    
    void testValidationsStepOne_whenSc_thenThrowErrorDocumentConditions() {
        // Arrange, Act and Assert
        assertThrows(
                ErrorDocumentConditions.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "SC", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String,
     * String, String)}.
     *
     * <ul>
     *   <li>When {@code TI}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#validationsStepOne(String, String, String)}
     */
    @Test
    @DisplayName(
            "Test validationsStepOne(String, String, String); when 'TI'; then throw AffiliationError")
    
    void testValidationsStepOne_whenTi_thenThrowAffiliationError() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("8", "TI", ""));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}.
     *
     * <ul>
     *   <li>When {@link DataBasicCompanyDTO#DataBasicCompanyDTO()}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepOne(DataBasicCompanyDTO)}
     */
    @Test
    @DisplayName(
            "Test stepOne(DataBasicCompanyDTO); when DataBasicCompanyDTO(); then throw AffiliationError")
    
    void testStepOne_whenDataBasicCompanyDTO_thenThrowAffiliationError() {
        // Arrange
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(
                Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepOne(new DataBasicCompanyDTO()));
        verify(iUserPreRegisterRepository).findByIdentificationTypeAndIdentification(null, null);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#findUser(AffiliateMercantile)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#findUser(AffiliateMercantile)}
     */
    @Test
    @DisplayName("Test findUser(AffiliateMercantile)")
    
    void testFindUser() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.findUser(affiliateMercantile));
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#findUser(AffiliateMercantile)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#findUser(AffiliateMercantile)}
     */
    @Test
    @DisplayName("Test findUser(AffiliateMercantile)")
    
    void testFindUser2() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any()))
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.findUser(affiliateMercantile));
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}
     */
    @Test
    @DisplayName("Test stepTwo(DataLegalRepresentativeDTO, boolean)")
    
    void testStepTwo() {
        // Arrange
        when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(
                                new DataLegalRepresentativeDTO(), true));
        verify(affiliateMercantileRepository).findById(isNull());
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}.
     *
     * <ul>
     *   <li>Then calls {@link IUserPreRegisterRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}
     */
    @Test
    @DisplayName(
            "Test stepTwo(DataLegalRepresentativeDTO, boolean); then calls findOne(Specification)")
    
    void testStepTwo_thenCallsFindOne() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(
                                new DataLegalRepresentativeDTO(), true));
        verify(iUserPreRegisterRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findById(isNull());
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}.
     *
     * <ul>
     *   <li>Then calls {@link DataLegalRepresentativeDTO#getIdAffiliationMercantile()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}
     */
    @Test
    @DisplayName(
            "Test stepTwo(DataLegalRepresentativeDTO, boolean); then calls getIdAffiliationMercantile()")
    
    void testStepTwo_thenCallsGetIdAffiliationMercantile() {
        // Arrange
        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = mock(DataLegalRepresentativeDTO.class);
        when(dataLegalRepresentativeDTO.getIdAffiliationMercantile())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(
                                dataLegalRepresentativeDTO, true));
        verify(dataLegalRepresentativeDTO).getIdAffiliationMercantile();
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}.
     *
     * <ul>
     *   <li>When {@code null}.
     *   <li>Then throw {@link IllegalArgumentException}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stepTwo(DataLegalRepresentativeDTO,
     * boolean)}
     */
    @Test
    @DisplayName(
            "Test stepTwo(DataLegalRepresentativeDTO, boolean); when 'null'; then throw NullPointerException")
    
    void testStepTwo_whenNull_thenThrowIllegalArgumentException() {
        // Arrange, Act and Assert
        assertThrows(
                NullPointerException.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepTwo(null, false));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree() {
        // Arrange
        when(collectProperties.getFolderIdMercantile())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree2() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree3() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree4() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree5() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree6() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree7() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation())
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List)")
    
    void testStepThree8() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(genericWebClient.getChildrenNode(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} StageManagement is {@code
     *       entrevista web}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given AffiliateMercantile() StageManagement is 'entrevista web'")
    
    void testStepThree_givenAffiliateMercantileStageManagementIsEntrevistaWeb() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("entrevista web");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} StageManagement is {@code firma}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given AffiliateMercantile() StageManagement is 'firma'")
    
    void testStepThree_givenAffiliateMercantileStageManagementIsFirma() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("firma");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} StageManagement is {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given AffiliateMercantile() StageManagement is 'null'")
    
    void testStepThree_givenAffiliateMercantileStageManagementIsNull() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement(null);
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link AlfrescoResponseDTO#AlfrescoResponseDTO()} List is {@link
     *       ListDTO#ListDTO()}.
     *   <li>Then calls {@link GenericWebClient#getChildrenNode(String)}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given AlfrescoResponseDTO() List is ListDTO(); then calls getChildrenNode(String)")
    
    void testStepThree_givenAlfrescoResponseDTOListIsListDTO_thenCallsGetChildrenNode() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(new ListDTO());
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given builder file {@code File} idDocument one name {@code Name} build.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given builder file 'File' idDocument one name 'Name' build")
    
    void testStepThree_givenBuilderFileFileIdDocumentOneNameNameBuild() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        ArrayList<DocumentRequestDTO> files = new ArrayList<>();
        files.add(DocumentRequestDTO.builder().file("File").idDocument(1L).name("Name").build());

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L, files));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link CollectProperties}.
     *   <li>When {@link ArrayList#ArrayList()}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given CollectProperties; when ArrayList(); then throw AffiliationError")
    
    void testStepThree_givenCollectProperties_whenArrayList_thenThrowAffiliationError() {
        // Arrange
        when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link DocumentRequested#DocumentRequested()} Id is one.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List); given DocumentRequested() Id is one")
    
    void testStepThree_givenDocumentRequestedIdIsOne() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        DocumentRequested documentRequested = new DocumentRequested();
        documentRequested.setId(1L);
        documentRequested.setName("firma");
        documentRequested.setRequested(true);
        documentRequested.setSubTypeEmployers(new HashSet<>());

        ArrayList<DocumentRequested> documentRequestedList = new ArrayList<>();
        documentRequestedList.add(documentRequested);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(documentRequestedList);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link FiledService}.
     *   <li>When {@link ArrayList#ArrayList()}.
     *   <li>Then calls {@link GenericWebClient#getChildrenNode(String)}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given FiledService; when ArrayList(); then calls getChildrenNode(String)")
    
    void testStepThree_givenFiledService_whenArrayList_thenCallsGetChildrenNode() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(genericWebClient.getChildrenNode(Mockito.<String>any()))
                .thenReturn(new AlfrescoResponseDTO());
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link FiledService}.
     *   <li>When {@link ArrayList#ArrayList()}.
     *   <li>Then calls {@link GenericWebClient#getChildrenNode(String)}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given FiledService; when ArrayList(); then calls getChildrenNode(String)")
    
    void testStepThree_givenFiledService_whenArrayList_thenCallsGetChildrenNode2() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(new EntryDTO());
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Given {@link FiledService}.
     *   <li>When {@link ArrayList#ArrayList()}.
     *   <li>Then calls {@link GenericWebClient#getChildrenNode(String)}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName(
            "Test stepThree(Long, Long, Long, List); given FiledService; when ArrayList(); then calls getChildrenNode(String)")
    
    void testStepThree_givenFiledService_whenArrayList_thenCallsGetChildrenNode3() {
        // Arrange
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);
        ListDTO list = new ListDTO(pagination, new ArrayList<>());
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stepThree(
                                1L, 1L, 1L, new ArrayList<>()));
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long, Long, Long,
     * List)}.
     *
     * <ul>
     *   <li>Then return Address is {@code 42 Main St}.
     * </ul>
     *
     * <p>Method under test: {@link AffiliationEmployerActivitiesMercantileServiceImpl#stepThree(Long,
     * Long, Long, List)}
     */
    @Test
    @DisplayName("Test stepThree(Long, Long, Long, List); then return Address is '42 Main St'")
    
    void testStepThree_thenReturnAddressIs42MainSt() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        when(affiliateRepository.save(Mockito.<Affiliate>any())).thenReturn(affiliate);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);

        AffiliateMercantile affiliateMercantile2 = new AffiliateMercantile();
        affiliateMercantile2.setAddress("42 Main St");
        affiliateMercantile2.setAddressContactCompany("42 Main St");
        affiliateMercantile2.setAddressIsEqualsContactCompany(true);
        affiliateMercantile2.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile2.setAffiliationCancelled(true);
        affiliateMercantile2.setAffiliationStatus("Affiliation Status");
        affiliateMercantile2.setAfp(1L);
        affiliateMercantile2.setArl("Arl");
        affiliateMercantile2.setBusinessName("Business Name");
        affiliateMercantile2.setCityMunicipality(1L);
        affiliateMercantile2.setCityMunicipalityContactCompany(1L);
        affiliateMercantile2.setCodeContributorType("Code Contributor Type");
        affiliateMercantile2.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile2.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRequest("2020-03-01");
        affiliateMercantile2.setDecentralizedConsecutive(1L);
        affiliateMercantile2.setDepartment(1L);
        affiliateMercantile2.setDepartmentContactCompany(1L);
        affiliateMercantile2.setDigitVerificationDV(1);
        affiliateMercantile2.setEconomicActivity(new ArrayList<>());
        affiliateMercantile2.setEmail("jane.doe@example.org");
        affiliateMercantile2.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile2.setEps(1L);
        affiliateMercantile2.setFiledNumber("42");
        affiliateMercantile2.setId(1L);
        affiliateMercantile2.setIdAffiliate(1L);
        affiliateMercantile2.setIdCardinalPoint2(1L);
        affiliateMercantile2.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile2.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile2.setIdCardinalPointMainStreet(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdCity(1L);
        affiliateMercantile2.setIdCityContactCompany(1L);
        affiliateMercantile2.setIdCityLegalRepresentative(1L);
        affiliateMercantile2.setIdDepartment(1L);
        affiliateMercantile2.setIdDepartmentContactCompany(1L);
        affiliateMercantile2.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile2.setIdEmployerSize(1L);
        affiliateMercantile2.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile2.setIdHorizontalProperty1(1L);
        affiliateMercantile2.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty2(1L);
        affiliateMercantile2.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty3(1L);
        affiliateMercantile2.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty4(1L);
        affiliateMercantile2.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdLetter1MainStreet(1L);
        affiliateMercantile2.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetter2MainStreet(1L);
        affiliateMercantile2.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetterSecondStreet(1L);
        affiliateMercantile2.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile2.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdMainHeadquarter(1L);
        affiliateMercantile2.setIdMainStreet(1L);
        affiliateMercantile2.setIdMainStreetContactCompany(1L);
        affiliateMercantile2.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum1SecondStreet(1L);
        affiliateMercantile2.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum2SecondStreet(1L);
        affiliateMercantile2.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdNumberMainStreet(1L);
        affiliateMercantile2.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile2.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdProcedureType(1L);
        affiliateMercantile2.setIdSubTypeEmployer(1L);
        affiliateMercantile2.setIdTypeEmployer(1L);
        affiliateMercantile2.setIdUserPreRegister(1L);
        affiliateMercantile2.setIsBis(true);
        affiliateMercantile2.setIsBisContactCompany(true);
        affiliateMercantile2.setIsBisLegalRepresentative(true);
        affiliateMercantile2.setIsVip(true);
        affiliateMercantile2.setLegalStatus("Legal Status");
        affiliateMercantile2.setNumberDocumentPersonResponsible("42");
        affiliateMercantile2.setNumberIdentification("42");
        affiliateMercantile2.setNumberWorkers(1L);
        affiliateMercantile2.setPhoneOne("6625550144");
        affiliateMercantile2.setPhoneOneContactCompany("6625550144");
        affiliateMercantile2.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile2.setPhoneTwo("6625550144");
        affiliateMercantile2.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile2.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile2.setRealNumberWorkers(1L);
        affiliateMercantile2.setStageManagement("Stage Management");
        affiliateMercantile2.setStatusDocument(true);
        affiliateMercantile2.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile2.setTypeAffiliation("Type Affiliation");
        affiliateMercantile2.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile2.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile2.setTypePerson("Type Person");
        affiliateMercantile2.setZoneLocationEmployer("Zone Location Employer");
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenReturn(affiliateMercantile2);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act
        AffiliateMercantileDTO actualStepThreeResult =
                affiliationEmployerActivitiesMercantileServiceImpl.stepThree(1L, 1L, 1L, new ArrayList<>());

        // Assert
        verify(filedService).getNextFiledNumberAffiliation();
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(collectProperties).getFolderIdMercantile();
        verify(genericWebClient).folderExistsByName("Folder Id Mercantile", "42");
        verify(genericWebClient).getChildrenNode("42");
        verify(affiliateMercantileRepository).findById(1L);
        verify(affiliateRepository).save(isA(Affiliate.class));
        verify(affiliateMercantileRepository, atLeast(1)).save(isA(AffiliateMercantile.class));
        assertEquals("42 Main St", actualStepThreeResult.getAddress());
        assertEquals("42 Main St", actualStepThreeResult.getAddressContactCompany());
        assertEquals("42", actualStepThreeResult.getFiledNumber());
        assertEquals("42", actualStepThreeResult.getIdFolderAlfresco());
        assertEquals("42", actualStepThreeResult.getNumberIdentification());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOne());
        assertEquals("6625550144", actualStepThreeResult.getPhoneOneContactCompany());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwo());
        assertEquals("6625550144", actualStepThreeResult.getPhoneTwoContactCompany());
        assertEquals("Business Name", actualStepThreeResult.getBusinessName());
        assertEquals("Stage Management", actualStepThreeResult.getStageManagement());
        assertEquals(
                "Type Document Identification", actualStepThreeResult.getTypeDocumentIdentification());
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
        assertEquals(1L, actualStepThreeResult.getAfp().longValue());
        assertEquals(1L, actualStepThreeResult.getCityMunicipality().longValue());
        assertEquals(1L, actualStepThreeResult.getCityMunicipalityContactCompany().longValue());
        assertEquals(1L, actualStepThreeResult.getDepartment().longValue());
        assertEquals(1L, actualStepThreeResult.getDepartmentContactCompany().longValue());
        assertEquals(1L, actualStepThreeResult.getEps().longValue());
        assertEquals(1L, actualStepThreeResult.getId().longValue());
        assertEquals(1L, actualStepThreeResult.getIdUserPreRegister().longValue());
        assertEquals(1L, actualStepThreeResult.getNumberWorkers().longValue());
        assertTrue(actualStepThreeResult.getAffiliationCancelled());
        assertTrue(actualStepThreeResult.getStatusDocument());
        assertTrue(actualStepThreeResult.getDocuments().isEmpty());
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName("Test stateDocuments(List, Long)")
    
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
        when(iDataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(iDataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(
                                listDocumentsDTOS, 1L));
        verify(iDataDocumentRepository).findById(1L);
        verify(iDataDocumentRepository).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName("Test stateDocuments(List, Long)")
    
    void testStateDocuments2() {
        // Arrange
        when(iDataDocumentRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(
                                listDocumentsDTOS, 1L));
        verify(iDataDocumentRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <ul>
     *   <li>Given {@link IDataDocumentRepository} {@link IDataDocumentRepository#save(Object)} return
     *       {@link DataDocumentAffiliate#DataDocumentAffiliate()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName(
            "Test stateDocuments(List, Long); given IDataDocumentRepository save(Object) return DataDocumentAffiliate()")
    
    void testStateDocuments_givenIDataDocumentRepositorySaveReturnDataDocumentAffiliate() {
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
        when(iDataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any()))
                .thenReturn(dataDocumentAffiliate2);
        when(iDataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L);

        // Assert
        verify(iDataDocumentRepository, atLeast(1)).findById(1L);
        verify(iDataDocumentRepository, atLeast(1)).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <ul>
     *   <li>Given {@link IDataDocumentRepository} {@link IDataDocumentRepository#save(Object)} return
     *       {@link DataDocumentAffiliate#DataDocumentAffiliate()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName(
            "Test stateDocuments(List, Long); given IDataDocumentRepository save(Object) return DataDocumentAffiliate()")
    
    void testStateDocuments_givenIDataDocumentRepositorySaveReturnDataDocumentAffiliate2() {
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
        when(iDataDocumentRepository.save(Mockito.<DataDocumentAffiliate>any()))
                .thenReturn(dataDocumentAffiliate2);
        when(iDataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(listDocumentsDTOS, 1L);

        // Assert
        verify(iDataDocumentRepository).findById(1L);
        verify(iDataDocumentRepository).save(isA(DataDocumentAffiliate.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <ul>
     *   <li>Given {@link IDataDocumentRepository}.
     *   <li>When {@link ArrayList#ArrayList()}.
     *   <li>Then does not throw.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName(
            "Test stateDocuments(List, Long); given IDataDocumentRepository; when ArrayList(); then does not throw")
    
    void testStateDocuments_givenIDataDocumentRepository_whenArrayList_thenDoesNotThrow() {
        // Arrange, Act and Assert
        assertDoesNotThrow(
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(
                                new ArrayList<>(), 1L));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}.
     *
     * <ul>
     *   <li>Then throw {@link ErrorFindDocumentsAlfresco}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateDocuments(List, Long)}
     */
    @Test
    @DisplayName("Test stateDocuments(List, Long); then throw ErrorFindDocumentsAlfresco")
    
    void testStateDocuments_thenThrowErrorFindDocumentsAlfresco() {
        // Arrange
        Optional<DataDocumentAffiliate> emptyResult = Optional.empty();
        when(iDataDocumentRepository.findById(Mockito.<Long>any())).thenReturn(emptyResult);

        ArrayList<DocumentsDTO> listDocumentsDTOS = new ArrayList<>();
        listDocumentsDTOS.add(
                DocumentsDTO.builder()
                        .dateTime("2020-03-01")
                        .id(1L)
                        .idDocument("Id Document")
                        .name("Name")
                        .reject(true)
                        .revised(true)
                        .build());

        // Act and Assert
        assertThrows(
                ErrorFindDocumentsAlfresco.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(
                                listDocumentsDTOS, 1L));
        verify(iDataDocumentRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateAffiliation(AffiliateMercantile,
     * StateAffiliation)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#stateAffiliation(AffiliateMercantile,
     * StateAffiliation)}
     */
    @Test
    @DisplayName("Test stateAffiliation(AffiliateMercantile, StateAffiliation)")
    
    void testStateAffiliation() {
        // Arrange
        when(iUserPreRegisterRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.stateAffiliation(
                                affiliateMercantile, new StateAffiliation()));
        verify(iUserPreRegisterRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}.
     *
     * <ul>
     *   <li>Then return Empty.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    @DisplayName("Test scheduleInterviewWeb(DateInterviewWebDTO); then return Empty")
    
    void testScheduleInterviewWeb_thenReturnEmpty() {
        // Arrange
        when(scheduleInterviewWebService.createScheduleInterviewWeb(Mockito.<DateInterviewWebDTO>any()))
                .thenReturn(new HashMap<>());

        // Act
        Map<String, Object> actualScheduleInterviewWebResult =
                affiliationEmployerActivitiesMercantileServiceImpl.scheduleInterviewWeb(
                        new DateInterviewWebDTO());

        // Assert
        verify(scheduleInterviewWebService).createScheduleInterviewWeb(isA(DateInterviewWebDTO.class));
        assertTrue(actualScheduleInterviewWebResult.isEmpty());
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#scheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    @DisplayName("Test scheduleInterviewWeb(DateInterviewWebDTO); then throw AffiliationError")
    
    void testScheduleInterviewWeb_thenThrowAffiliationError() {
        // Arrange
        when(scheduleInterviewWebService.createScheduleInterviewWeb(Mockito.<DateInterviewWebDTO>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.scheduleInterviewWeb(
                                new DateInterviewWebDTO()));
        verify(scheduleInterviewWebService).createScheduleInterviewWeb(isA(DateInterviewWebDTO.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}.
     *
     * <ul>
     *   <li>Given {@link IUserPreRegisterRepository}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}
     */
    @Test
    @DisplayName(
            "Test interviewWeb(StateAffiliation); given IUserPreRegisterRepository; then throw AffiliationError")
    
    void testInterviewWeb_givenIUserPreRegisterRepository_thenThrowAffiliationError() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.interviewWeb(
                                new StateAffiliation()));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}.
     *
     * <ul>
     *   <li>Then calls {@link IUserPreRegisterRepository#findById(Long)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#interviewWeb(StateAffiliation)}
     */
    @Test
    @DisplayName("Test interviewWeb(StateAffiliation); then calls findById(Long)")
    
    void testInterviewWeb_thenCallsFindById() {
        // Arrange
        when(iUserPreRegisterRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.interviewWeb(
                                new StateAffiliation()));
        verify(iUserPreRegisterRepository).findById(1L);
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List)")
    
    void testRegularizationDocuments() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List)")
    
    void testRegularizationDocuments2() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List)")
    
    void testRegularizationDocuments3() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List)")
    
    void testRegularizationDocuments4() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(iDataDocumentRepository.findByIdAffiliate(Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(iDataDocumentRepository).findByIdAffiliate(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} StageManagement is {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName(
            "Test regularizationDocuments(String, Long, Long, List); given AffiliateMercantile() StageManagement is 'null'")
    
    void testRegularizationDocuments_givenAffiliateMercantileStageManagementIsNull() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement(null);
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateRepository}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName(
            "Test regularizationDocuments(String, Long, Long, List); given AffiliateRepository; then throw AffiliationError")
    
    void testRegularizationDocuments_givenAffiliateRepository_thenThrowAffiliationError() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Given {@link DocumentRequested#DocumentRequested()} Id is one.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName(
            "Test regularizationDocuments(String, Long, Long, List); given DocumentRequested() Id is one")
    
    void testRegularizationDocuments_givenDocumentRequestedIdIsOne() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        DocumentRequested documentRequested = new DocumentRequested();
        documentRequested.setId(1L);
        documentRequested.setName("regularizacion");
        documentRequested.setRequested(true);
        documentRequested.setSubTypeEmployers(new HashSet<>());

        ArrayList<DocumentRequested> documentRequestedList = new ArrayList<>();
        documentRequestedList.add(documentRequested);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(documentRequestedList);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Then calls {@link IDataDocumentRepository#delete(Object)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List); then calls delete(Object)")
    
    void testRegularizationDocuments_thenCallsDelete() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        DataDocumentAffiliate dataDocumentAffiliate = new DataDocumentAffiliate();
        dataDocumentAffiliate.setDateUpload(LocalDate.of(1970, 1, 1).atStartOfDay());
        dataDocumentAffiliate.setId(1L);
        dataDocumentAffiliate.setIdAffiliate(1L);
        dataDocumentAffiliate.setIdAlfresco("regularizacion");
        dataDocumentAffiliate.setName("regularizacion");
        dataDocumentAffiliate.setRevised(true);
        dataDocumentAffiliate.setState(true);

        ArrayList<DataDocumentAffiliate> dataDocumentAffiliateList = new ArrayList<>();
        dataDocumentAffiliateList.add(dataDocumentAffiliate);
        doThrow(new ErrorDocumentConditions("Not all who wander are lost"))
                .when(iDataDocumentRepository)
                .delete(Mockito.<DataDocumentAffiliate>any());
        when(iDataDocumentRepository.findByIdAffiliate(Mockito.<Long>any()))
                .thenReturn(dataDocumentAffiliateList);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(iDataDocumentRepository).findByIdAffiliate(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(iDataDocumentRepository).delete(isA(DataDocumentAffiliate.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#save(Object)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List); then calls save(Object)")
    
    void testRegularizationDocuments_thenCallsSave() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(iDataDocumentRepository.findByIdAffiliate(Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(iDataDocumentRepository).findByIdAffiliate(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String,
     * Long, Long, List)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateRepository#save(Object)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#regularizationDocuments(String, Long, Long,
     * List)}
     */
    @Test
    @DisplayName("Test regularizationDocuments(String, Long, Long, List); then calls save(Object)")
    
    void testRegularizationDocuments_thenCallsSave2() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.save(Mockito.<Affiliate>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(iDataDocumentRepository.findByIdAffiliate(Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(
                Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("regularizacion");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);

        AffiliateMercantile affiliateMercantile2 = new AffiliateMercantile();
        affiliateMercantile2.setAddress("42 Main St");
        affiliateMercantile2.setAddressContactCompany("42 Main St");
        affiliateMercantile2.setAddressIsEqualsContactCompany(true);
        affiliateMercantile2.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile2.setAffiliationCancelled(true);
        affiliateMercantile2.setAffiliationStatus("Affiliation Status");
        affiliateMercantile2.setAfp(1L);
        affiliateMercantile2.setArl("Arl");
        affiliateMercantile2.setBusinessName("Business Name");
        affiliateMercantile2.setCityMunicipality(1L);
        affiliateMercantile2.setCityMunicipalityContactCompany(1L);
        affiliateMercantile2.setCodeContributorType("Code Contributor Type");
        affiliateMercantile2.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile2.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRequest("2020-03-01");
        affiliateMercantile2.setDecentralizedConsecutive(1L);
        affiliateMercantile2.setDepartment(1L);
        affiliateMercantile2.setDepartmentContactCompany(1L);
        affiliateMercantile2.setDigitVerificationDV(1);
        affiliateMercantile2.setEconomicActivity(new ArrayList<>());
        affiliateMercantile2.setEmail("jane.doe@example.org");
        affiliateMercantile2.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile2.setEps(1L);
        affiliateMercantile2.setFiledNumber("42");
        affiliateMercantile2.setId(1L);
        affiliateMercantile2.setIdAffiliate(1L);
        affiliateMercantile2.setIdCardinalPoint2(1L);
        affiliateMercantile2.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile2.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile2.setIdCardinalPointMainStreet(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdCity(1L);
        affiliateMercantile2.setIdCityContactCompany(1L);
        affiliateMercantile2.setIdCityLegalRepresentative(1L);
        affiliateMercantile2.setIdDepartment(1L);
        affiliateMercantile2.setIdDepartmentContactCompany(1L);
        affiliateMercantile2.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile2.setIdEmployerSize(1L);
        affiliateMercantile2.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile2.setIdHorizontalProperty1(1L);
        affiliateMercantile2.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty2(1L);
        affiliateMercantile2.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty3(1L);
        affiliateMercantile2.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty4(1L);
        affiliateMercantile2.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdLetter1MainStreet(1L);
        affiliateMercantile2.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetter2MainStreet(1L);
        affiliateMercantile2.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetterSecondStreet(1L);
        affiliateMercantile2.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile2.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdMainHeadquarter(1L);
        affiliateMercantile2.setIdMainStreet(1L);
        affiliateMercantile2.setIdMainStreetContactCompany(1L);
        affiliateMercantile2.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum1SecondStreet(1L);
        affiliateMercantile2.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum2SecondStreet(1L);
        affiliateMercantile2.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdNumberMainStreet(1L);
        affiliateMercantile2.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile2.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdProcedureType(1L);
        affiliateMercantile2.setIdSubTypeEmployer(1L);
        affiliateMercantile2.setIdTypeEmployer(1L);
        affiliateMercantile2.setIdUserPreRegister(1L);
        affiliateMercantile2.setIsBis(true);
        affiliateMercantile2.setIsBisContactCompany(true);
        affiliateMercantile2.setIsBisLegalRepresentative(true);
        affiliateMercantile2.setIsVip(true);
        affiliateMercantile2.setLegalStatus("Legal Status");
        affiliateMercantile2.setNumberDocumentPersonResponsible("42");
        affiliateMercantile2.setNumberIdentification("42");
        affiliateMercantile2.setNumberWorkers(1L);
        affiliateMercantile2.setPhoneOne("6625550144");
        affiliateMercantile2.setPhoneOneContactCompany("6625550144");
        affiliateMercantile2.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile2.setPhoneTwo("6625550144");
        affiliateMercantile2.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile2.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile2.setRealNumberWorkers(1L);
        affiliateMercantile2.setStageManagement("Stage Management");
        affiliateMercantile2.setStatusDocument(true);
        affiliateMercantile2.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile2.setTypeAffiliation("Type Affiliation");
        affiliateMercantile2.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile2.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile2.setTypePerson("Type Person");
        affiliateMercantile2.setZoneLocationEmployer("Zone Location Employer");
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenReturn(affiliateMercantile2);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.regularizationDocuments(
                                "42", 1L, 1L, new ArrayList<>()));
        verify(typeEmployerDocumentService).findByIdSubTypeEmployerListDocumentRequested(1L);
        verify(iDataDocumentRepository).findByIdAffiliate(1L);
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateRepository).save(isA(Affiliate.class));
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}
     */
    @Test
    @DisplayName("Test updateDataInterviewWeb(InterviewWebDTO)")
    
    void testUpdateDataInterviewWeb() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.updateDataInterviewWeb(
                                new InterviewWebDTO()));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#findById(Object)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}
     */
    @Test
    @DisplayName("Test updateDataInterviewWeb(InterviewWebDTO); then calls findById(Object)")
    
    void testUpdateDataInterviewWeb_thenCallsFindById() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        InterviewWebDTO interviewWebDTO = new InterviewWebDTO();
        interviewWebDTO.setDataLegalRepresentativeDTO(new DataLegalRepresentativeDTO());

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.updateDataInterviewWeb(
                                interviewWebDTO));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findById(isNull());
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}.
     *
     * <ul>
     *   <li>Then calls {@link InterviewWebDTO#getDataBasicCompanyDTO()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}
     */
    @Test
    @DisplayName("Test updateDataInterviewWeb(InterviewWebDTO); then calls getDataBasicCompanyDTO()")
    
    void testUpdateDataInterviewWeb_thenCallsGetDataBasicCompanyDTO() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        InterviewWebDTO interviewWebDTO = mock(InterviewWebDTO.class);
        when(interviewWebDTO.getDataBasicCompanyDTO())
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(interviewWebDTO.hasNullDataBasicCompanyDTO()).thenReturn(true);
        when(interviewWebDTO.getFiledNumber()).thenReturn("42");
        doNothing()
                .when(interviewWebDTO)
                .setDataLegalRepresentativeDTO(Mockito.<DataLegalRepresentativeDTO>any());
        interviewWebDTO.setDataLegalRepresentativeDTO(new DataLegalRepresentativeDTO());

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.updateDataInterviewWeb(
                                interviewWebDTO));
        verify(interviewWebDTO).getDataBasicCompanyDTO();
        verify(interviewWebDTO).getFiledNumber();
        verify(interviewWebDTO).hasNullDataBasicCompanyDTO();
        verify(interviewWebDTO).setDataLegalRepresentativeDTO(isA(DataLegalRepresentativeDTO.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}.
     *
     * <ul>
     *   <li>When {@link InterviewWebDTO#InterviewWebDTO()}.
     *   <li>Then return {@code OK}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#updateDataInterviewWeb(InterviewWebDTO)}
     */
    @Test
    @DisplayName(
            "Test updateDataInterviewWeb(InterviewWebDTO); when InterviewWebDTO(); then return 'OK'")
    
    void testUpdateDataInterviewWeb_whenInterviewWebDTO_thenReturnOk() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        // Act
        String actualUpdateDataInterviewWebResult =
                affiliationEmployerActivitiesMercantileServiceImpl.updateDataInterviewWeb(
                        new InterviewWebDTO());

        // Assert
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        assertEquals("OK", actualUpdateDataInterviewWebResult);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeAffiliation(String)")
    
    void testChangeAffiliation() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.changeAffiliation("42"));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeAffiliation(String)")
    
    void testChangeAffiliation2() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.changeAffiliation("42"));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#changeAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeAffiliation(String)")
    
    void testChangeAffiliation3() {
        // Arrange
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);

        AffiliateMercantile affiliateMercantile2 = new AffiliateMercantile();
        affiliateMercantile2.setAddress("42 Main St");
        affiliateMercantile2.setAddressContactCompany("42 Main St");
        affiliateMercantile2.setAddressIsEqualsContactCompany(true);
        affiliateMercantile2.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile2.setAffiliationCancelled(true);
        affiliateMercantile2.setAffiliationStatus("Affiliation Status");
        affiliateMercantile2.setAfp(1L);
        affiliateMercantile2.setArl("Arl");
        affiliateMercantile2.setBusinessName("Business Name");
        affiliateMercantile2.setCityMunicipality(1L);
        affiliateMercantile2.setCityMunicipalityContactCompany(1L);
        affiliateMercantile2.setCodeContributorType("Code Contributor Type");
        affiliateMercantile2.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile2.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile2.setDateRequest("2020-03-01");
        affiliateMercantile2.setDecentralizedConsecutive(1L);
        affiliateMercantile2.setDepartment(1L);
        affiliateMercantile2.setDepartmentContactCompany(1L);
        affiliateMercantile2.setDigitVerificationDV(1);
        affiliateMercantile2.setEconomicActivity(new ArrayList<>());
        affiliateMercantile2.setEmail("jane.doe@example.org");
        affiliateMercantile2.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile2.setEps(1L);
        affiliateMercantile2.setFiledNumber("42");
        affiliateMercantile2.setId(1L);
        affiliateMercantile2.setIdAffiliate(1L);
        affiliateMercantile2.setIdCardinalPoint2(1L);
        affiliateMercantile2.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile2.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile2.setIdCardinalPointMainStreet(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile2.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdCity(1L);
        affiliateMercantile2.setIdCityContactCompany(1L);
        affiliateMercantile2.setIdCityLegalRepresentative(1L);
        affiliateMercantile2.setIdDepartment(1L);
        affiliateMercantile2.setIdDepartmentContactCompany(1L);
        affiliateMercantile2.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile2.setIdEmployerSize(1L);
        affiliateMercantile2.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile2.setIdHorizontalProperty1(1L);
        affiliateMercantile2.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty2(1L);
        affiliateMercantile2.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty3(1L);
        affiliateMercantile2.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdHorizontalProperty4(1L);
        affiliateMercantile2.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdLetter1MainStreet(1L);
        affiliateMercantile2.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetter2MainStreet(1L);
        affiliateMercantile2.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile2.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdLetterSecondStreet(1L);
        affiliateMercantile2.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile2.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdMainHeadquarter(1L);
        affiliateMercantile2.setIdMainStreet(1L);
        affiliateMercantile2.setIdMainStreetContactCompany(1L);
        affiliateMercantile2.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum1SecondStreet(1L);
        affiliateMercantile2.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNum2SecondStreet(1L);
        affiliateMercantile2.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile2.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile2.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile2.setIdNumberMainStreet(1L);
        affiliateMercantile2.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile2.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile2.setIdProcedureType(1L);
        affiliateMercantile2.setIdSubTypeEmployer(1L);
        affiliateMercantile2.setIdTypeEmployer(1L);
        affiliateMercantile2.setIdUserPreRegister(1L);
        affiliateMercantile2.setIsBis(true);
        affiliateMercantile2.setIsBisContactCompany(true);
        affiliateMercantile2.setIsBisLegalRepresentative(true);
        affiliateMercantile2.setIsVip(true);
        affiliateMercantile2.setLegalStatus("Legal Status");
        affiliateMercantile2.setNumberDocumentPersonResponsible("42");
        affiliateMercantile2.setNumberIdentification("42");
        affiliateMercantile2.setNumberWorkers(1L);
        affiliateMercantile2.setPhoneOne("6625550144");
        affiliateMercantile2.setPhoneOneContactCompany("6625550144");
        affiliateMercantile2.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile2.setPhoneTwo("6625550144");
        affiliateMercantile2.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile2.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile2.setRealNumberWorkers(1L);
        affiliateMercantile2.setStageManagement("Stage Management");
        affiliateMercantile2.setStatusDocument(true);
        affiliateMercantile2.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile2.setTypeAffiliation("Type Affiliation");
        affiliateMercantile2.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile2.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile2.setTypePerson("Type Person");
        affiliateMercantile2.setZoneLocationEmployer("Zone Location Employer");
        when(affiliateMercantileRepository.save(Mockito.<AffiliateMercantile>any()))
                .thenReturn(affiliateMercantile2);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.changeAffiliation("42");

        // Assert
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).save(isA(AffiliateMercantile.class));
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String,
     * String, DataBasicCompanyDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String, String,
     * DataBasicCompanyDTO)}
     */
    @Test
    @DisplayName("Test consultWSConfecamaras(String, String, DataBasicCompanyDTO)")
    
    void testConsultWSConfecamaras() {
        // Arrange
        Mockito.<RequestHeadersUriSpec<?>>when(webClient.get())
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        when(collectProperties.getUrlTransversal()).thenReturn("https://example.org/example");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.consultWSConfecamaras(
                                "42", "Dv", new DataBasicCompanyDTO()));
        verify(collectProperties).getUrlTransversal();
        verify(webClient).get();
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String,
     * String, DataBasicCompanyDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String, String,
     * DataBasicCompanyDTO)}
     */
    @Test
    @DisplayName("Test consultWSConfecamaras(String, String, DataBasicCompanyDTO)")
    
    void testConsultWSConfecamaras2() {
        // Arrange
        Mockito.<RequestHeadersUriSpec<?>>when(webClient.get())
                .thenThrow(new ErrorDocumentConditions("Not all who wander are lost"));
        when(collectProperties.getUrlTransversal()).thenReturn("https://example.org/example");

        // Act
        affiliationEmployerActivitiesMercantileServiceImpl.consultWSConfecamaras(
                "42", "Dv", new DataBasicCompanyDTO());

        // Assert
        verify(collectProperties).getUrlTransversal();
        verify(webClient).get();
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String,
     * String, DataBasicCompanyDTO)}.
     *
     * <ul>
     *   <li>Given {@link WebClient}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#consultWSConfecamaras(String, String,
     * DataBasicCompanyDTO)}
     */
    @Test
    @DisplayName(
            "Test consultWSConfecamaras(String, String, DataBasicCompanyDTO); given WebClient; then throw AffiliationError")
    
    void testConsultWSConfecamaras_givenWebClient_thenThrowAffiliationError() {
        // Arrange
        when(collectProperties.getUrlTransversal())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.consultWSConfecamaras(
                                "42", "Dv", new DataBasicCompanyDTO()));
        verify(collectProperties).getUrlTransversal();
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String,
     * Integer)}.
     *
     * <ul>
     *   <li>Given {@link ConsultEmployerClient} {@link ConsultEmployerClient#consult(String, String,
     *       Integer)} return just {@link ArrayList#ArrayList()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String, Integer)}
     */
    @Test
    @DisplayName(
            "Test affiliateBUs(String, String, Integer); given ConsultEmployerClient consult(String, String, Integer) return just ArrayList()")
    
    void testAffiliateBUs_givenConsultEmployerClientConsultReturnJustArrayList() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        Mono<List<EmployerResponse>> justResult = Mono.just(new ArrayList<>());
        when(consultEmployerClient.consult(
                Mockito.<String>any(), Mockito.<String>any(), Mockito.<Integer>any()))
                .thenReturn(justResult);

        // Act
        Boolean actualAffiliateBUsResult =
                affiliationEmployerActivitiesMercantileServiceImpl.affiliateBUs(
                        "Tipo Doc", "Id Empresa", 1);

        // Assert
        verify(filedService).getNextFiledNumberAffiliation();
        verify(consultEmployerClient).consult("Tipo Doc", "Id Empresa", 1);
        assertTrue(actualAffiliateBUsResult);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String,
     * Integer)}.
     *
     * <ul>
     *   <li>Given {@link FiledService}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String, Integer)}
     */
    @Test
    @DisplayName(
            "Test affiliateBUs(String, String, Integer); given FiledService; then throw AffiliationError")
    
    void testAffiliateBUs_givenFiledService_thenThrowAffiliationError() {
        // Arrange
        when(consultEmployerClient.consult(
                Mockito.<String>any(), Mockito.<String>any(), Mockito.<Integer>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerActivitiesMercantileServiceImpl.affiliateBUs(
                                "Tipo Doc", "Id Empresa", 1));
        verify(consultEmployerClient).consult("Tipo Doc", "Id Empresa", 1);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String,
     * Integer)}.
     *
     * <ul>
     *   <li>Given {@link Mono} {@link Mono#subscribe(Consumer)} return {@link Disposable}.
     *   <li>Then calls {@link Mono#subscribe(Consumer)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String, Integer)}
     */
    @Test
    @DisplayName(
            "Test affiliateBUs(String, String, Integer); given Mono subscribe(Consumer) return Disposable; then calls subscribe(Consumer)")
    
    void testAffiliateBUs_givenMonoSubscribeReturnDisposable_thenCallsSubscribe() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");

        Mono<List<EmployerResponse>> mono = mock(Mono.class);
        when(mono.subscribe(Mockito.<Consumer<List<EmployerResponse>>>any()))
                .thenReturn(mock(Disposable.class));
        when(consultEmployerClient.consult(
                Mockito.<String>any(), Mockito.<String>any(), Mockito.<Integer>any()))
                .thenReturn(mono);

        // Act
        Boolean actualAffiliateBUsResult =
                affiliationEmployerActivitiesMercantileServiceImpl.affiliateBUs(
                        "Tipo Doc", "Id Empresa", 1);

        // Assert
        verify(filedService).getNextFiledNumberAffiliation();
        verify(consultEmployerClient).consult("Tipo Doc", "Id Empresa", 1);
        verify(mono).subscribe(isA(Consumer.class));
        assertTrue(actualAffiliateBUsResult);
    }

    /**
     * Test {@link AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String,
     * Integer)}.
     *
     * <ul>
     *   <li>Then calls {@link
     *       IUserPreRegisterRepository#findByIdentificationTypeAndIdentification(String, String)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerActivitiesMercantileServiceImpl#affiliateBUs(String, String, Integer)}
     */
    @Test
    @DisplayName(
            "Test affiliateBUs(String, String, Integer); then calls findByIdentificationTypeAndIdentification(String, String)")
    
    void testAffiliateBUs_thenCallsFindByIdentificationTypeAndIdentification() {
        // Arrange
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(
                Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        EmployerResponse employerResponse = new EmployerResponse();
        employerResponse.setDireccionEmpresa("Direccion Empresa");
        employerResponse.setEmailEmpresa("jane.doe@example.org");
        employerResponse.setEstado(1);
        employerResponse.setFechaAfiliacionEfectiva("Fecha Afiliacion Efectiva");
        employerResponse.setFechaRetiroInactivacion("Fecha Retiro Inactivacion");
        employerResponse.setIdActEconomica(1L);
        employerResponse.setIdDepartamento(1);
        employerResponse.setIdEmpresa("Id Empresa");
        employerResponse.setIdMunicipio(1);
        employerResponse.setIdRepresentanteLegal("Id Representante Legal");
        employerResponse.setIdSubEmpresa(1);
        employerResponse.setIdTipoDoc("Id Tipo Doc");
        employerResponse.setIdTipoDocRepLegal("Id Tipo Doc Rep Legal");
        employerResponse.setNombreEstado("Nombre Estado");
        employerResponse.setRazonSocial("Razon Social");
        employerResponse.setRazonSocialSubempresa("Razon Social Subempresa");
        employerResponse.setRepresentanteLegal("Representante Legal");
        employerResponse.setTelefonoEmpresa("Telefono Empresa");

        ArrayList<EmployerResponse> employerResponseList = new ArrayList<>();
        employerResponseList.add(employerResponse);
        Mono<List<EmployerResponse>> justResult = Mono.just(employerResponseList);
        when(consultEmployerClient.consult(
                Mockito.<String>any(), Mockito.<String>any(), Mockito.<Integer>any()))
                .thenReturn(justResult);

        // Act
        Boolean actualAffiliateBUsResult =
                affiliationEmployerActivitiesMercantileServiceImpl.affiliateBUs(
                        "Tipo Doc", "Id Empresa", 1);

        // Assert
        verify(filedService).getNextFiledNumberAffiliation();
        verify(consultEmployerClient).consult("Tipo Doc", "Id Empresa", 1);
        verify(iUserPreRegisterRepository)
                .findByIdentificationTypeAndIdentification(
                        "Id Tipo Doc Rep Legal", "Id Representante Legal");
        assertTrue(actualAffiliateBUsResult);
    }

    @Test
    void validationsStepOne_TI_error() {
        assertThrows(AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.validationsStepOne("123", Constant.TI, null));
    }

    @Test
    void stateDocuments_marksReviewed_and_persists() {
        DocumentsDTO d1 = new DocumentsDTO();
        d1.setId(1L);
        d1.setReject(true);
        DocumentsDTO d2 = new DocumentsDTO();
        d2.setId(2L);
        d2.setReject(false);

        DataDocumentAffiliate e1 = new DataDocumentAffiliate();
        e1.setId(1L);
        DataDocumentAffiliate e2 = new DataDocumentAffiliate();
        e2.setId(2L);

        when(iDataDocumentRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(iDataDocumentRepository.findById(2L)).thenReturn(Optional.of(e2));

        affiliationEmployerActivitiesMercantileServiceImpl.stateDocuments(List.of(d1, d2), 777L);

        assertTrue(e1.getRevised());
        assertTrue(e1.getState());    // reject = true  -> state = true
        assertTrue(e2.getRevised());
        assertFalse(e2.getState());   // reject = false -> state = false
        verify(iDataDocumentRepository).save(e1);
        verify(iDataDocumentRepository).save(e2);
    }

    @Test
    void stepOne_userNotFound_throws() {
        DataBasicCompanyDTO dto = basicDTO("CC", "100");
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "100"))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundInDataBase.class, () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dto));
    }

    @Test
    void stepOne_ageOutOfRange_throws() {
        DataBasicCompanyDTO dto = basicDTO("CC", "200");
        UserMain u = new UserMain();
        // Menor de 18 => fuera de rango [min=18, max=65] que devolvemos arriba
        u.setDateBirth(LocalDate.now().minusYears(17));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "200"))
                .thenReturn(Optional.of(u));

        // NO hacemos when(messageErrorAge.messageErrorAge(...)) para evitar el error de método inexistente
        assertThrows(AffiliationError.class, () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dto));
    }

    @ParameterizedTest
    @ValueSource(strings  = {"5", "", "0"})
    @NullSource
    void validationsStepOne_exception_user_no_found(String value){

        when(collectProperties.getUrlTransversal())
                .thenReturn("");

        String nit = "900373120";
        String dv = "0";
        String url = "https://mockurl/api/ws_confecamaras?nit=" + nit + "&dv=" + dv;

        RecordResponseDTO response = new RecordResponseDTO();

        when(collectProperties.getUrlTransversal()).thenReturn("https://mockurl/api/");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RecordResponseDTO.class))
                .thenReturn(Flux.fromIterable(List.of(response)));


        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .validationsStepOne("900373120", Constant.NI, value)
        );

        assertNotNull(ex);
    }

    @ParameterizedTest
    @CsvSource({
            "NI, 900373120, 0",
            "NI, 600123456, 1",
            "CC, 123456789, 1"
    })
    void validationsStepOne(String type, String n, String d){

        when(collectProperties.getUrlTransversal())
                .thenReturn("");

        String nit = "900373120";
        String dv = "0";
        String url = "https://mockurl/api/ws_confecamaras?nit=" + nit + "&dv=" + dv;

        UserMain userMain = new UserMain();

        RecordResponseDTO response = new RecordResponseDTO();

        when(collectProperties.getUrlTransversal()).thenReturn("https://mockurl/api/");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RecordResponseDTO.class))
                .thenReturn(Flux.fromIterable(List.of(response)));
        when(iUserPreRegisterRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(userMain));
        when(iUserRegisterService.calculateModulo11DV(n))
                .thenReturn(Integer.valueOf(d));


        DataBasicCompanyDTO ex =  affiliationEmployerActivitiesMercantileServiceImpl
                        .validationsStepOne(n, type, d);

        assertNotNull(ex);
    }

    @Test
    void validationsStepOne_exception_(){

        List<AffiliateMercantile> listMercantile = listMercantile();
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(collectProperties.getUrlTransversal())
                .thenReturn("");

        String nit = "900373120";
        String dv = "0";
        String url = "https://mockurl/api/ws_confecamaras?nit=" + nit + "&dv=" + dv;

        UserMain userMain = new UserMain();

        RecordResponseDTO response = new RecordResponseDTO();

        when(collectProperties.getUrlTransversal()).thenReturn("https://mockurl/api/");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RecordResponseDTO.class))
                .thenReturn(Flux.fromIterable(List.of(response)));
        when(iUserPreRegisterRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(userMain));
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(listMercantile);
        when(affiliateRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliate));



        AffiliationAlreadyExistsError ex =  assertThrows(
                AffiliationAlreadyExistsError.class,
                () ->affiliationEmployerActivitiesMercantileServiceImpl
                .validationsStepOne("123456789", "CC", ""));

        assertNotNull(ex);
    }

    @Test
    void validationsStepOne_exception(){

        when(collectProperties.getUrlTransversal())
                .thenReturn("");

        String nit = "900373120";
        String dv = "0";
        String url = "https://mockurl/api/ws_confecamaras?nit=" + nit + "&dv=" + dv;

        RecordResponseDTO response = new RecordResponseDTO();

        when(collectProperties.getUrlTransversal()).thenReturn("https://mockurl/api/");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RecordResponseDTO.class))
                .thenReturn(Flux.fromIterable(List.of(response)));


        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .validationsStepOne("900373120", Constant.NI, "0")
        );

        assertNotNull(ex);
    }

    @Test
    void testConsultWSConfecamaras_whenResponseHasData_thenFillCompany() {
        // given
        String nit = "900373120";
        String dv = "0";
        String url = "https://mockurl/api/ws_confecamaras?nit=" + nit + "&dv=" + dv;

        DataBasicCompanyDTO dto = new DataBasicCompanyDTO();
        RecordResponseDTO response = new RecordResponseDTO();

        when(collectProperties.getUrlTransversal()).thenReturn("https://mockurl/api/");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RecordResponseDTO.class))
                .thenReturn(Flux.fromIterable(List.of(response)));

        // when
        affiliationEmployerActivitiesMercantileServiceImpl.consultWSConfecamaras(nit, dv, dto);

        // then
        verify(collectProperties).getUrlTransversal();
        verify(webClient).get();
        verify(requestHeadersSpec).retrieve();

    }

    @Test
    void stepOne_exception_age_min(){

        DataBasicCompanyDTO dataBasicCompanyDTO = createRandomDataBasicCompanyDTO();
        UserMain userMain = new UserMain();
        userMain.setDateBirth(LocalDate.now());

        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(dataBasicCompanyDTO.getTypeDocumentPersonResponsible(), dataBasicCompanyDTO.getNumberDocumentPersonResponsible()))
                .thenReturn(Optional.of(userMain));

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO)
        );
        assertNotNull(ex);
    }

    @Test
    void stepOne_exception_other_affiliation(){

        List<AffiliateMercantile> mercantileList =  listMercantile()
                .stream()
                .peek(a -> {
                    a.setAffiliationCancelled(false);
                    a.setFiledNumber("123");
                })
                .toList();

        DataBasicCompanyDTO dataBasicCompanyDTO = createRandomDataBasicCompanyDTO();
        UserMain userMain = new UserMain();
        userMain.setDateBirth(LocalDate.of(2000, 01, 01));

        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(dataBasicCompanyDTO.getTypeDocumentPersonResponsible(), dataBasicCompanyDTO.getNumberDocumentPersonResponsible()))
                .thenReturn(Optional.of(userMain));
        when( collectProperties.getMinimumAge()).thenReturn(1);
        when( collectProperties.getMaximumAge()).thenReturn(100);
        when(identificationLegalNatureService.findByNit(dataBasicCompanyDTO.getNumberIdentification().concat(":").concat(dataBasicCompanyDTO.getDigitVerificationDV().toString())))
                .thenReturn(true);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(mercantileList);


        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO)
        );
        assertNotNull(ex);
    }

    @Test
    void stepOne_exception_affiliate_not_found(){

        List<AffiliateMercantile> mercantileList =  new ArrayList<>();

        AffiliateMercantile mercantile = new AffiliateMercantile();

        DataBasicCompanyDTO dataBasicCompanyDTO = createRandomDataBasicCompanyDTO();
        UserMain userMain = new UserMain();
        userMain.setDateBirth(LocalDate.of(2000, 01, 01));

        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(dataBasicCompanyDTO.getTypeDocumentPersonResponsible(), dataBasicCompanyDTO.getNumberDocumentPersonResponsible()))
                .thenReturn(Optional.of(userMain));
        when( collectProperties.getMinimumAge()).thenReturn(1);
        when( collectProperties.getMaximumAge()).thenReturn(100);
        when(identificationLegalNatureService.findByNit(dataBasicCompanyDTO.getNumberIdentification().concat(":").concat(dataBasicCompanyDTO.getDigitVerificationDV().toString())))
                .thenReturn(true);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(mercantileList);
        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        Exception response = assertThrows(
                Exception.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO)
        );
        assertNotNull(response);
    }

    @Test
    void findUser(){

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setTypeDocumentIdentification("123456789");
        affiliateMercantile.setNumberIdentification("CC");

        UserMain userMain = createUserMainTestData();

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        DataLegalRepresentativeDTO response = affiliationEmployerActivitiesMercantileServiceImpl
                .findUser(affiliateMercantile);

        assertNotNull(response);
    }

    @ParameterizedTest
    @ValueSource(strings  = {"RURAL", "URBANA"})
    void stepOne(String value){

        List<AffiliateMercantile> mercantileList =  new ArrayList<>();

        AffiliateMercantile mercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();

        DataBasicCompanyDTO dataBasicCompanyDTO = createRandomDataBasicCompanyDTO();
        dataBasicCompanyDTO.setZoneLocationEmployer(value);
        UserMain userMain = new UserMain();
        userMain.setDateBirth(LocalDate.of(2000, 01, 01));

        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(dataBasicCompanyDTO.getTypeDocumentPersonResponsible(), dataBasicCompanyDTO.getNumberDocumentPersonResponsible()))
                .thenReturn(Optional.of(userMain));
        when( collectProperties.getMinimumAge()).thenReturn(1);
        when( collectProperties.getMaximumAge()).thenReturn(100);
        when(identificationLegalNatureService.findByNit(dataBasicCompanyDTO.getNumberIdentification().concat(":").concat(dataBasicCompanyDTO.getDigitVerificationDV().toString())))
                .thenReturn(true);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(mercantileList);
        when(affiliateRepository.save(any()))
                .thenReturn(affiliate1);
        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        AffiliateMercantile response = affiliationEmployerActivitiesMercantileServiceImpl.stepOne(dataBasicCompanyDTO);
        assertNotNull(response);
    }

    @Test
    void stepTwo_user_not_found(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        AffiliateMercantile mercantile = new AffiliateMercantile();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () ->affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
         assertNotNull(ex);
    }

    @Test
    void stepTwo_exception(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        AffiliateMercantile mercantile = new AffiliateMercantile();
        UserMain userMain =  createUserMainTestData();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        Exception response = assertThrows(
                Exception.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
        assertNotNull(response);
    }

    @Test
    void stepTwo_exception_activity_economic(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setTypeDocumentIdentification("CC");
        UserMain userMain =  createUserMainTestData();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
        assertNotNull(response);
    }

    @Test
    void stepTwo_exception_economic_null(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("CC");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic()
                .stream()
                .peek(e -> e.setId(1L))
                .toList();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, true)
        );
        assertNotNull(response);
    }

    @Test
    void stepTwo_exception_economic_other(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();

        dataLegalRepresentativeDTO.setIdActivityEconomic(activityEconomic());
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("NI");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic()
                .stream()
                .peek(e -> e.setId(1L))
                .toList();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
        assertNotNull(response);
    }

    @Test
    void stepTwo_exception_economic_other2(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        dataLegalRepresentativeDTO.setIdActivityEconomic(activityEconomic2());
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("NI");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic()
                .stream()
                .peek(e -> e.setId(1L))
                .toList();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
        assertNotNull(response);
    }

    @Test
    void stepTwo_exception_economic(){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        dataLegalRepresentativeDTO.setIdActivityEconomic(null);
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("CC");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic()
                .stream()
                .peek(e -> e.setId(1L))
                .toList();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepTwo(dataLegalRepresentativeDTO, false)
        );
        assertNotNull(response);
    }

    @ParameterizedTest
    @CsvSource({
            "CC, true",
            "NI, true"
    })
    void stepTwo(String type, boolean r){

        DataLegalRepresentativeDTO dataLegalRepresentativeDTO = createDataLegalRepresentativeTestData();
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification(type);
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        AffiliateMercantile response =  affiliationEmployerActivitiesMercantileServiceImpl
                .stepTwo(dataLegalRepresentativeDTO, r);
        assertNotNull(response);
    }

    @Test
    void stepThree_exception() {

        List<DocumentRequestDTO> files = listDocuments()
                .stream()
                .peek(e -> e.setIdDocument(2L))
                .toList();

       when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
       when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
       AffiliateMercantile mercantile = mercantile();
       when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
               .thenReturn(Optional.of(mercantile));
       when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(any()))
               .thenReturn(listDocumentRequested());

            EntryDTO entryDTO = new EntryDTO();
            CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
            ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
            ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
            entryDTO.setEntry(new EntryDetailsDTO(
                            "Jan 1, 2020 8:00am GMT+0100",
                            true,
                            true,
                            createdByUser,
                            "Jan 1, 2020 9:00am GMT+0100",
                            modifiedByUser,
                            "firma",
                            "42",
                            "firma",
                            content,
                            "42"));

            ArrayList<EntryDTO> entries = new ArrayList<>();
            entries.add(entryDTO);
            PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

            ListDTO list = new ListDTO(pagination, entries);

            AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
            alfrescoResponseDTO.setList(list);
            when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
            Optional<String> ofResult2 = Optional.of("42");
            when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                    .thenReturn(ofResult2);

            // Act and Assert
       AffiliationError response = assertThrows(
               AffiliationError.class,
               () -> affiliationEmployerActivitiesMercantileServiceImpl
                       .stepThree(1L, 1L, 1L, files)
       );
           assertNotNull(response);
    }

    @Test
    void stepThree_exception_document() {

        Affiliate affiliate1 = new Affiliate();

        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        AffiliateMercantile mercantile = mercantile();
        when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
                .thenReturn(Optional.of(mercantile));
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(any()))
                .thenReturn(listDocumentRequested());
        when(affiliateRepository.save(any()))
                .thenReturn(affiliate1);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stepThree(1L, 1L, 1L, listDocuments())
        );

        assertNotNull(response);
    }

    @Test
    void stepThree() throws IOException {

        Affiliate affiliate1 = new Affiliate();
        AlfrescoUploadResponse alfrescoUploadResponse = new AlfrescoUploadResponse();
        DataUpload dataUpload = new DataUpload();
        Entry entry = new Entry();
        entry.setId("123");
        dataUpload.setEntry(entry);
        alfrescoUploadResponse.setData(dataUpload);
        DataDocumentAffiliate dataDocument = new DataDocumentAffiliate();
        List<DocumentRequestDTO> listDocuments = listDocuments()
                .stream()
                .peek(e ->e.setFile("aGVsbG8gd29ybGQ="))
                .toList();


        when(filedService.getNextFiledNumberAffiliation()).thenReturn("42");
        when(collectProperties.getFolderIdMercantile()).thenReturn("Folder Id Mercantile");
        AffiliateMercantile mercantile = mercantile();
        when(affiliateMercantileRepository.findById(Mockito.<Long>any()))
                .thenReturn(Optional.of(mercantile));
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(any()))
                .thenReturn(listDocumentRequested());
        when(affiliateRepository.save(any()))
                .thenReturn(affiliate1);
        when( documentNameStandardizationService.getName(any(), any(), any()))
                .thenReturn("nombre.pdf");
        when(alfrescoService.uploadFileAlfresco(any()))
                .thenReturn(alfrescoUploadResponse);
        when(iDataDocumentRepository.save(any()))
                .thenReturn(dataDocument);

        EntryDTO entryDTO = new EntryDTO();
        CreatedByUserDTO createdByUser = new CreatedByUserDTO("42", "firma");
        ModifiedByUserDTO modifiedByUser = new ModifiedByUserDTO("42", "firma");
        ContentDTO content = new ContentDTO("text/plain", "firma", 3, "UTF-8");
        entryDTO.setEntry(
                new EntryDetailsDTO(
                        "Jan 1, 2020 8:00am GMT+0100",
                        true,
                        true,
                        createdByUser,
                        "Jan 1, 2020 9:00am GMT+0100",
                        modifiedByUser,
                        "firma",
                        "42",
                        "firma",
                        content,
                        "42"));

        ArrayList<EntryDTO> entries = new ArrayList<>();
        entries.add(entryDTO);
        PaginationDTO pagination = new PaginationDTO(3, true, 1000, 3, 3);

        ListDTO list = new ListDTO(pagination, entries);

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();
        alfrescoResponseDTO.setList(list);
        when(genericWebClient.getChildrenNode(Mockito.<String>any())).thenReturn(alfrescoResponseDTO);
        Optional<String> ofResult2 = Optional.of("42");
        when(genericWebClient.folderExistsByName(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(ofResult2);

        AffiliateMercantileDTO response = affiliationEmployerActivitiesMercantileServiceImpl
                        .stepThree(1L, 1L, 1L, listDocuments);

        assertNotNull(response);
    }

    @Test
    void stateAffiliation_error_affiliation(){

        AffiliateMercantile affiliateMercantile = mercantile();
        StateAffiliation stateAffiliation = new StateAffiliation();
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate1 = new Affiliate();

        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne((Specification<Affiliate>) any()))
                .thenReturn(Optional.of(affiliate1));

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                .stateAffiliation(affiliateMercantile, stateAffiliation));
        assertNotNull(ex);
    }

    @Test
    void stateAffiliation_error_affiliation2(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        affiliateMercantile.setStageManagement(Constant.REGULARIZATION);
        StateAffiliation stateAffiliation = new StateAffiliation();
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate1 = new Affiliate();

        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne((Specification<Affiliate>) any()))
                .thenReturn(Optional.of(affiliate1));

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stateAffiliation(affiliateMercantile, stateAffiliation));
        assertNotNull(ex);
    }

    @Test
    void stateAffiliation_error_affiliate3(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        StateAffiliation stateAffiliation = new StateAffiliation();
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setIdAffiliate(1L);

        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne((Specification<Affiliate>) any()))
                .thenReturn(Optional.of(affiliate1));
        when(iDataDocumentRepository.findAll((Specification<DataDocumentAffiliate>) any()))
                .thenReturn(lisDocuments());

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .stateAffiliation(affiliateMercantile, stateAffiliation));
        assertNotNull(ex);
    }


    @Test
    void stateAffiliation(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        StateAffiliation stateAffiliation = new StateAffiliation();
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate1 = new Affiliate();

        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne((Specification<Affiliate>) any()))
                .thenReturn(Optional.of(affiliate1));

        affiliationEmployerActivitiesMercantileServiceImpl
                .stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(affiliateMercantileRepository, times(1)).save(any());
    }

    @Test
    void stateAffiliation_reject(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setRejectAffiliation(true);
        stateAffiliation.setComment(List.of("comentario"));
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate1 = new Affiliate();

        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne((Specification<Affiliate>) any()))
                .thenReturn(Optional.of(affiliate1));

        affiliationEmployerActivitiesMercantileServiceImpl
                .stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(affiliateMercantileRepository, times(1)).save(any());
    }

    @Test
    void interviewWeb_error_affiliate(){

        AffiliateMercantile affiliateMercantile = mercantile();
        UserMain userMain =  createUserMainTestData();

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));

        StateAffiliation stateAffiliation = new StateAffiliation();

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () ->  affiliationEmployerActivitiesMercantileServiceImpl
                        .interviewWeb(stateAffiliation));
        assertNotNull(ex);
    }

    @Test
    void interviewWeb_error_affiliate2(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        affiliateMercantile.setStageManagement(Constant.REGULARIZATION);
        UserMain userMain =  createUserMainTestData();

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));

        StateAffiliation stateAffiliation = new StateAffiliation();

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () ->  affiliationEmployerActivitiesMercantileServiceImpl
                        .interviewWeb(stateAffiliation));
        assertNotNull(ex);
    }

    @Test
    void interviewWeb_reject(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        UserMain userMain =  createUserMainTestData();

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setRejectAffiliation(true);
        stateAffiliation.setComment(List.of("Mensaje"));

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));

        affiliationEmployerActivitiesMercantileServiceImpl
                .interviewWeb(stateAffiliation);

        verify(iUserPreRegisterRepository, times(1)).findById(any());
    }

    @Test
    void interviewWeb_error_activity_economic(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate = new Affiliate();

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setRejectAffiliation(false);
        stateAffiliation.setComment(List.of("Mensaje"));

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByFiledNumber(any()))
                .thenReturn(Optional.of(affiliate));
        when(mainOfficeService.findCode())
                .thenReturn("123");

        AffiliationError ex = assertThrows(
                AffiliationError.class,
                () ->  affiliationEmployerActivitiesMercantileServiceImpl
                        .interviewWeb(stateAffiliation));
        assertNotNull(ex);
    }

    @Test
    void interviewWeb(){

        AffiliateMercantile affiliateMercantile = mercantile();
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        affiliateMercantile.setEconomicActivity(listEconomicAffiliate());
        UserMain userMain =  createUserMainTestData();
        Affiliate affiliate = new Affiliate();
        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(1L);

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setRejectAffiliation(false);
        stateAffiliation.setComment(List.of("Mensaje"));

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findById(any()))
                .thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByFiledNumber(any()))
                .thenReturn(Optional.of(affiliate));
        when(mainOfficeService.findCode())
                .thenReturn("123");
        when(workCenterRepository.save(any()))
                .thenReturn(workCenter);

        affiliationEmployerActivitiesMercantileServiceImpl
                .interviewWeb(stateAffiliation);

        verify(iUserPreRegisterRepository, times(2)).findById(any());
    }

    @Test
    void updateDataInterviewWeb_exception(){

        AffiliateMercantile affiliateMercantile = mercantile();
        InterviewWebDTO interviewWebDTO = new InterviewWebDTO();
        interviewWebDTO.setFiledNumber("123");
        interviewWebDTO.setDataBasicCompanyDTO(createRandomDataBasicCompanyDTO());
        interviewWebDTO.setDataLegalRepresentativeDTO(createDataLegalRepresentativeTestData());
        interviewWebDTO.setIdActivityEconomic(activityEconomic2());

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("CC");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));

        AffiliationError response = assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerActivitiesMercantileServiceImpl
                        .updateDataInterviewWeb(interviewWebDTO)
        );
        assertNotNull(response);
    }

    @Test
    void updateDataInterviewWeb(){

        AffiliateMercantile affiliateMercantile = mercantile();
        InterviewWebDTO interviewWebDTO = new InterviewWebDTO();
        interviewWebDTO.setFiledNumber("123");
        interviewWebDTO.setDataBasicCompanyDTO(createRandomDataBasicCompanyDTO());
        interviewWebDTO.setDataLegalRepresentativeDTO(createDataLegalRepresentativeTestData());
        interviewWebDTO.setIdActivityEconomic(activityEconomic3());

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(listEconomicAffiliate());
        mercantile.setTypeDocumentIdentification("CC");
        UserMain userMain =  createUserMainTestData();
        List<EconomicActivity> listEconomic =  listEconomic();

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(mercantile));

        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));

        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantile);

        when(iEconomicActivityRepository.findEconomicActivities(anyList()))
                .thenReturn(listEconomic);

        when(affiliateMercantileRepository.findOne((Specification<AffiliateMercantile>) any()))
                .thenReturn(Optional.of(affiliateMercantile));

        String response  = affiliationEmployerActivitiesMercantileServiceImpl
                .updateDataInterviewWeb(interviewWebDTO);
        assertNotNull(response);
    }

    @Test
    void updateDataRegularizationStepOne(){

        DataBasicCompanyDTO dto = createRandomDataBasicCompanyDTO();

        AffiliateMercantile response =  new AffiliateMercantile();
        response.setStageManagement(Constant.REGULARIZATION);

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(response));
        when( affiliateMercantileRepository.save(any()))
                .thenReturn(response);

        response = affiliationEmployerActivitiesMercantileServiceImpl
                .updateDataRegularizationStepOne(dto);

        assertNotNull(response);

    }

    @Test
    void updateDataRegularizationStepTwo(){

        DataLegalRepresentativeDTO dto = new DataLegalRepresentativeDTO();
        AddressDTO addressDTO = new AddressDTO();
        dto.setAddressDTO(addressDTO);

        AffiliateMercantile response =  new AffiliateMercantile();
        response.setStageManagement(Constant.REGULARIZATION);

        when(affiliateMercantileRepository.findById(any()))
                .thenReturn(Optional.of(response));
        when( affiliateMercantileRepository.save(any()))
                .thenReturn(response);

        response = affiliationEmployerActivitiesMercantileServiceImpl
                .updateDataRegularizationStepTwo(dto);

        assertNotNull(response);

    }

    @Test
    void affiliateBUs(){

        // given
        String tipoDoc = "NIT";
        String idEmpresa = "900123456";
        Integer idSubEmpresa = 1;

        EmployerResponse mockResponse = new EmployerResponse();
        mockResponse.setIdTipoDoc("CC");
        mockResponse.setIdEmpresa("900123456");
        mockResponse.setRazonSocial("Mi Empresa SAS");
        mockResponse.setIdDepartamento(5);
        mockResponse.setIdMunicipio(120);
        mockResponse.setDireccionEmpresa("Calle 123");
        mockResponse.setTelefonoEmpresa("3001234567");
        mockResponse.setEmailEmpresa("info@empresa.com");
        mockResponse.setIdTipoDocRepLegal("CC");
        mockResponse.setIdRepresentanteLegal("12345678");
        mockResponse.setIdActEconomica(777L);
        mockResponse.setFechaAfiliacionEfectiva("2025/10/22 00:00:00");

        when(consultEmployerClient.consult(tipoDoc, idEmpresa, idSubEmpresa))
                .thenReturn(Mono.just(List.of(mockResponse)));

        when(filedService.getNextFiledNumberAffiliation()).thenReturn("FILED123");

        UserMain user = new UserMain();
        user.setId(1L);
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(any(), any()))
                .thenReturn(Optional.of(user));

        Municipality muni = new Municipality();
        muni.setIdMunicipality(999L);
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(any(), any()))
                .thenReturn(Optional.of(muni));

        ArlInformation arl = new ArlInformation();
        arl.setCode("ARL001");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(List.of(arl));

        AffiliateMercantile mercantileSaved = new AffiliateMercantile();
        mercantileSaved.setId(10L);
        when(affiliateMercantileRepository.save(any()))
                .thenReturn(mercantileSaved);

        EconomicActivity activity = new EconomicActivity();
        activity.setId(100L);
        activity.setEconomicActivityCode("777");
        when(iEconomicActivityRepository.findByEconomicActivityCode(any()))
                .thenReturn(List.of(activity));

        when(iUserRegisterService.isEmployerPersonJuridica(anyLong())).thenReturn(true);

        when(affiliateRepository.save(any())).thenReturn(new Affiliate());

        Boolean result = affiliationEmployerActivitiesMercantileServiceImpl.affiliateBUs(tipoDoc, idEmpresa, idSubEmpresa);
        assertNotNull(result);


    }

    List<AffiliateMercantile> listMercantile(){

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setFiledNumber("123");
        List<AffiliateMercantile> list  = new ArrayList<>();
        list.add(affiliateMercantile);
        return list;
    }

    DataBasicCompanyDTO createRandomDataBasicCompanyDTO() {
        AddressDTO address = addressDTO();

        DataContactCompanyDTO contact = new DataContactCompanyDTO();
        contact.setPhoneOneContactCompany("6015551111");
        contact.setPhoneTwoContactCompany("3205551111");
        contact.setEmailContactCompany("contacto@empresa-prueba.com");
        contact.setDepartmentContactCompany(11L);
        contact.setCityMunicipalityContactCompany(11001L);
        contact.setAddressDTO(address);

        DataBasicCompanyDTO company = new DataBasicCompanyDTO();
        company.setIdAffiliationMercantile(1L);
        company.setTypeDocumentIdentification("NIT");
        company.setNumberIdentification("600123456");
        company.setDigitVerificationDV(1L);
        company.setBusinessName("EMPRESA DE PRUEBA S.A.S");
        company.setTypePerson("JURIDICA");
        company.setNumberWorkers(25L);
        company.setZoneLocationEmployer("URBANA");
        company.setDepartment(11L);
        company.setCityMunicipality(11001L);
        company.setPhoneOne("6015552222");
        company.setPhoneTwo("3205552222");
        company.setEmail("info@empresa-prueba.com");
        company.setNumberDocumentPersonResponsible("1234567890");
        company.setTypeDocumentPersonResponsible("CC");
        company.setAddressDTO(address);
        company.setAddressIsEqualsContactCompany(false);
        company.setDataContactCompanyDTO(contact);
        company.setConsecutiveDecentralized("0");
        company.setLegalStatus("ACTIVA");

        return company;
    }

    UserMain createUserMainTestData() {
        UserMain user = new UserMain();
        user.setId(1L);
        user.setFirstName("Juan");
        user.setSecondName("Carlos");
        user.setSurname("Pérez");
        user.setSecondSurname("Gómez");
        user.setCompanyName("EMPRESA DE PRUEBA S.A.S");
        user.setUserType(2L);
        user.setStatus(1L);
        user.setEmail("juan.perez@example.com");
        user.setPhoneNumber("3001234567");
        user.setPhoneNumber2("6014567890");
        user.setPin("1234");
        user.setCreateDate(new Timestamp(System.currentTimeMillis()));
        user.setIdentificationType("NIT");
        user.setIdentification("900600123");
        user.setVerificationDigit(3);
        user.setAcceptNotification(true);
        user.setProfile("EMPLOYER");
        user.setDateBirth(LocalDate.of(1990, 5, 14));
        user.setAge(35);
        user.setSex("M");
        user.setOtherSex(null);
        user.setNationality(1L);
        user.setAddress("Cra 10 # 25 - 33");
        user.setStatusPreRegister(true);
        user.setStatusActive(true);
        user.setStatusStartAfiiliate(false);
        user.setLastAffiliationAttempt(LocalDateTime.now().minusDays(10));
        user.setStatusInactiveSince(null);
        user.setLoginAttempts(0);
        user.setLockoutTime(null);
        user.setValidAttempts(1);
        user.setValidOutTime(LocalDateTime.now().plusDays(30));
        user.setGenerateAttempts(0);
        user.setGenerateOutTime(null);
        user.setIdDepartment(11L);
        user.setIdCity(11001L);
        user.setIdMainStreet(5L);
        user.setIdNumberMainStreet(45L);
        user.setIdLetter1MainStreet(1L);
        user.setIsBis(false);
        user.setIdLetter2MainStreet(2L);
        user.setIdCardinalPointMainStreet(3L);
        user.setIdNum1SecondStreet(10L);
        user.setIdLetterSecondStreet(4L);
        user.setIdNum2SecondStreet(25L);
        user.setIdCardinalPoint2(1L);
        user.setIdHorizontalProperty1(1L);
        user.setIdNumHorizontalProperty1(2L);
        user.setIdHorizontalProperty2(3L);
        user.setIdNumHorizontalProperty2(4L);
        user.setIdHorizontalProperty3(5L);
        user.setIdNumHorizontalProperty3(6L);
        user.setIdHorizontalProperty4(7L);
        user.setIdNumHorizontalProperty4(8L);
        user.setLastUpdate(LocalDateTime.now());
        user.setHealthPromotingEntity(101L);
        user.setPensionFundAdministrator(202L);
        user.setLastPasswordUpdate(LocalDateTime.now().minusDays(15));
        user.setIsPasswordExpired(false);
        user.setInactiveByPendingAffiliation(false);
        user.setIsTemporalPassword(false);
        user.setCreatedAtTemporalPassword(LocalDate.now());
        user.setInfoOperator(null);
        user.setFinancialOperator(null);
        user.setIsInArrearsStatus(false);
        user.setUserName("jperez");
        user.setIsImport(false);
        user.setAssignedPassword(true);
        user.setEmployerUpdateTime(LocalDateTime.now().minusDays(5));
        user.setCodeOtp("987654");
        user.setPosition(1);
        user.setOffice(101);
        user.setArea(10L);
        user.setLevelAuthorization("LEVEL_1");
        user.setRoles(Collections.emptyList());

        return user;
    }

    AddressDTO addressDTO(){
        return AddressDTO.builder()
                .address("Calle 45 #12A-34")
                .idDepartment(11L) // Bogotá
                .idCity(11001L)
                .idMainStreet(10L)
                .idNumberMainStreet(12L)
                .idLetter1MainStreet(1L)
                .isBis(false)
                .idLetter2MainStreet(2L)
                .idCardinalPointMainStreet(1L)
                .idNum1SecondStreet(45L)
                .idLetterSecondStreet(3L)
                .idNum2SecondStreet(67L)
                .idCardinalPoint2(2L)
                .idHorizontalProperty1(1L)
                .idNumHorizontalProperty1(101L)
                .idHorizontalProperty2(2L)
                .idNumHorizontalProperty2(202L)
                .idHorizontalProperty3(3L)
                .idNumHorizontalProperty3(303L)
                .idHorizontalProperty4(4L)
                .idNumHorizontalProperty4(404L)
                .build();
    }

    DataLegalRepresentativeDTO createDataLegalRepresentativeTestData() {
        DataLegalRepresentativeDTO dto = new DataLegalRepresentativeDTO();

        dto.setIdAffiliationMercantile(1001L);
        dto.setIdentificationType("CC");
        dto.setIdentification("1020304050");
        dto.setTypePerson("NATURAL");
        dto.setFirstName("Carlos");
        dto.setSecondName("Andrés");
        dto.setSurname("Ramírez");
        dto.setSecondSurname("López");
        dto.setDateBirth(LocalDate.of(1985, 7, 22));
        dto.setAge(40);
        dto.setSex("M");
        dto.setNacionality("Colombiana");
        dto.setEps(101L);
        dto.setAfp(202L);
        dto.setPhoneOne("3004567890");
        dto.setPhoneTwo("6017894561");
        dto.setEmail("carlos.ramirez@example.com");

        dto.setIdDepartment(11L);  // Bogotá
        dto.setIdCity(11001L);
        dto.setAddress("Calle 45 # 25-10");

        AddressDTO addressDTO = addressDTO();

        dto.setAddressDTO(addressDTO);

        Map<Long, Boolean> activities = new HashMap<>();
        activities.put(101L, true);
        activities.put(202L, false);
        dto.setIdActivityEconomic(activities);

        return dto;
    }

    List<AffiliateActivityEconomic> listEconomicAffiliate(){
        EconomicActivity economicActivity = EconomicActivity.builder()
                .classRisk("High")
                .codeCIIU("A123")
                .additionalCode("X1")
                .description("Manufacturing of widgets")
                .economicActivityCode("ECO001")
                .idEconomicSector(10L)
                .build();
        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setIsPrimary(true);
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        List<AffiliateActivityEconomic> list = new ArrayList<>();
        list.add(affiliateActivityEconomic);
        return list;
    }

    List<EconomicActivity> listEconomic(){
        List<EconomicActivity> list = new ArrayList<>();
        EconomicActivity economicActivity1 = new EconomicActivity();
        EconomicActivity economicActivity2 = new EconomicActivity();
        economicActivity2.setId(101L);
        economicActivity1.setId(202L);
        list.add(economicActivity1);
        list.add(economicActivity2);
        return list;
    }

    Map<Long, Boolean> activityEconomic(){

        Map<Long, Boolean> map = new HashMap<>();
        map.put(1L, true);
        map.put(2L, true);
        map.put(3L, true);
        map.put(4L, true);
        map.put(5L, true);
        map.put(6L, true);
        return map;
    }

    Map<Long, Boolean> activityEconomic2(){

        Map<Long, Boolean> map = new HashMap<>();
        map.put(1L, true);
        map.put(2L, true);
        map.put(3L, true);
        map.put(4L, true);
        return map;
    }

    Map<Long, Boolean> activityEconomic3(){

        Map<Long, Boolean> map = new HashMap<>();
        map.put(101L, true);
        map.put(202L, false);
        return map;
    }

    List<DocumentRequestDTO> listDocuments(){
        List<DocumentRequestDTO> list = new ArrayList<>();
        DocumentRequestDTO document =  new DocumentRequestDTO();
        document.setIdDocument(1L);
        document.setName("nombre.pdf");
        list.add(document);
        return list;
    }

    AffiliateMercantile mercantile(){

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        return affiliateMercantile;
    }

    List<DocumentRequested> listDocumentRequested(){
        List<DocumentRequested> list = new ArrayList<>();
        DocumentRequested documentRequested = new DocumentRequested();
        documentRequested.setRequested(true);
        documentRequested.setId(1L);
        documentRequested.setName("documento.pdf");
        list.add(documentRequested);
        return list;
    }

    List<DataDocumentAffiliate> lisDocuments(){
        List<DataDocumentAffiliate> list = new ArrayList<>();
        DataDocumentAffiliate affiliate = new DataDocumentAffiliate();
        list.add(affiliate);
        return list;
    }


}
