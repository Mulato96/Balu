package com.gal.afiliaciones.application.service.affiliationindependentcouncillor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.server.ResponseStatusException;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressContractDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressWorkDataCenterDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.InformationIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.mayoraltydependence.MayoraltyDependenceDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;


@ExtendWith(MockitoExtension.class)
public class AffiliationIndependentCouncillorServiceImplTest {

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private FiledService filedService;

    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;

    @Mock
    private IDataDocumentRepository dataDocumentRepository;

    @Mock
    private AlfrescoService alfrescoService;

    @Mock
    private CollectProperties properties;

    @Mock
    private MessageErrorAge messageError;

    @Mock
    private SendEmails sendEmails;

    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;

    @Mock
    private DocumentNameStandardizationService documentNameStandardizationService;

    @Mock
    private GenericWebClient webClient;

    @Mock
    private AffiliateMercantileRepository mercantileRepository;

    @InjectMocks
    private AffiliationIndependentCouncillorServiceImpl service;

    private AffiliationCouncillorStep1DTO step1DTO;
    private AffiliationCouncillorStep2DTO step2DTO;
    private AffiliationCouncillorStep3DTO step3DTO;
    private Affiliation affiliation;
    private UserMain userMain;

    @BeforeEach
    void setUp() {
        // Setup for Step1
        step1DTO = new AffiliationCouncillorStep1DTO();
        step1DTO.setId(0L);
        step1DTO.setIdentificationDocumentType("CC");
        step1DTO.setIdentificationDocumentNumber("12345678");
        step1DTO.setIs723(false);

        ContractorDataStep1DTO contractorDataDTO = new ContractorDataStep1DTO();
        contractorDataDTO.setCurrentARL("ARL_TEST");
        step1DTO.setContractorDataDTO(contractorDataDTO);

        InformationIndependentWorkerDTO workerDTO = new InformationIndependentWorkerDTO();
        workerDTO.setDateOfBirthIndependentWorker(LocalDate.now().minusYears(30));
        workerDTO.setFirstNameIndependentWorker("John");
        workerDTO.setSecondNameIndependentWorker("Doe");
        workerDTO.setSurnameIndependentWorker("Smith");
        workerDTO.setSecondSurnameIndependentWorker("Johnson");
        workerDTO.setAge("30");
        workerDTO.setGender("M");
        workerDTO.setNationalityIndependentWorker(1L);
        workerDTO.setHealthPromotingEntity(1L);
        workerDTO.setPensionFundAdministrator(1L);
        workerDTO.setPhone1IndependentWorker("12345678");
        workerDTO.setPhone2IndependentWorker("87654321");
        workerDTO.setEmailIndependentWorker("test@test.com");

        AddressIndependentWorkerDTO addressDTO = new AddressIndependentWorkerDTO();
        addressDTO.setIdDepartmentIndependentWorker(01L);
        addressDTO.setIdCityIndependentWorker(001L);
        addressDTO.setAddressIndependentWorker("Test Address 123");
        workerDTO.setAddressIndependentWorkerDTO(addressDTO);

        step1DTO.setInformationIndependentWorkerDTO(workerDTO);

        // Setup for Step2
        step2DTO = new AffiliationCouncillorStep2DTO();
        step2DTO.setId(1L);
        step2DTO.setCodeMainEconomicActivity(1234L);

        ContractorDataStep2DTO contractorDataStep2DTO = new ContractorDataStep2DTO();
        contractorDataStep2DTO.setContractMonthlyValue(new BigDecimal("1000000"));
        contractorDataStep2DTO.setContractIbcValue(new BigDecimal("400000"));
        contractorDataStep2DTO.setStartDate(LocalDate.now());
        contractorDataStep2DTO.setEndDate(LocalDate.now().plusMonths(6));
        contractorDataStep2DTO.setDuration("6");

        AddressContractDataStep2DTO addressContractDTO = new AddressContractDataStep2DTO();
        contractorDataStep2DTO.setAddressContractDataStep2DTO(addressContractDTO);
        step2DTO.setContractorDataStep2DTO(contractorDataStep2DTO);

        AddressWorkDataCenterDTO addressWorkDTO = new AddressWorkDataCenterDTO();
        step2DTO.setAddressWorkDataCenterDTO(addressWorkDTO);

        // Setup for Step3
        step3DTO = new AffiliationCouncillorStep3DTO();
        step3DTO.setId(1L);

        // Setup for affiliation
        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("12345678");
        affiliation.setCompanyName("Test Company");

        // Setup for userMain
        userMain = new UserMain();
        userMain.setId(1L);
        userMain.setIdentification("12345678");
        userMain.setPensionFundAdministrator(1L);
        userMain.setHealthPromotingEntity(1L);
    }

