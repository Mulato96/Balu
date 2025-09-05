package com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.ErrorAffiliationProvisionService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressContractDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressWorkDataCenterDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.InformationIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.validation.AffiliationValidations;


@ExtendWith(MockitoExtension.class)
class AffiliationEmployerProvisionServiceIndependentServiceImplTest {

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    
    @Mock
    private FiledService filedService;
    
    @Mock
    private AlfrescoService alfrescoService;
    
    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;
    
    @Mock
    private IDataDocumentRepository dataDocumentRepository;
    
    @Mock
    private AffiliateRepository affiliateRepository;
    
    @Mock
    private CollectProperties properties;
    
    @Mock
    private MessageErrorAge messageError;
    
    @Mock
    private SendEmails sendEmails;
    
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;
    
    @Mock
    private GenericWebClient webClient;

    @InjectMocks
    private AffiliationEmployerProvisionServiceIndependentServiceImpl service;

    private ProvisionServiceAffiliationStep1DTO step1DTO;
    private ProvisionServiceAffiliationStep2DTO step2DTO;
    private ProvisionServiceAffiliationStep3DTO step3DTO;
    private Affiliation affiliation;
    private UserMain userMain;

    @BeforeEach
    void setUp() {
        // Configure step1DTO
        step1DTO = new ProvisionServiceAffiliationStep1DTO();
        ContractorDataStep1DTO contractorDataDTO = new ContractorDataStep1DTO();
        contractorDataDTO.setCompanyName("Test Company");
        contractorDataDTO.setIdentificationDocumentTypeLegalRepresentative("CC");
        contractorDataDTO.setIdentificationDocumentNumberContractorLegalRepresentative("12345678");
        contractorDataDTO.setFirstNameContractor("John");
        contractorDataDTO.setSurnameContractor("Doe");
        contractorDataDTO.setEmailContractor("john.doe@example.com");
        contractorDataDTO.setCurrentARL("Test ARL");
        step1DTO.setContractorDataDTO(contractorDataDTO);
        
        InformationIndependentWorkerDTO workerDTO = new InformationIndependentWorkerDTO();
        workerDTO.setFirstNameIndependentWorker("Jane");
        workerDTO.setSurnameIndependentWorker("Smith");
        workerDTO.setEmailIndependentWorker("jane.smith@example.com");
        workerDTO.setHealthPromotingEntity(1L);
        workerDTO.setPensionFundAdministrator(1L);
        
        AddressIndependentWorkerDTO addressDTO = new AddressIndependentWorkerDTO();
        addressDTO.setIdDepartmentIndependentWorker(01L);
        addressDTO.setIdCityIndependentWorker(001L);
        addressDTO.setAddressIndependentWorker("123 Main St");
        workerDTO.setAddressIndependentWorkerDTO(addressDTO);
        
        step1DTO.setInformationIndependentWorkerDTO(workerDTO);
        step1DTO.setIdentificationDocumentType("CC");
        step1DTO.setIdentificationDocumentNumber("87654321");
        step1DTO.setIs723(false);
        step1DTO.setId(0L);
        
        // Configure step2DTO
        step2DTO = new ProvisionServiceAffiliationStep2DTO();
        step2DTO.setId(1L);
        
        ContractorDataStep2DTO contractorDataStep2DTO = new ContractorDataStep2DTO();
        contractorDataStep2DTO.setStartDate(LocalDate.of(2023, 01, 01));
        contractorDataStep2DTO.setEndDate(LocalDate.of(2023, 12, 31));
        contractorDataStep2DTO.setDuration("12 months");
        contractorDataStep2DTO.setContractMonthlyValue(new BigDecimal("2000000"));
        contractorDataStep2DTO.setContractIbcValue(new BigDecimal("800000"));
        
        AddressContractDataStep2DTO addressContractDTO = new AddressContractDataStep2DTO();
        contractorDataStep2DTO.setAddressContractDataStep2DTO(addressContractDTO);
        step2DTO.setContractorDataStep2DTO(contractorDataStep2DTO);
        
        AddressWorkDataCenterDTO addressWorkDTO = new AddressWorkDataCenterDTO();
        step2DTO.setAddressWorkDataCenterDTO(addressWorkDTO);
        step2DTO.setCodeMainEconomicActivity(1234L);
        
        // Configure step3DTO
        step3DTO = new ProvisionServiceAffiliationStep3DTO();
        step3DTO.setId(1L);
        
        // Configure affiliation
        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("87654321");
        affiliation.setCompanyName("Test Company");
        
        // Configure userMain
        userMain = new UserMain();
        userMain.setId(1L);
        userMain.setIdentificationType("CC");
        userMain.setIdentification("87654321");
        userMain.setDateBirth(LocalDate.now().minusYears(30));
    }

    @Test
    void createAffiliationProvisionServiceStep1_Success() {
        // Arrange
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
            .thenReturn(Optional.of(userMain));
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(65);
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(affiliation);
        
        try (MockedStatic<AffiliationValidations> mockedValidations = Mockito.mockStatic(AffiliationValidations.class)) {
            // Act
            ProvisionServiceAffiliationStep1DTO result = service.createAffiliationProvisionServiceStep1(step1DTO);
            
            // Assert
            assertNotNull(result);
            assertEquals(affiliation.getId(), result.getId());
            verify(repositoryAffiliation).save(any(Affiliation.class));
            verify(userPreRegisterRepository).save(any(UserMain.class));
        }
    }
    
