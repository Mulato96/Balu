package com.gal.afiliaciones.application.service.workermanagement.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.employer.RecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.RecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;


@ExtendWith(MockitoExtension.class)
class WorkerManagementServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    
    @Mock
    private OccupationRepository occupationRepository;
    
    @Mock
    private GenericWebClient webClient;
    
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    
    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;
    
    @Mock
    private RecordMassiveUpdateWorkerService recordMassiveUpdateService;
    
    @Mock
    private CollectProperties properties;
    
    @Mock
    private AlfrescoService alfrescoService;
    
    @Mock
    private RecordMassiveUpdateWorkerRepository recordMassiveUpdateWorkerRepository;
    
    @Mock
    private DetailRecordMassiveUpdateWorkerService detailRecordMassiveService;
    
    @Mock
    private SendEmails sendEmails;
    
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    
    @Mock
    private CertificateService certificateService;
    
    @Mock
    private RetirementRepository retirementRepository;

    @InjectMocks
    private WorkerManagementServiceImpl workerManagementService;

    @Test
    void findDataDependentById_whenDependentNotFound_thenThrowException() {
        // Arrange
        String filedNumber = "FILE123";
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(AffiliationNotFoundError.class, () -> workerManagementService.findDataDependentById(filedNumber));
    }

    @Test
    void findDataDependentById_whenDependentFound_thenReturnData() {
        // Arrange
        String filedNumber = "FILE123";
        
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdentificationDocumentType("TI");
        affiliationDependent.setIdentificationDocumentNumber("123456789");
        
        when(affiliationDependentRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(affiliationDependent));
        
        // Act
        BodyResponseConfig<AffiliationDependent> result = workerManagementService.findDataDependentById(filedNumber);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("TI", result.getData().getIdentificationDocumentType());
    }

    @Test
    void downloadTemplateMassiveUpdate_shouldReturnDocumentFromAlfresco() {
        // Arrange
        when(properties.getIdTemplateMassiveUpdate()).thenReturn("template-123");
        when(alfrescoService.getDocument(anyString())).thenReturn("document-content");
        
        // Act
        String result = workerManagementService.downloadTemplateMassiveUpdate();
        
        // Assert
        assertEquals("document-content", result);
        verify(alfrescoService).getDocument("template-123");
    }

    @Test
    void downloadTemplateGuide_shouldReturnDocumentFromAlfresco() {
        // Arrange
        when(properties.getIdTemplateGuideMassiveUpdate()).thenReturn("guide-123");
        when(alfrescoService.getDocument(anyString())).thenReturn("guide-content");
        
        // Act
        String result = workerManagementService.downloadTemplateGuide();
        
        // Assert
        assertEquals("guide-content", result);
        verify(alfrescoService).getDocument("guide-123");
    }
    
    @Test
    void findAllByIdUser_shouldReturnSortedRecords() {
        // Arrange
        Long userId = 1L;
        RecordMassiveUpdateWorker record1 = new RecordMassiveUpdateWorker();
        record1.setId(2L);
        
        RecordMassiveUpdateWorker record2 = new RecordMassiveUpdateWorker();
        record2.setId(1L);
        
        List<RecordMassiveUpdateWorker> records = Arrays.asList(record1, record2);
        
        when(recordMassiveUpdateWorkerRepository.findAll(any(Specification.class)))
            .thenReturn(records);
        
        // Act
        List<RecordMassiveUpdateWorker> result = workerManagementService.findAllByIdUser(userId);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId()); // Verify reversed order
    }
    
    @Test
    void capitalize_shouldReturnCapitalizedString() {
        // Act & Assert
        assertEquals("Test", WorkerManagementServiceImpl.capitalize("test"));
        assertEquals("Test", WorkerManagementServiceImpl.capitalize("TEST"));
        assertEquals("Test", WorkerManagementServiceImpl.capitalize("tEST"));
        assertEquals("", WorkerManagementServiceImpl.capitalize(""));
        assertEquals("", WorkerManagementServiceImpl.capitalize(null));
    }
    
    @Test
    void generateEmloyerCertificate_whenEmployerNotFound_thenReturnEmptyString() {
        // Arrange
        EmployerCertificateRequestDTO requestDTO = new EmployerCertificateRequestDTO();
        requestDTO.setIdentificationDocumentNumberEmployer("123456");
        requestDTO.setAffiliationTypeEmployer("TYPE");
        requestDTO.setIdAffiliateEmployer(1L);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setDocumenTypeCompany("NI");
        affiliate.setNitCompany("123456");
        affiliate.setAffiliationSubType("TYPE");

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        
        // Act
        String result = workerManagementService.generateEmloyerCertificate(requestDTO);
        
        // Assert
        assertEquals(null, result);
    }
    
    @Test
    void generateEmloyerCertificate_whenEmployerFound_thenReturnCertificate() {
        // Arrange
        EmployerCertificateRequestDTO requestDTO = new EmployerCertificateRequestDTO();
        requestDTO.setIdentificationDocumentTypeEmployer("CC");
        requestDTO.setIdentificationDocumentNumberEmployer("123456");
        requestDTO.setAffiliationTypeEmployer("TYPE");
        requestDTO.setIdAffiliateEmployer(1L);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setDocumenTypeCompany("NI");
        affiliate.setNitCompany("123456");
        affiliate.setAffiliationSubType("TYPE");
        
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        
        when(certificateService.createAndGenerateCertificate(any(FindAffiliateReqDTO.class)))
            .thenReturn("certificate-content");
        
        // Act
        String result = workerManagementService.generateEmloyerCertificate(requestDTO);
        
        // Assert
        assertEquals("certificate-content", result);
    }
}