    @Test
    void findAllMayoraltyDependence_Success() {
        // Arrange
        String nit = "123456789";
        
        AffiliateMercantile mercantile1 = new AffiliateMercantile();
        mercantile1.setNumberIdentification(nit);
        mercantile1.setDigitVerificationDV(1);
        mercantile1.setBusinessName("Test Business 1");
        mercantile1.setDecentralizedConsecutive(123L);
        
        AffiliateMercantile mercantile2 = new AffiliateMercantile();
        mercantile2.setNumberIdentification(nit);
        mercantile2.setDigitVerificationDV(2);
        mercantile2.setBusinessName("Test Business 2");
        mercantile2.setDecentralizedConsecutive(456L);
        
        List<AffiliateMercantile> mercantileList = Arrays.asList(mercantile1, mercantile2);
        
        when(mercantileRepository.findAll(any(Specification.class))).thenReturn(mercantileList);

        // Act
        List<MayoraltyDependenceDTO> result = service.findAllMayoraltyDependence(nit);

        // Assert
        assertEquals(2, result.size());
        assertEquals(nit, result.get(0).getNit());
        assertEquals(1, result.get(0).getDv());
        assertEquals("Test Business 1", result.get(0).getName());
        assertEquals(123L, result.get(0).getDecentralizedConsecutive());
        verify(mercantileRepository).findAll(any(Specification.class));
    }

    @Test
    void createAffiliationStep2_AffiliationNotFound() {
        // Arrange
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> service.createAffiliationStep2(step2DTO));
        assertEquals("404 NOT_FOUND \"Afiliación no encontrada\"", exception.getMessage());
    }

    @Test
    void createAffiliationStep3_Success() throws IOException {
        // Arrange
        List<MultipartFile> documents = new ArrayList<>();
        MultipartFile mockFile = mock(MultipartFile.class);
        documents.add(mockFile);
        
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("12345");
        
        ConsultFiles consultFiles = new ConsultFiles();
        
        ResponseUploadOrReplaceFilesDTO uploadResponse = new ResponseUploadOrReplaceFilesDTO();
        uploadResponse.setIdNewFolder("folder123");
        
        ReplacedDocumentDTO documentDTO = new ReplacedDocumentDTO();
        documentDTO.setDocumentId("doc123");
        documentDTO.setDocumentName("document.pdf");
        uploadResponse.setDocuments(Arrays.asList(documentDTO));
        
        when(repositoryAffiliation.findById(anyLong())).thenReturn(Optional.of(affiliation));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("12345");
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.of(consultFiles));
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(uploadResponse);
        when(documentNameStandardizationService.getName(anyString(), anyString(), anyString())).thenReturn("standardized_name.pdf");
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(affiliation);
        when(properties.getAffiliationCouncillorFolderId()).thenReturn("councilFolder");
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn("serviceFolder");

        // Act
        AffiliationCouncillorStep3DTO result = service.createAffiliationStep3(step3DTO, documents);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345", result.getFiledNumber());
        verify(repositoryAffiliation).findById(1L);
        verify(filedService).getNextFiledNumberAffiliation();
        verify(userPreRegisterRepository).findOne(any(Specification.class));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(alfrescoService).getIdDocumentsFolder(anyString());
        verify(alfrescoService).uploadAffiliationDocuments(anyString(), anyString(), anyList());
        verify(dataDocumentRepository).save(any(DataDocumentAffiliate.class));
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

}