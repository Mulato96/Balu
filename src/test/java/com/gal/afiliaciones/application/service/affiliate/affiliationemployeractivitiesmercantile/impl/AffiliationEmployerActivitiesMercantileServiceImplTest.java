package com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.impl;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.typeemployerdocument.TypeEmployerDocumentService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.sat.SatConsultTransferableEmployerClient;
import com.gal.afiliaciones.infrastructure.dao.repository.*;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
//import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseCreateFolderDTO;
//import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.DataDTO;
//import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataContactCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffiliationEmployerActivitiesMercantileServiceImplTest {

    WebClient webClient = mock(WebClient.class);
    SendEmails sendEmails = mock(SendEmails.class);
    FiledService filedService = mock(FiledService.class);
    CollectProperties properties = mock(CollectProperties.class);
    AlfrescoService alfrescoService = mock(AlfrescoService.class);
    MainOfficeService mainOfficeService = mock(MainOfficeService.class);
    AffiliateRepository iAffiliateRepository = mock(AffiliateRepository.class);
    MainOfficeRepository mainOfficeRepository = mock(MainOfficeRepository.class);
    IUserRegisterService iUserRegisterService = mock(IUserRegisterService.class);
    WorkCenterRepository workCenterRepository = mock(WorkCenterRepository.class);
    IDataDocumentRepository dataDocumentRepository = mock(IDataDocumentRepository.class);
    IUserPreRegisterRepository iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
    IEconomicActivityRepository economicActivityRepository = mock(IEconomicActivityRepository.class);
    IAffiliationCancellationTimerRepository timerRepository = mock(IAffiliationCancellationTimerRepository.class);
    ScheduleInterviewWebService scheduleInterviewWebService = mock(ScheduleInterviewWebService.class);
    TypeEmployerDocumentService typeEmployerDocumentService = mock(TypeEmployerDocumentService.class);
    AffiliateMercantileRepository affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
    ObservationsAffiliationService observationsAffiliationService = mock(ObservationsAffiliationService.class);
    DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    MunicipalityRepository municipalityRepository = mock(MunicipalityRepository.class);
    IdentificationLegalNatureService identificationLegalNatureService = mock(IdentificationLegalNatureService.class);
    MessageErrorAge messageErrorAge = mock(MessageErrorAge.class);
    DocumentNameStandardizationService documentNameStandardizationService = mock(DocumentNameStandardizationService.class);
    ConsultEmployerClient consultEmployerClient = mock(ConsultEmployerClient.class);
    ArlInformationDao arlInformationDao = mock(ArlInformationDao.class);
    ArlRepository arlRepository = mock(ArlRepository.class);
    PolicyService policyService = mock(PolicyService.class);
    com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService registraduriaUnifiedService =
            mock(com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService.class);
    SatConsultTransferableEmployerClient satConsultTransferableEmployerClient =
            mock(SatConsultTransferableEmployerClient.class);
    com.gal.afiliaciones.infrastructure.service.ConfecamarasConsultationService confecamarasConsultationService =
            mock(com.gal.afiliaciones.infrastructure.service.ConfecamarasConsultationService.class);
    GenericWebClient genericWebClient = mock(GenericWebClient.class);

    AffiliationEmployerActivitiesMercantileServiceImpl service;

    @BeforeEach
    void setup() {
        service = Mockito.spy(new AffiliationEmployerActivitiesMercantileServiceImpl(
                webClient, sendEmails, filedService, properties, alfrescoService, mainOfficeService,
                iAffiliateRepository, mainOfficeRepository, iUserRegisterService, workCenterRepository,
                dataDocumentRepository, iUserPreRegisterRepository, economicActivityRepository,
                timerRepository, scheduleInterviewWebService, typeEmployerDocumentService,
                affiliateMercantileRepository, observationsAffiliationService, departmentRepository,
                municipalityRepository, identificationLegalNatureService, messageErrorAge,
                documentNameStandardizationService, consultEmployerClient, arlInformationDao, arlRepository,
                policyService, registraduriaUnifiedService, satConsultTransferableEmployerClient,
                confecamarasConsultationService, genericWebClient
        ));
        ReflectionTestUtils.setField(service, "satValidationEnabled", false);
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(65);
    }

    @AfterEach
    void tearDown() {
        // nada por ahora
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

    @Test
    void validationsStepOne_TI_error() {
        assertThrows(AffiliationError.class,
                () -> service.validationsStepOne("123", Constant.TI, null));
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

        when(dataDocumentRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(dataDocumentRepository.findById(2L)).thenReturn(Optional.of(e2));

        service.stateDocuments(List.of(d1, d2), 777L);

        assertTrue(e1.getRevised());
        assertTrue(e1.getState());    // reject = true  -> state = true
        assertTrue(e2.getRevised());
        assertFalse(e2.getState());   // reject = false -> state = false
        verify(dataDocumentRepository).save(e1);
        verify(dataDocumentRepository).save(e2);
    }

    @Test
    void stepOne_userNotFound_throws() {
        DataBasicCompanyDTO dto = basicDTO("CC", "100");
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "100"))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundInDataBase.class, () -> service.stepOne(dto));
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
        assertThrows(AffiliationError.class, () -> service.stepOne(dto));
    }

    @Test
    @DisplayName("validationsStepOne should return DataBasicCompanyDTO for valid CC")
    void validationsStepOne_validCC() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Jwt jwt = mock(Jwt.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("email")).thenReturn("test@test.com");

        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("12345");
        when(iUserPreRegisterRepository.findByEmailIgnoreCase("test@test.com")).thenReturn(Optional.of(user));

        DataBasicCompanyDTO result = service.validationsStepOne("12345", "CC", null);

        assertNotNull(result);
        assertEquals("CC", result.getTypeDocumentIdentification());
        assertEquals("12345", result.getNumberIdentification());
    }

    @Test
    @DisplayName("validationsStepOne should throw AffiliationError for existing active affiliation")
    void validationsStepOne_existingAffiliation() {
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(List.of(new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile()));
        when(iAffiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.Affiliate() {{
            setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        }}));

        assertThrows(AffiliationAlreadyExistsError.class, () -> service.validationsStepOne("12345", "CC", null));
    }

    @Test
    @DisplayName("stepOne should save and return AffiliateMercantile")
    void stepOne_success() {
        DataBasicCompanyDTO dto = basicDTO("CC", "123");
        dto.setAddressDTO(new AddressDTO());
        dto.setDataContactCompanyDTO(new DataContactCompanyDTO() {{
            setAddressDTO(new AddressDTO());
        }});
        UserMain user = new UserMain();
        user.setDateBirth(LocalDate.now().minusYears(30));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "123")).thenReturn(Optional.of(user));
        when(affiliateMercantileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(iAffiliateRepository.save(any())).thenAnswer(i -> {
            com.gal.afiliaciones.domain.model.affiliate.Affiliate affiliate = i.getArgument(0);
            affiliate.setIdAffiliate(1L);
            return affiliate;
        });

        com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile result = service.stepOne(dto);

        assertNotNull(result);
        verify(affiliateMercantileRepository).save(any());
    }

    @Test
    @DisplayName("stepTwo should save and return AffiliateMercantile")
    void stepTwo_success() {
        DataLegalRepresentativeDTO dto = new DataLegalRepresentativeDTO();
        dto.setIdAffiliationMercantile(1L);
        dto.setIdentificationType("CC");
        dto.setIdentification("123");
        dto.setAddressDTO(new AddressDTO());

        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile()));
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new UserMain()));
        when(affiliateMercantileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile result = service.stepTwo(dto, false);

        assertNotNull(result);
        verify(affiliateMercantileRepository).save(any());
    }

    @Test
    @DisplayName("stepThree should throw error for invalid stage")
    void stepThree_invalidStage() {
        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile() {{
            setStageManagement(Constant.SING);
        }}));

        assertThrows(AffiliationError.class, () -> service.stepThree(1L, 1L, 1L, new ArrayList<>()));
    }

    @Test
    @DisplayName("stepThree should process files and save")
    void stepThree_success() {
        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile() {{
            setNumberIdentification("123");
        }}));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F123");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(1L)).thenReturn(new ArrayList<>());
        when(alfrescoService.createFolder(any(), anyString())).thenReturn(
                new AlfrescoResponseCreateFolderDTO(new DataDTO(new EntryResponseDTO() {{
                    setId("folderId");
                }}))
        );
        when(iAffiliateRepository.save(any())).thenAnswer(i -> {
            com.gal.afiliaciones.domain.model.affiliate.Affiliate affiliate = i.getArgument(0);
            affiliate.setIdAffiliate(1L);
            return affiliate;
        });

        List<DocumentRequestDTO> files = new ArrayList<>();
        DocumentRequestDTO doc = new DocumentRequestDTO();
        doc.setFile("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
        doc.setName("test.pdf");
        doc.setIdDocument(1L);
        files.add(doc);

        when(documentNameStandardizationService.getName(any(), any(), any())).thenReturn("test.pdf");
        when(alfrescoService.uploadFileAlfresco(any())).thenReturn(new com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseCreateFolderDTO() {{
            setData(new com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.DataDTO() {{
                setEntry(new com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryResponseDTO() {{
                    setId("docId");
                }});
            }});
        }});
        when(dataDocumentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);


        com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO result = service.stepThree(1L, 1L, 1L, files);

        assertNotNull(result);
        verify(affiliateMercantileRepository, times(2)).save(any());
        verify(dataDocumentRepository).save(any());

    }

    @Test
    @DisplayName("stateAffiliation should reject affiliation")
    void stateAffiliation_reject() {
        StateAffiliation state = new StateAffiliation();
        state.setRejectAffiliation(true);
        state.setComment(List.of("comment"));
        state.setFieldNumber("F123");

        com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile am = new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile();
        am.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(am));
        when(iUserPreRegisterRepository.findById(any())).thenReturn(Optional.of(new UserMain()));
        when(iAffiliateRepository.findByFiledNumber("F123")).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.Affiliate()));

        service.stateAffiliation(am, state);

        verify(affiliateMercantileRepository).save(any());
        verify(timerRepository).save(any());
        verify(sendEmails).requestDeniedDocumentsMercantile(any(), any());
    }

    @Test
    @DisplayName("interviewWeb should reject affiliation")
    void interviewWeb_reject() {
        StateAffiliation state = new StateAffiliation();
        state.setRejectAffiliation(true);
        state.setComment(List.of("comment"));
        state.setFieldNumber("F123");

        com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile am = new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile();
        am.setStageManagement(Constant.SCHEDULING);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(am));
        when(iUserPreRegisterRepository.findById(any())).thenReturn(Optional.of(new UserMain()));

        service.interviewWeb(state);

        verify(affiliateMercantileRepository).save(any());
        verify(sendEmails).requestDeniedDocumentsMercantile(any(), any());
    }

    @Test
    @DisplayName("regularizationDocuments should throw error for invalid stage")
    void regularizationDocuments_invalidStage() {
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile() {{
            setStageManagement(Constant.SING);
        }}));

        assertThrows(AffiliationError.class, () -> service.regularizationDocuments("F123", 1L, 1L, new ArrayList<>()));
    }
}
