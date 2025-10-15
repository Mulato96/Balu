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
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
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

}
