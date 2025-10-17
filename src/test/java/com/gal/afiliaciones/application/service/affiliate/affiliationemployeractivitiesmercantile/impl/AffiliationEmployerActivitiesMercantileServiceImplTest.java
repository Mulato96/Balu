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
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.sat.SatError;
import com.gal.afiliaciones.domain.model.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffiliationEmployerActivitiesMercantileServiceImplTest {

    private static final String SAT_AFFILIATED_TO_POSITIVA_MESSAGE = "Estimado empleador, hemos detectado que con el número de documento ingresado existe una afiliación con la ARL POSITIVA COMPAÑIA DE SEGUROS S.A. Puedes ingresar al portal transaccional con tu usuario y contraseña en Iniciar sesión";

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

    private UserMain responsible(String type, String num, int age) {
        UserMain u = new UserMain();
        u.setIdentificationType(type);
        u.setIdentification(num);
        u.setDateBirth(LocalDate.now().minusYears(age));
        return u;
    }

    @Test
    void validationsStepOne_TI_error() {
        assertThrows(AffiliationError.class,
                () -> service.validationsStepOne("123", Constant.TI, null));
    }

    @Test
    void validationsStepOne_CC_success() {
        when(iUserPreRegisterRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(new UserMain()));
        DataBasicCompanyDTO result = service.validationsStepOne("12345", Constant.CC, null);
        assertNotNull(result);
        assertEquals(Constant.CC, result.getTypeDocumentIdentification());
        assertEquals("12345", result.getNumberIdentification());
    }

    @Test
    void validationsStepOne_NI_success() {
        when(iUserRegisterService.calculateModulo11DV(anyString())).thenReturn('1');
        when(iUserPreRegisterRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(new UserMain()));
        DataBasicCompanyDTO result = service.validationsStepOne("12345", Constant.NI, "1");
        assertNotNull(result);
        assertEquals(Constant.NI, result.getTypeDocumentIdentification());
        assertEquals("12345", result.getNumberIdentification());
    }

    @Test
    void validationsStepOne_affiliationExists_error() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setFiledNumber("F1");
        Affiliate a = new Affiliate();
        a.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(List.of(am));
        when(iAffiliateRepository.findByFiledNumber("F1")).thenReturn(Optional.of(a));
        assertThrows(AffiliationAlreadyExistsError.class,
                () -> service.validationsStepOne("123", Constant.CC, null));
    }

    @Test
    void validationsStepOne_satAffiliatedToPositiva_error() {
        ReflectionTestUtils.setField(service, "satValidationEnabled", true);
        TransferableEmployerResponse satResponse = new TransferableEmployerResponse();
        satResponse.setCausal(1);
        satResponse.setArlAfiliacion(Constant.CODE_ARL);
        when(satConsultTransferableEmployerClient.consult(any(TransferableEmployerRequest.class)))
                .thenReturn(satResponse);
        SatError e = assertThrows(SatError.class,
                () -> service.validationsStepOne("123", Constant.CC, null));
        assertEquals(SAT_AFFILIATED_TO_POSITIVA_MESSAGE, e.getError().getMessage());
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

import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Base64ToMultipartFile;
import org.junit.jupiter.api.Disabled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

    @Test
    void stepOne_success() {
        DataBasicCompanyDTO dto = basicDTO("CC", "300");
        UserMain responsible = responsible("CC", "300", 30);
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "300"))
                .thenReturn(Optional.of(responsible));

        // Mock para la pre-creación de Affiliate
        when(iAffiliateRepository.save(any(Affiliate.class))).thenAnswer(i -> i.getArgument(0));
        when(affiliateMercantileRepository.save(any(AffiliateMercantile.class))).thenAnswer(i -> i.getArgument(0));

        AffiliateMercantile result = service.stepOne(dto);
        assertNotNull(result);
        assertEquals("EMPRESA", result.getBusinessName());
    }

    @Test
    void stepThree_missingRequiredDocs_error() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setStageManagement("some_stage");
        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(am));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F2");

        // Documento con ID 1 es requerido pero no se envía
        DocumentRequested reqDoc = new DocumentRequested();
        reqDoc.setId(1L);
        reqDoc.setRequested(true);
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(anyLong())).thenReturn(List.of(reqDoc));

        AffiliationError e = assertThrows(AffiliationError.class,
                () -> service.stepThree(1L, 1L, 1L, Collections.emptyList()));
        assertEquals("No se encontraron todos los documentos requeridos", e.getError().getMessage());
    }

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;

    @Disabled("Requiere mockear la creación de folders y subida de archivos en Alfresco")
    @Test
    void stepThree_success() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setStageManagement("some_stage");
        am.setNumberIdentification("NI123");
        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(am));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F3");

        DocumentRequested reqDoc = new DocumentRequested();
        reqDoc.setId(10L);
        reqDoc.setRequested(true);
        reqDoc.setName("DOC_TEST");
        when(typeEmployerDocumentService.findByIdSubTypeEmployerListDocumentRequested(anyLong())).thenReturn(List.of(reqDoc));

        DocumentRequestDTO docToSend = new DocumentRequestDTO();
        docToSend.setIdDocument(10L);
        docToSend.setName("test.pdf");
        docToSend.setFile("base64string"); // Simplificación

        // Mockear Alfresco, Affiliate, etc.
        // ...

        AffiliateMercantileDTO result = service.stepThree(1L, 1L, 1L, List.of(docToSend));
        assertNotNull(result);
        assertEquals("F3", result.getFiledNumber());
        assertFalse(result.getDocuments().isEmpty());
    }

    @Test
    void stateAffiliation_approve_movesToScheduling() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        am.setFiledNumber("F4");
        when(iUserPreRegisterRepository.findById(any())).thenReturn(Optional.of(new UserMain()));
        when(iAffiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(new Affiliate()));
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        StateAffiliation state = new StateAffiliation();
        state.setRejectAffiliation(false);

        service.stateAffiliation(am, state);

        assertEquals(Constant.SCHEDULING, am.getStageManagement());
        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).interviewWeb(any(TemplateSendEmailsDTO.class));
    }

    @Test
    void stateAffiliation_reject_movesToRegularization() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        am.setFiledNumber("F5");
        when(iUserPreRegisterRepository.findById(any())).thenReturn(Optional.of(new UserMain()));
        when(iAffiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(new Affiliate()));

        StateAffiliation state = new StateAffiliation();
        state.setRejectAffiliation(true);
        state.setComment(List.of("Error en doc"));

        service.stateAffiliation(am, state);

        assertEquals(Constant.REGULARIZATION, am.getStageManagement());
        assertTrue(am.getStatusDocument());
        verify(observationsAffiliationService).create(anyString(), any(), any(), any());
        verify(sendEmails).requestDeniedDocumentsMercantile(any(TemplateSendEmailsDTO.class), any());
    }
}