    @Test
    void createAffiliationProvisionServiceStep1_WithExistingId_Success() {
        // Arrange
        step1DTO.setId(1L);
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
            .thenReturn(Optional.of(userMain));
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(65);
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.of(affiliation));
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(affiliation);
        
        try (MockedStatic<AffiliationValidations> mockedValidations = Mockito.mockStatic(AffiliationValidations.class)) {
            // Act
            ProvisionServiceAffiliationStep1DTO result = service.createAffiliationProvisionServiceStep1(step1DTO);
            
            // Assert
            assertNotNull(result);
            assertEquals(affiliation.getId(), result.getId());
            verify(repositoryAffiliation).findById(1L);
            verify(repositoryAffiliation).save(any(Affiliation.class));
        }
    }
        
    @Test
    void createAffiliationProvisionServiceStep2_AffiliationNotFound() {
        // Arrange
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createAffiliationProvisionServiceStep2(step2DTO));
    }

    @Test
    void createAffiliationProvisionServiceStep2_InvalidMonthlyValue() {
        // Arrange
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000000L); // Minimum wage = 1,000,000

        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.of(affiliation));
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        // Set a monthly contract value below the minimum wage to trigger validation error
        step2DTO.getContractorDataStep2DTO().setContractMonthlyValue(new BigDecimal("500000"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createAffiliationProvisionServiceStep2(step2DTO));
    }

    @Test
    void createAffiliationProvisionServiceStep3_Success() throws IOException {
        // Arrange
        List<MultipartFile> documents = new ArrayList<>();
        String filedNumber = "RAD123456";
        ConsultFiles consultFiles = new ConsultFiles();
        ResponseUploadOrReplaceFilesDTO responseDTO = new ResponseUploadOrReplaceFilesDTO();
        responseDTO.setIdNewFolder("folder123");
        List<ReplacedDocumentDTO> replacedDocuments = new ArrayList<>();
        ReplacedDocumentDTO replacedDocument = new ReplacedDocumentDTO();
        replacedDocument.setDocumentId("doc123");
        replacedDocument.setDocumentName("document.pdf");
        replacedDocuments.add(replacedDocument);
        responseDTO.setDocuments(replacedDocuments);
        
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.of(affiliation));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn(filedNumber);
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.of(consultFiles));
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(responseDTO);
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(affiliation);
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn("folderID");
        
        // Act
        ProvisionServiceAffiliationStep3DTO result = service.createAffiliationProvisionServiceStep3(step3DTO, documents);
        
        // Assert
        assertNotNull(result);
        assertEquals(affiliation.getId(), result.getId());
        assertEquals(filedNumber, result.getFiledNumber());
        verify(repositoryAffiliation).save(any(Affiliation.class));
        verify(dataDocumentRepository).save(any(DataDocumentAffiliate.class));
    }
    
    @Test
    void createAffiliationProvisionServiceStep3_AffiliationNotFound() {
        // Arrange
        List<MultipartFile> documents = new ArrayList<>();
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            service.createAffiliationProvisionServiceStep3(step3DTO, documents));
    }
        
    @Test
    void createProvisionServiceStep3FromPila_AffiliationNotFound() {
        // Arrange
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            service.createProvisionServiceStep3FromPila(step3DTO));
    }
    
    @Test
    void validateContractorData_Success() {
        // No need for additional setup as step1DTO is already configured properly
        
        // Act
        try (MockedStatic<AffiliationValidations> mockedValidations = Mockito.mockStatic(AffiliationValidations.class)) {
            service.validateContractorData(step1DTO);
            
            // Assert - no exception thrown
            mockedValidations.verify(() -> AffiliationValidations.validateArl(anyString(), anyBoolean()));
        }
    }
    
    @Test
    void validateContractorData_NullData() {
        // Act & Assert
        assertThrows(ErrorAffiliationProvisionService.class, () -> 
            service.validateContractorData(null));
    }
    
    @Test
    void validateContractorData_InvalidCompanyName() {
        // Arrange
        step1DTO.getContractorDataDTO().setCompanyName("SIN NOMBRE");
        
        // Act & Assert
        assertThrows(ErrorAffiliationProvisionService.class, () -> 
            service.validateContractorData(step1DTO));
    }
    
    @Test
    void validateContractorData_InvalidEmail() {
        // Arrange
        step1DTO.getContractorDataDTO().setEmailContractor("invalid-email");
        
        // Act & Assert
        assertThrows(ErrorAffiliationProvisionService.class, () -> 
            service.validateContractorData(step1DTO));
    }
    
    @Test
    void isValidEmail_ValidEmail() {
        // Act & Assert
        assertTrue(service.isValidEmail("valid@example.com"));
    }
    
    @Test
    void isValidEmail_InvalidEmail() {
        // Act & Assert
        assertFalse(service.isValidEmail("invalid-email"));
        assertFalse(service.isValidEmail(null));
    }
}