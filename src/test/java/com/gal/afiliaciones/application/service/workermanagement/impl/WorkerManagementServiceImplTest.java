package com.gal.afiliaciones.application.service.workermanagement.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.domain.model.affiliate.TraceabilityOfficialUpdates;
import com.gal.afiliaciones.infrastructure.dao.repository.traceability.TraceabilityOfficialUpdatesRepository;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchFilterDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchResponseDTO;
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
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;


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

    @Mock
    private RegistraduriaUnifiedService registraduriaUnifiedService;

    @Mock
    private TraceabilityOfficialUpdatesRepository traceRepository;

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

    @Test
    void getWorkersByDocument_whenOnlyDependentWorkersFound_thenReturnDependentWorkers() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("CC");
        filter.setIdentificationDocumentNumber("123456789");

        WorkerSearchResponseDTO dependentWorker = new WorkerSearchResponseDTO();
        dependentWorker.setIdentificationDocumentType("CC");
        dependentWorker.setIdentificationDocumentNumber("123456789");
        dependentWorker.setCompleteName("Prue");
        dependentWorker.setOccupation("Developer");
        dependentWorker.setAffiliationType("Dependiente");
        dependentWorker.setCompany("Test Company");
        dependentWorker.setCoverageDate("2024-01-01");

        List<WorkerSearchResponseDTO> dependentWorkers = Arrays.asList(dependentWorker);

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(dependentWorkers);

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(Collections.emptyList());


        List<WorkerSearchResponseDTO> result = workerManagementService.getWorkersByDocument(filter);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Prue", result.get(0).getCompleteName());
        assertEquals("Developer", result.get(0).getOccupation());
        assertEquals("Dependiente", result.get(0).getAffiliationType());
        assertEquals("Test Company", result.get(0).getCompany());
        assertEquals("2024-01-01", result.get(0).getCoverageDate());

        verify(affiliateRepository).searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
        verify(affiliateRepository).searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
    }

    @Test
    void getWorkersByDocument_whenOnlyIndependentWorkersFound_thenReturnIndependentWorkers() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("CC");
        filter.setIdentificationDocumentNumber("987654321");

        WorkerSearchResponseDTO independentWorker = new WorkerSearchResponseDTO();
        independentWorker.setIdentificationDocumentType("CC");
        independentWorker.setIdentificationDocumentNumber("987654321");
        independentWorker.setCompleteName("Prue");
        independentWorker.setOccupation(null);
        independentWorker.setAffiliationType("Independiente");
        independentWorker.setCompany("Test Company");
        independentWorker.setCoverageDate("2024-01-01");

        List<WorkerSearchResponseDTO> independentWorkers = Arrays.asList(independentWorker);

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(Collections.emptyList());

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(independentWorkers);


        List<WorkerSearchResponseDTO> result = workerManagementService.getWorkersByDocument(filter);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Prue", result.get(0).getCompleteName());
        assertEquals(null, result.get(0).getOccupation());
        assertEquals("Independiente", result.get(0).getAffiliationType());
        assertEquals("Test Company", result.get(0).getCompany());
        assertEquals("2024-01-01", result.get(0).getCoverageDate());

        verify(affiliateRepository).searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
        verify(affiliateRepository).searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
    }

    @Test
    void getWorkersByDocument_whenBothTypesFound_thenReturnUnifiedList() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("CC");
        filter.setIdentificationDocumentNumber("555555555");

        WorkerSearchResponseDTO dependentWorker = new WorkerSearchResponseDTO();
        dependentWorker.setIdentificationDocumentType("CC");
        dependentWorker.setIdentificationDocumentNumber("555555555");
        dependentWorker.setCompleteName("Prue");
        dependentWorker.setOccupation("Analyst");
        dependentWorker.setAffiliationType("Dependiente");
        dependentWorker.setCompany("Test Company");
        dependentWorker.setCoverageDate("2024-01-01");

        WorkerSearchResponseDTO independentWorker = new WorkerSearchResponseDTO();
        independentWorker.setIdentificationDocumentType("CC");
        independentWorker.setIdentificationDocumentNumber("555555555");
        independentWorker.setCompleteName("Prue");
        independentWorker.setOccupation(null);
        independentWorker.setAffiliationType("Independiente");
        independentWorker.setCompany("Test Company");
        independentWorker.setCoverageDate("2024-01-01");

        List<WorkerSearchResponseDTO> dependentWorkers = Arrays.asList(dependentWorker);
        List<WorkerSearchResponseDTO> independentWorkers = Arrays.asList(independentWorker);

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(dependentWorkers);

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(independentWorkers);


        List<WorkerSearchResponseDTO> result = workerManagementService.getWorkersByDocument(filter);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dependiente", result.get(0).getAffiliationType());
        assertEquals("Independiente", result.get(1).getAffiliationType());

        verify(affiliateRepository).searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
        verify(affiliateRepository).searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
    }

    @Test
    void getWorkersByDocument_whenNoWorkersFound_thenThrowNotFoundWorkersException() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("CC");
        filter.setIdentificationDocumentNumber("000000000");

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(Collections.emptyList());

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(Collections.emptyList());


        assertThrows(com.gal.afiliaciones.config.ex.workermanagement.NotFoundWorkersException.class,
                () -> workerManagementService.getWorkersByDocument(filter));

        verify(affiliateRepository).searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
        verify(affiliateRepository).searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        );
    }

    @Test
    void getWorkersByDocument_whenDependentListIsNull_thenReturnOnlyIndependentWorkers() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("CE");
        filter.setIdentificationDocumentNumber("111111111");

        WorkerSearchResponseDTO independentWorker = new WorkerSearchResponseDTO();
        independentWorker.setIdentificationDocumentType("CE");
        independentWorker.setIdentificationDocumentNumber("111111111");
        independentWorker.setCompleteName("Prue");
        independentWorker.setCompany("Test Company");
        independentWorker.setCoverageDate("2024-01-01");

        List<WorkerSearchResponseDTO> independentWorkers = Arrays.asList(independentWorker);

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(null);

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(independentWorkers);


        List<WorkerSearchResponseDTO> result = workerManagementService.getWorkersByDocument(filter);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Prue", result.get(0).getCompleteName());
    }

    @Test
    void getWorkersByDocument_whenIndependentListIsNull_thenReturnOnlyDependentWorkers() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("TI");
        filter.setIdentificationDocumentNumber("222222222");

        WorkerSearchResponseDTO dependentWorker = new WorkerSearchResponseDTO();
        dependentWorker.setIdentificationDocumentType("TI");
        dependentWorker.setIdentificationDocumentNumber("222222222");
        dependentWorker.setCompleteName("Prue");
        dependentWorker.setCompany("Test Company");
        dependentWorker.setCoverageDate("2024-01-01");

        List<WorkerSearchResponseDTO> dependentWorkers = Arrays.asList(dependentWorker);

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(dependentWorkers);

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(null);


        List<WorkerSearchResponseDTO> result = workerManagementService.getWorkersByDocument(filter);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Prue", result.get(0).getCompleteName());
    }

    @Test
    void getWorkersByDocument_whenBothListsAreNull_thenThrowNotFoundWorkersException() {

        WorkerSearchFilterDTO filter = new WorkerSearchFilterDTO();
        filter.setIdentificationDocumentType("PA");
        filter.setIdentificationDocumentNumber("333333333");

        when(affiliateRepository.searchWorkersDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(null);

        when(affiliateRepository.searchWorkersInDependent(
                filter.getIdentificationDocumentType(),
                filter.getIdentificationDocumentNumber()
        )).thenReturn(null);


        assertThrows(com.gal.afiliaciones.config.ex.workermanagement.NotFoundWorkersException.class,
                () -> workerManagementService.getWorkersByDocument(filter));
    }

    @Test
    void updateWorkerCoverageDate_whenDependentWorker_thenUpdateSuccessfully() {
        Long affiliateId = 1L;
        Long employerId = 100L;
        LocalDate newDate = LocalDate.of(2024, 1, 15);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);
        LocalDate employerCoverageDate = LocalDate.of(2024, 1, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("12345");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(employerCoverageDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenReturn(dependent);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        UpdateWorkerCoverageDateResponseDTO response = workerManagementService.updateWorkerCoverageDate(dto);

        assertNotNull(response);
        assertEquals(true, response.isSuccess());
        assertEquals("Fecha de cobertura actualizada exitosamente", response.getMessage());
        assertEquals(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT, response.getWorkerType());
        assertEquals(previousDate, response.getPreviousDate());
        assertEquals(newDate, response.getNewDate());
        assertEquals("12345", response.getFiledNumber());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliateRepository).findByIdAffiliate(employerId);
        verify(affiliationDependentRepository).save(any(AffiliationDependent.class));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }

    @Test
    void updateWorkerCoverageDate_whenIndependentWorker_thenUpdateSuccessfully() {
        Long affiliateId = 2L;
        LocalDate newDate = LocalDate.of(2024, 2, 1);
        LocalDate previousDate = LocalDate.of(2024, 1, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("independent.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("67890");
        affiliate.setAffiliationStatus("Activa");
        affiliate.setCoverageStartDate(previousDate);
        affiliate.setAffiliationDate(LocalDate.of(2023, 6, 1).atStartOfDay());

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        UpdateWorkerCoverageDateResponseDTO response = workerManagementService.updateWorkerCoverageDate(dto);

        assertNotNull(response);
        assertEquals(true, response.isSuccess());
        assertEquals("Fecha de cobertura actualizada exitosamente", response.getMessage());
        assertEquals(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT, response.getWorkerType());
        assertEquals(previousDate, response.getPreviousDate());
        assertEquals(newDate, response.getNewDate());
        assertEquals("67890", response.getFiledNumber());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }

    @Test
    void updateWorkerCoverageDate_whenAffiliateNotFound_thenThrowException() {
        Long affiliateId = 999L;
        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(LocalDate.now());
        dto.setUser("test.user@example.com");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateWorkerCoverageDate_whenInvalidAffiliationType_thenThrowException() {
        Long affiliateId = 3L;
        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(LocalDate.now());
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType("TipoInvalido");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateWorkerCoverageDate_whenDependentNotFound_thenThrowException() {
        Long affiliateId = 4L;
        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(LocalDate.of(2024, 1, 15));
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("11111");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationDependentRepository).findOne(any(Specification.class));
    }

    @Test
    void updateWorkerCoverageDate_whenIndependentNotActive_thenThrowException() {
        Long affiliateId = 6L;
        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(LocalDate.of(2024, 2, 1));
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus("Inactiva");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateWorkerCoverageDate_whenDateBeforeContractStart_thenThrowException() {
        Long affiliateId = 7L;
        LocalDate newDate = LocalDate.of(2023, 1, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus("Activa");
        affiliate.setAffiliationDate(LocalDate.of(2023, 6, 1).atStartOfDay());

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateWorkerCoverageDate_whenDependentWorker_verifyTraceabilitySaved() {
        Long affiliateId = 8L;
        Long employerId = 200L;
        LocalDate newDate = LocalDate.of(2024, 3, 1);
        LocalDate previousDate = LocalDate.of(2024, 1, 1);
        LocalDate employerCoverageDate = LocalDate.of(2024, 2, 1);
        String userName = "auditor@example.com";

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser(userName);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("TRACE-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(employerCoverageDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenReturn(dependent);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenAnswer(invocation -> {
            TraceabilityOfficialUpdates trace = invocation.getArgument(0);
            assertEquals(userName, trace.getUpdateBy());
            assertEquals(affiliateId, trace.getIdAffiliate());
            assertNotNull(trace.getModifyType());
            assertEquals(LocalDate.now(), trace.getUpdateDate());
            return trace;
        });

        workerManagementService.updateWorkerCoverageDate(dto);

        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }

    @Test
    void updateWorkerCoverageDate_whenIndependentWorker_verifyTraceabilitySaved() {
        Long affiliateId = 9L;
        LocalDate newDate = LocalDate.of(2024, 4, 1);
        LocalDate previousDate = LocalDate.of(2024, 1, 1);
        String userName = "admin@example.com";

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser(userName);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("TRACE-002");
        affiliate.setAffiliationStatus("Activa");
        affiliate.setCoverageStartDate(previousDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenAnswer(invocation -> {
            TraceabilityOfficialUpdates trace = invocation.getArgument(0);
            assertEquals(userName, trace.getUpdateBy());
            assertEquals(affiliateId, trace.getIdAffiliate());
            assertNotNull(trace.getModifyType());
            return trace;
        });

        workerManagementService.updateWorkerCoverageDate(dto);

        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }

    @Test
    void updateWorkerCoverageDate_whenDependentWorker_verifyAffiliateCoverageStartDateUpdated() {
        Long affiliateId = 10L;
        Long employerId = 300L;
        LocalDate newDate = LocalDate.of(2024, 5, 1);
        LocalDate employerCoverageDate = LocalDate.of(2024, 4, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("verify@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("VERIFY-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(LocalDate.of(2024, 1, 1));
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(employerCoverageDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenReturn(dependent);
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(invocation -> {
            Affiliate saved = invocation.getArgument(0);
            assertEquals(newDate, saved.getCoverageStartDate());
            return saved;
        });
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        workerManagementService.updateWorkerCoverageDate(dto);

        verify(affiliateRepository).save(any(Affiliate.class));
    }

    @Test
    void updateWorkerCoverageDate_whenDependentWithoutEmployer_thenUpdateSuccessfully() {
        Long affiliateId = 11L;
        LocalDate newDate = LocalDate.of(2024, 1, 15);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("NO-EMPLOYER-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(null); // Sin empleador

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenReturn(dependent);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        UpdateWorkerCoverageDateResponseDTO response = workerManagementService.updateWorkerCoverageDate(dto);

        assertNotNull(response);
        assertEquals(true, response.isSuccess());
        verify(affiliateRepository, never()).findByIdAffiliate(anyLong());
    }

    @Test
    void updateWorkerCoverageDate_whenEmployerCoverageDateIsNull_thenThrowException() {
        Long affiliateId = 12L;
        Long employerId = 400L;
        LocalDate newDate = LocalDate.of(2024, 1, 15);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("NULL-EMPLOYER-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(null); // Fecha null

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));

        AffiliationError exception = assertThrows(AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        assertEquals("La fecha de cobertura del dependiente no puede ser anterior a la fecha de cobertura del empleador.",
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliateRepository).findByIdAffiliate(employerId);
        verify(affiliationDependentRepository, never()).save(any(AffiliationDependent.class));
    }

    @Test
    void updateWorkerCoverageDate_whenNewDateBeforeEmployerCoverageDate_thenThrowException() {
        Long affiliateId = 13L;
        Long employerId = 500L;
        LocalDate newDate = LocalDate.of(2024, 1, 10);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);
        LocalDate employerCoverageDate = LocalDate.of(2024, 1, 15);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("BEFORE-EMPLOYER-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(employerCoverageDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));

        AffiliationError exception = assertThrows(AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        assertEquals("La fecha de cobertura del dependiente no puede ser anterior a la fecha de cobertura del empleador.",
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliateRepository).findByIdAffiliate(employerId);
        verify(affiliationDependentRepository, never()).save(any(AffiliationDependent.class));
    }

    @Test
    void updateWorkerCoverageDate_whenEmployerNotFound_thenThrowException() {
        Long affiliateId = 14L;
        Long employerId = 600L;
        LocalDate newDate = LocalDate.of(2024, 1, 15);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("NOT-FOUND-EMPLOYER-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class,
                () -> workerManagementService.updateWorkerCoverageDate(dto));

        assertEquals("Empleador no encontrado para el dependiente.", exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliateRepository).findByIdAffiliate(employerId);
        verify(affiliationDependentRepository, never()).save(any(AffiliationDependent.class));
    }

    @Test
    void updateWorkerCoverageDate_whenNewDateEqualsEmployerCoverageDate_thenUpdateSuccessfully() {
        Long affiliateId = 15L;
        Long employerId = 700L;
        LocalDate newDate = LocalDate.of(2024, 1, 15);
        LocalDate previousDate = LocalDate.of(2023, 12, 1);
        LocalDate employerCoverageDate = LocalDate.of(2024, 1, 15); // Misma fecha

        UpdateWorkerCoverageDateDTO dto = new UpdateWorkerCoverageDateDTO();
        dto.setIdAffiliate(affiliateId);
        dto.setNewCoverageDate(newDate);
        dto.setUser("test.user@example.com");

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("EQUAL-DATE-001");

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setIdAffiliate(affiliateId);
        dependent.setCoverageDate(previousDate);
        dependent.setIdAffiliateEmployer(employerId);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(employerId);
        employerAffiliate.setCoverageStartDate(employerCoverageDate);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        when(affiliateRepository.findByIdAffiliate(employerId)).thenReturn(Optional.of(employerAffiliate));
        when(affiliationDependentRepository.save(any(AffiliationDependent.class))).thenReturn(dependent);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        UpdateWorkerCoverageDateResponseDTO response = workerManagementService.updateWorkerCoverageDate(dto);

        assertNotNull(response);
        assertEquals(true, response.isSuccess());
        assertEquals(newDate, response.getNewDate());
        verify(affiliateRepository).findByIdAffiliate(employerId);
    }


    @Test
    void getWorkerDetail_whenIndependentWorker_thenReturnWorkerDetail() {
        Long affiliateId = 100L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("IND-001");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("1234567890");
        affiliate.setCoverageStartDate(LocalDate.of(2024, 1, 15));

        WorkerDetailDTO expectedDTO = WorkerDetailDTO.builder()
                .idAffiliate(affiliateId)
                .filedNumber("IND-001")
                .identificationDocumentType("CC")
                .identificationDocumentNumber("1234567890")
                .completeName("JUAN CARLOS PEREZ GOMEZ")
                .contractType("Civil")
                .contractQuality("Contratista")
                .contractTransport(false)
                .journeyEstablishment("Tiempo completo")
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("12")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .build();

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findWorkerDetailByAffiliateId(affiliateId))
                .thenReturn(Optional.of(expectedDTO));

        WorkerDetailDTO result = workerManagementService.getWorkerDetail(affiliateId);

        assertNotNull(result);
        assertEquals(affiliateId, result.getIdAffiliate());
        assertEquals("IND-001", result.getFiledNumber());
        assertEquals("CC", result.getIdentificationDocumentType());
        assertEquals("1234567890", result.getIdentificationDocumentNumber());
        assertEquals("JUAN CARLOS PEREZ GOMEZ", result.getCompleteName());
        assertEquals("Civil", result.getContractType());
        assertEquals("Contratista", result.getContractQuality());
        assertEquals(false, result.getContractTransport());
        assertEquals("Tiempo completo", result.getJourneyEstablishment());
        assertEquals(LocalDate.of(2024, 1, 15), result.getContractStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), result.getContractEndDate());
        assertEquals("12", result.getContractDuration());
        assertEquals(LocalDate.of(2024, 1, 15), result.getCoverageDate());
        assertEquals(java.math.BigDecimal.valueOf(57600000.00), result.getContractTotalValue());
        assertEquals(java.math.BigDecimal.valueOf(4800000.00), result.getContractMonthlyValue());
        assertEquals(java.math.BigDecimal.valueOf(1920000.00), result.getContractIbcValue());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationRepository).findWorkerDetailByAffiliateId(affiliateId);
    }

    @Test
    void getWorkerDetail_whenDependentWorker_thenReturnWorkerDetail() {
        Long affiliateId = 200L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("DEP-001");

        WorkerDetailDTO expectedDTO = WorkerDetailDTO.builder()
                .idAffiliate(affiliateId)
                .filedNumber("DEP-001")
                .identificationDocumentType("CC")
                .identificationDocumentNumber("1054541549")
                .completeName("MARIA FERNANDA LOPEZ GARCIA")
                .contractType("Termino Indefinido")
                .contractQuality("Trabajador")
                .contractTransport(true)
                .journeyEstablishment("Ordinaria")
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(42000000.00))
                .contractMonthlyValue(null)
                .contractIbcValue(java.math.BigDecimal.valueOf(3500000.00))
                .build();

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findWorkerDetailByAffiliateId(affiliateId))
                .thenReturn(Optional.of(expectedDTO));

        WorkerDetailDTO result = workerManagementService.getWorkerDetail(affiliateId);

        assertNotNull(result);
        assertEquals(affiliateId, result.getIdAffiliate());
        assertEquals("DEP-001", result.getFiledNumber());
        assertEquals("CC", result.getIdentificationDocumentType());
        assertEquals("1054541549", result.getIdentificationDocumentNumber());
        assertEquals("MARIA FERNANDA LOPEZ GARCIA", result.getCompleteName());
        assertEquals("Termino Indefinido", result.getContractType());
        assertEquals("Trabajador", result.getContractQuality());
        assertEquals(true, result.getContractTransport());
        assertEquals("Ordinaria", result.getJourneyEstablishment());
        assertEquals(LocalDate.of(2024, 1, 15), result.getContractStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), result.getContractEndDate());
        assertEquals("11 MESES", result.getContractDuration());
        assertEquals(LocalDate.of(2024, 1, 15), result.getCoverageDate());
        assertEquals(java.math.BigDecimal.valueOf(42000000.00), result.getContractTotalValue());
        assertEquals(null, result.getContractMonthlyValue());  // null para dependientes
        assertEquals(java.math.BigDecimal.valueOf(3500000.00), result.getContractIbcValue());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationDependentRepository).findWorkerDetailByAffiliateId(affiliateId);
    }

    @Test
    void getWorkerDetail_whenAffiliateNotFound_thenThrowAffiliateNotFound() {
        Long affiliateId = 999L;

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> {
            workerManagementService.getWorkerDetail(affiliateId);
        });

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void getWorkerDetail_whenUnsupportedAffiliationType_thenThrowAffiliationError() {
        Long affiliateId = 300L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType("EMPLEADOR");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.getWorkerDetail(affiliateId);
        });

        assertEquals("Tipo de afiliación no soportado para consulta de detalle: EMPLEADOR", 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void getWorkerDetail_whenIndependentWorkerNotFound_thenThrowAffiliationError() {
        Long affiliateId = 400L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findWorkerDetailByAffiliateId(affiliateId))
                .thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.getWorkerDetail(affiliateId);
        });

        assertEquals("No se encontraron datos del contrato para el trabajador independiente", 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationRepository).findWorkerDetailByAffiliateId(affiliateId);
    }

    @Test
    void getWorkerDetail_whenDependentWorkerNotFound_thenThrowAffiliationError() {
        Long affiliateId = 500L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findWorkerDetailByAffiliateId(affiliateId))
                .thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.getWorkerDetail(affiliateId);
        });

        assertEquals("No se encontraron datos del contrato para el trabajador dependiente", 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationDependentRepository).findWorkerDetailByAffiliateId(affiliateId);
    }

    // ========== Tests for updateContract ==========

    @Test
    void updateContract_whenIndependentWorker_thenUpdateSuccessfully() {
        Long affiliateId = 1000L;
        LocalDate newStartDate = LocalDate.of(2024, 1, 15);
        LocalDate newEndDate = LocalDate.of(2024, 12, 31);
        LocalDate newCoverageDate = LocalDate.of(2024, 1, 15);
        String newDuration = "11 MESES";
        java.math.BigDecimal newTotalValue = java.math.BigDecimal.valueOf(57600000.00);
        java.math.BigDecimal newMonthlyValue = java.math.BigDecimal.valueOf(4800000.00);
        java.math.BigDecimal newIbcValue = java.math.BigDecimal.valueOf(1920000.00);

        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(newStartDate)
                .contractEndDate(newEndDate)
                .contractDuration(newDuration)
                .coverageDate(newCoverageDate)
                .contractTotalValue(newTotalValue)
                .contractMonthlyValue(newMonthlyValue)
                .contractIbcValue(newIbcValue)
                .user("test@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setFiledNumber("IND-1000");
        affiliate.setCoverageStartDate(LocalDate.of(2023, 3, 11));

        Affiliation affiliation = new Affiliation();
        affiliation.setContractStartDate(LocalDate.of(2023, 3, 11));
        affiliation.setContractEndDate(null);
        affiliation.setContractDuration("12");
        affiliation.setContractTotalValue(java.math.BigDecimal.valueOf(45000000.00));
        affiliation.setContractMonthlyValue(java.math.BigDecimal.valueOf(3750000.00));
        affiliation.setContractIbcValue(java.math.BigDecimal.valueOf(1500000.00));

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliationRepository.save(any(Affiliation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        UpdateContractResponseDTO response = workerManagementService.updateContract(dto);

        assertNotNull(response);
        assertEquals(true, response.getSuccess());
        assertEquals("Contrato actualizado exitosamente", response.getMessage());
        assertEquals("IND-1000", response.getFiledNumber());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationRepository).findOne(any(Specification.class));
        verify(affiliationRepository).save(any(Affiliation.class));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }

    @Test
    void updateContract_whenAffiliateNotFound_thenThrowAffiliateNotFound() {
        Long affiliateId = 9999L;
        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .user("test@example.com")
                .build();

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> {
            workerManagementService.updateContract(dto);
        });

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateContract_whenNotIndependentWorker_thenThrowAffiliationError() {
        Long affiliateId = 2000L;
        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .user("test@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT);

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.updateContract(dto);
        });

        assertEquals("El afiliado no es un trabajador independiente. Tipo: " + 
                com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_DEPENDENT, 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateContract_whenNotActive_thenThrowAffiliationError() {
        Long affiliateId = 3000L;
        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .user("test@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus("INACTIVO");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.updateContract(dto);
        });

        assertEquals("La afiliación debe estar activa para actualizar el contrato.", 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
    }

    @Test
    void updateContract_whenAffiliationNotFound_thenThrowAffiliationError() {
        Long affiliateId = 4000L;
        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .user("test@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setFiledNumber("IND-4000");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            workerManagementService.updateContract(dto);
        });

        assertEquals("No se encontraron datos del contrato para el trabajador independiente", 
                exception.getError().getMessage());

        verify(affiliateRepository).findById(affiliateId);
        verify(affiliationRepository).findOne(any(Specification.class));
    }

    @Test
    void updateContract_whenSuccess_verifyFieldsUpdated() {
        Long affiliateId = 5000L;
        LocalDate newStartDate = LocalDate.of(2024, 1, 15);
        LocalDate newEndDate = LocalDate.of(2024, 12, 31);
        LocalDate newCoverageDate = LocalDate.of(2024, 1, 15);
        String newDuration = "11 MESES";
        java.math.BigDecimal newTotalValue = java.math.BigDecimal.valueOf(57600000.00);
        java.math.BigDecimal newMonthlyValue = java.math.BigDecimal.valueOf(4800000.00);
        java.math.BigDecimal newIbcValue = java.math.BigDecimal.valueOf(1920000.00);

        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(newStartDate)
                .contractEndDate(newEndDate)
                .contractDuration(newDuration)
                .coverageDate(newCoverageDate)
                .contractTotalValue(newTotalValue)
                .contractMonthlyValue(newMonthlyValue)
                .contractIbcValue(newIbcValue)
                .user("test@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setFiledNumber("IND-5000");
        affiliate.setCoverageStartDate(LocalDate.of(2023, 3, 11));

        Affiliation affiliation = new Affiliation();
        affiliation.setContractStartDate(LocalDate.of(2023, 3, 11));
        affiliation.setContractEndDate(null);
        affiliation.setContractDuration("12");
        affiliation.setContractTotalValue(java.math.BigDecimal.valueOf(45000000.00));
        affiliation.setContractMonthlyValue(java.math.BigDecimal.valueOf(3750000.00));
        affiliation.setContractIbcValue(java.math.BigDecimal.valueOf(1500000.00));

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliationRepository.save(any(Affiliation.class))).thenAnswer(invocation -> {
            Affiliation saved = invocation.getArgument(0);
            assertEquals(newStartDate, saved.getContractStartDate());
            assertEquals(newEndDate, saved.getContractEndDate());
            assertEquals(newDuration, saved.getContractDuration());
            assertEquals(newTotalValue, saved.getContractTotalValue());
            assertEquals(newMonthlyValue, saved.getContractMonthlyValue());
            assertEquals(newIbcValue, saved.getContractIbcValue());
            return saved;
        });
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(invocation -> {
            Affiliate saved = invocation.getArgument(0);
            assertEquals(newCoverageDate, saved.getCoverageStartDate());
            return saved;
        });
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenReturn(null);

        workerManagementService.updateContract(dto);

        verify(affiliationRepository).save(any(Affiliation.class));
        verify(affiliateRepository).save(any(Affiliate.class));
    }

    @Test
    void updateContract_whenSuccess_verifyTraceabilitySaved() {
        Long affiliateId = 6000L;
        UpdateContractDTO dto = UpdateContractDTO.builder()
                .idAffiliate(affiliateId)
                .contractStartDate(LocalDate.of(2024, 1, 15))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .contractDuration("11 MESES")
                .coverageDate(LocalDate.of(2024, 1, 15))
                .contractTotalValue(java.math.BigDecimal.valueOf(57600000.00))
                .contractMonthlyValue(java.math.BigDecimal.valueOf(4800000.00))
                .contractIbcValue(java.math.BigDecimal.valueOf(1920000.00))
                .user("trace@example.com")
                .build();

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(affiliateId);
        affiliate.setAffiliationType(com.gal.afiliaciones.infrastructure.utils.Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setFiledNumber("IND-6000");
        affiliate.setCoverageStartDate(LocalDate.of(2023, 3, 11));

        Affiliation affiliation = new Affiliation();
        affiliation.setContractStartDate(LocalDate.of(2023, 3, 11));
        affiliation.setContractEndDate(null);
        affiliation.setContractDuration("12");

        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliationRepository.save(any(Affiliation.class))).thenReturn(affiliation);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(traceRepository.save(any(TraceabilityOfficialUpdates.class))).thenAnswer(invocation -> {
            TraceabilityOfficialUpdates trace = invocation.getArgument(0);
            assertEquals("trace@example.com", trace.getUpdateBy());
            assertEquals(affiliateId, trace.getIdAffiliate());
            assertNotNull(trace.getModifyType());
            assertTrue(trace.getModifyType().length() <= 255);
            return trace;
        });

        workerManagementService.updateContract(dto);

        verify(traceRepository).save(any(TraceabilityOfficialUpdates.class));
    }
}