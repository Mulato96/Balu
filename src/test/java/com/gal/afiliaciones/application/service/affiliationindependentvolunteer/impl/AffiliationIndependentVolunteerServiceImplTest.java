package com.gal.afiliaciones.application.service.affiliationindependentvolunteer.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class AffiliationIndependentVolunteerServiceImplTest {

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private DangerRepository dangerRepository;
    @Mock
    private FiledService filedService;
    @Mock
    private IDataDocumentRepository dataDocumentRepository;
    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private CollectProperties properties;
    @Mock
    private MessageErrorAge messageError;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;

    @InjectMocks
    private AffiliationIndependentVolunteerServiceImpl affiliationIndependentVolunteerService;

    private AffiliationIndependentVolunteerStep1DTO step1DTO;
    private AffiliationIndependentVolunteerStep2DTO step2DTO;
    private AffiliationIndependentVolunteerStep3DTO step3DTO;
    private List<MultipartFile> documents;

    @BeforeEach
    void setUp() {
        step1DTO = new AffiliationIndependentVolunteerStep1DTO();
        step2DTO = new AffiliationIndependentVolunteerStep2DTO();
        step3DTO = new AffiliationIndependentVolunteerStep3DTO();
        documents = new ArrayList<>();
    }

    @Test
    void validateSalary_ValidSalary_DoesNotThrowException() {
        BigDecimal validSalary = new BigDecimal("1000000");
        int currentYear = LocalDate.now().getYear();
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(900000L);
        when(webClient.getSmlmvByYear(currentYear)).thenReturn(salaryDTO);

        assertDoesNotThrow(() -> {
            try {
                Method method = AffiliationIndependentVolunteerServiceImpl.class.getDeclaredMethod("validateSalary", BigDecimal.class);
                method.setAccessible(true);
                method.invoke(affiliationIndependentVolunteerService, validSalary);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void createAffiliationStep3_ValidInput_ReturnsAffiliation() throws IOException {
        step3DTO.setIdAffiliation(1L);
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentNumber("123456789");
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("FIL0001");
        UserMain user = new UserMain();
        user.setId(1L);
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        when(affiliateRepository.save(any())).thenReturn(affiliate);
        when(alfrescoService.getIdDocumentsFolder(any())).thenReturn(Optional.of(new ConsultFiles()));
        ResponseUploadOrReplaceFilesDTO response = new ResponseUploadOrReplaceFilesDTO();
        response.setIdNewFolder("folder123");
        response.setDocuments(new ArrayList<>());
        when(alfrescoService.uploadAffiliationDocuments(any(), any(), any())).thenReturn(response);
        when(repositoryAffiliation.save(any())).thenReturn(affiliation);

        Affiliation result = affiliationIndependentVolunteerService.createAffiliationStep3(step3DTO, documents);

        assertNotNull(result);
        verify(repositoryAffiliation, times(1)).save(any());
    }

    @Test
    void createAffiliationStep3_AffiliationError_ThrowsAffiliationError() throws IOException {
        step3DTO.setIdAffiliation(1L);
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> affiliationIndependentVolunteerService.createAffiliationStep3(step3DTO, documents));
    }

    @Test
    void isTransferableBySAT_Transferable_ReturnsTrue() {
        String identificationType = "CC";
        String identificationNumber = "123456789";
        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationType);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationNumber);
        ResponseConsultWorkerDTO consultWorkerDTO = new ResponseConsultWorkerDTO();
        consultWorkerDTO.setCausal(3L);
        when(webClient.consultWorkerDTO(any())).thenReturn(consultWorkerDTO);

        Boolean result = affiliationIndependentVolunteerService.isTransferableBySAT(identificationType, identificationNumber);

        assertTrue(result);
    }

    @Test
    void isTransferableBySAT_NotTransferable_ReturnsFalse() {
        String identificationType = "CC";
        String identificationNumber = "123456789";
        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationType);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationNumber);
        ResponseConsultWorkerDTO consultWorkerDTO = new ResponseConsultWorkerDTO();
        consultWorkerDTO.setCausal(1L);
        when(webClient.consultWorkerDTO(any())).thenReturn(consultWorkerDTO);

        Boolean result = affiliationIndependentVolunteerService.isTransferableBySAT(identificationType, identificationNumber);

        assertFalse(result);
    }

    @Test
    void createAffiliationStep3FromPila_ValidInput_ReturnsAffiliation() {
        step3DTO.setIdAffiliation(1L);
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("FIL0001");
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setIdAffiliate(1L);
        when(affiliateRepository.findByFiledNumber("FIL0001")).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.save(any())).thenReturn(affiliate);
        when(repositoryAffiliation.save(any())).thenReturn(affiliation);

        Affiliation result = affiliationIndependentVolunteerService.createAffiliationStep3FromPila(step3DTO);

        assertNotNull(result);
        verify(repositoryAffiliation, times(1)).save(any());
    }

    @Test
    void createAffiliationStep3FromPila_AffiliationError_ThrowsAffiliationError() {
        step3DTO.setIdAffiliation(1L);
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> affiliationIndependentVolunteerService.createAffiliationStep3FromPila(step3DTO));
    }
}
