
package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ApplicationForm;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.novelty.ContributorType;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.DomesticServicesAffiliationRepositoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.mainoffice.MainOfficeDao;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.workcenter.WorkCenterDao;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.ContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.EntryDetailsDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.ListDTO;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.ListDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

public class DomesticServiceIndependentServiceReportServiceImplTest {

    private DomesticServicesAffiliationRepositoryRepository domesticServicesAffiliationDao;
    private MainOfficeDao mainOfficeDao;
    private DepartmentRepository departmentRepository;
    private MunicipalityRepository municipalityRepository;
    private GenericWebClient genericWebClient;
    private ApplicationFormDao applicationFormDao;
    private FiledService filedService;
    private ContributorTypeRepository contributorTypeRepository;
    private CollectProperties properties;
    private WorkCenterService workCenterService;

    private DomesticServiceIndependentServiceReportServiceImpl service;

    @BeforeEach
    void setup() {
        domesticServicesAffiliationDao = mock(DomesticServicesAffiliationRepositoryRepository.class);
        mainOfficeDao = mock(MainOfficeDao.class);
        departmentRepository = mock(DepartmentRepository.class);
        municipalityRepository = mock(MunicipalityRepository.class);
        genericWebClient = mock(GenericWebClient.class);
        applicationFormDao = mock(ApplicationFormDao.class);
        filedService = mock(FiledService.class);
        contributorTypeRepository = mock(ContributorTypeRepository.class);
        properties = mock(CollectProperties.class);
        workCenterService = mock(WorkCenterService.class);

        service = new DomesticServiceIndependentServiceReportServiceImpl(
                domesticServicesAffiliationDao,
                mainOfficeDao,
                departmentRepository,
                municipalityRepository,
                genericWebClient,
                applicationFormDao,
                filedService,
                contributorTypeRepository,
                properties,
                workCenterService
        );
    }



    @Test
    void testGeneratePdfReport_WithMultipleEconomicActivities() {

        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        EconomicActivity economicActivity2 = new EconomicActivity();
        economicActivity2.setEconomicActivityCode("ECO2");
        economicActivity2.setDescription("Second Activity");
        economicActivity2.setClassRisk("2");

        AffiliateActivityEconomic affiliateActivityEconomic2 = new AffiliateActivityEconomic();
        affiliateActivityEconomic2.setActivityEconomic(economicActivity2);
        affiliateActivityEconomic2.setIsPrimary(false);

        List<AffiliateActivityEconomic> activities = new ArrayList<>(affiliation.getEconomicActivity());
        activities.add(affiliateActivityEconomic2);
        affiliation.setEconomicActivity(activities);

        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();

        WorkCenter workCenter1 = createMockWorkCenter();
        WorkCenter workCenter2 = new WorkCenter();
        workCenter2.setCode("WC2");
        workCenter2.setEconomicActivityCode("ECO2");
        workCenter2.setTotalWorkers(15);
        workCenter2.setRiskClass("Risk2");

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice("ECO1", 1L)).thenReturn(workCenter1);
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice("ECO2", 1L)).thenReturn(workCenter2);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        assertEquals("pdfReportContent", result);
    }


    @Test
    void testGeneratePdfReport_WithProcessType() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        affiliation.setIdProcedureType(2L);

        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        assertEquals("pdfReportContent", result);
    }

    @Test
    void testDateFormatter() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());

        CertificateReportRequestDTO request = captor.getValue();
        assertNotNull(request.getParameters().get("fechaInicioAfiliacion"));
        assertNotNull(request.getParameters().get("fechaRadicado"));
    }

    @Test
    void testWorkCenterEmpty() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());
        CertificateReportRequestDTO request = captor.getValue();
        Map<String, Object> parameters = request.getParameters();
        assertNotNull(parameters.get("centrosTrabajo2"));
        assertNotNull(parameters.get("centrosTrabajo3"));
        assertNotNull(parameters.get("centrosTrabajo4"));
        assertNotNull(parameters.get("centrosTrabajo5"));
        assertNotNull(parameters.get("centrosTrabajo6"));
    }

    @Test
    void testTransferMethod() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());
        CertificateReportRequestDTO request = captor.getValue();
        Map<String, Object> parameters = request.getParameters();
        assertEquals("N/A", parameters.get("arlTraslado "));
        assertEquals("N/A", parameters.get("numeroTotalTrabajadoresOestudiantes"));
        assertEquals("N/A", parameters.get("montoTotalCotizacion"));
    }

    @Test
    void testHeadquartersWorkCenters() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());
        CertificateReportRequestDTO request = captor.getValue();
        Map<String, Object> parameters = request.getParameters();
        assertEquals("2", parameters.get("numeroSedes"));
        assertEquals("3", parameters.get("numeroCentrosTrabajo"));
        assertEquals("5", parameters.get("numeroInicialTrabajadores"));
        assertEquals("N/A", parameters.get("valorTotalNomina"));
    }

    @Test
    void testCapitalize_WithValidInput() {
        assertEquals("Hello", DomesticServiceIndependentServiceReportServiceImpl.capitalize("hello"));
        assertEquals("Hello", DomesticServiceIndependentServiceReportServiceImpl.capitalize("HELLO"));
        assertEquals("Hello world", DomesticServiceIndependentServiceReportServiceImpl.capitalize("hello world"));
        assertEquals("H", DomesticServiceIndependentServiceReportServiceImpl.capitalize("h"));
    }

    @Test
    void testCapitalize_WithNullOrEmpty() {
        assertEquals("", DomesticServiceIndependentServiceReportServiceImpl.capitalize(null));
        assertEquals("", DomesticServiceIndependentServiceReportServiceImpl.capitalize(""));
    }

    @Test
    void testDefaultIfNullOrEmpty() {
        assertEquals("N/A", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty(null));
        assertEquals("N/A", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty(""));
        assertEquals("value", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty("value"));
        assertEquals("test", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty("test"));
    }

    @Test
    void testGeneratePdfReport_Success() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();
        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        assertEquals("pdfReportContent", result);
        ArgumentCaptor<ApplicationForm> appFormCaptor = ArgumentCaptor.forClass(ApplicationForm.class);
        verify(applicationFormDao).saveFormRegistry(appFormCaptor.capture());
        ApplicationForm savedForm = appFormCaptor.getValue();
        assertEquals("FN123", savedForm.getFiledNumberAffiliation());
        assertEquals("CC", savedForm.getIdentificationType());
        assertEquals("123456789", savedForm.getIdentificationNumber());
        assertEquals("DOC123", savedForm.getFiledNumberDocument());

        verify(genericWebClient).generateReportCertificate(any(CertificateReportRequestDTO.class));
    }

    @Test
    void testGeneratePdfReport_WithSignatureFound() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();
        AlfrescoResponseDTO alfrescoResponse = createMockAlfrescoResponse();
        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(properties.getNodeFirmas()).thenReturn("nodeId");
        when(genericWebClient.folderExistsByName("nodeId", "123456789"))
                .thenReturn(Optional.of("folderId"));
        when(genericWebClient.getChildrenNode("folderId")).thenReturn(alfrescoResponse);
        when(genericWebClient.getFileBase64("fileId")).thenReturn(Mono.just("base64Signature"));
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        assertEquals("pdfReportContent", result);
        verify(genericWebClient).getFileBase64("fileId");
    }

    @Test
    void testGeneratePdfReport_NoContributorType() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.empty());
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");
        String result = service.generatePdfReport(idAffiliate);
        assertNotNull(result);
        assertEquals("pdfReportContent", result);
    }

    @Test
    void testGeneratePdfReport_WithNullFields() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliationWithNulls();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        assertThrows(NullPointerException.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_DepartmentNotFound() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.empty());
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_MunicipalityNotFound() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.empty());
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_NoEconomicActivityFound() {

        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        affiliation.getEconomicActivity().get(0).setIsPrimary(false);

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.empty());
        assertThrows(NullPointerException.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_AffiliationErrorForNoEconomicActivity() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();

        affiliation.getEconomicActivity().get(0).setIsPrimary(false);

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        assertThrows(AffiliationError.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_WithEmptyAlfrescoResponse() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();
        EntryDTO entryDTO = mock(EntryDTO.class);
        when(entryDTO.getEntry()).thenReturn(null);

        AlfrescoResponseDTO alfrescoResponse = mock(AlfrescoResponseDTO.class);
        ListDTO listDTO = mock(ListDTO.class);
        when(listDTO.getEntries()).thenReturn(List.of(entryDTO));
        when(alfrescoResponse.getList()).thenReturn(listDTO);

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(properties.getNodeFirmas()).thenReturn("nodeId");
        when(genericWebClient.folderExistsByName("nodeId", "123456789"))
                .thenReturn(Optional.of("folderId"));
        when(genericWebClient.getChildrenNode("folderId")).thenReturn(alfrescoResponse);
        assertThrows(NullPointerException.class, () -> {
            service.generatePdfReport(idAffiliate);
        });
    }

    @Test
    void testGeneratePdfReport_WithEmptyEntriesList() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        MainOffice mainOffice = createMockMainOffice();
        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(properties.getNodeFirmas()).thenReturn("nodeId");
        when(genericWebClient.folderExistsByName("nodeId", "123456789"))
                .thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");

        String result = service.generatePdfReport(idAffiliate);

        assertNotNull(result);
        assertEquals("pdfReportContent", result);

        verify(genericWebClient, never()).getChildrenNode(anyString());
        verify(genericWebClient, never()).getFileBase64(anyString());
    }


    @Test
    void testGeneratePdfReport_WithPartialNullFields() {
        Long idAffiliate = 1L;
        Affiliation affiliation = createMockAffiliation();
        affiliation.setEmail(null);
        affiliation.setSecondName(null);
        affiliation.setSecondSurname(null);

        MainOffice mainOffice = createMockMainOffice();
        mainOffice.setMainOfficeEmail(null);
        mainOffice.setMainOfficeZone(null);

        Department department = createMockDepartment();
        Municipality municipality = createMockMunicipality();
        ContributorType contributorType = createMockContributorType();
        WorkCenter workCenter = createMockWorkCenter();

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));
        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(workCenter);
        when(genericWebClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class)))
                .thenReturn("pdfReportContent");

        String result = service.generatePdfReport(idAffiliate);

        assertNotNull(result);
        assertEquals("pdfReportContent", result);
    }

    private Affiliation createMockAffiliation() {
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("FN123");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFirstName("John");
        affiliation.setSecondName("M");
        affiliation.setSurname("Doe");
        affiliation.setSecondSurname("Smith");
        affiliation.setEmail("john.doe@example.com");
        affiliation.setIdMainHeadquarter(10L);
        affiliation.setCodeLegalNatureEmployer(1L);
        affiliation.setNameLegalNatureEmployer("Legal Nature");
        affiliation.setIdProcedureType(1L);
        affiliation.setCodeContributorType("CT1");
        affiliation.setNumHeadquarters(2);
        affiliation.setNumWorkCenters(3);
        affiliation.setInitialNumberWorkers(5);

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("ECO1");
        economicActivity.setDescription("Economic Activity");
        economicActivity.setClassRisk("1");

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        affiliateActivityEconomic.setIsPrimary(true);

        affiliation.setEconomicActivity(List.of(affiliateActivityEconomic));

        return affiliation;
    }

    private Affiliation createMockAffiliationWithNulls() {
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(null);
        affiliation.setIdentificationDocumentType(null);
        affiliation.setIdentificationDocumentNumber(null);
        affiliation.setFirstName(null);
        affiliation.setSecondName(null);
        affiliation.setSurname(null);
        affiliation.setSecondSurname(null);
        affiliation.setEmail(null);
        affiliation.setIdMainHeadquarter(10L);
        affiliation.setCodeLegalNatureEmployer(null);
        affiliation.setNameLegalNatureEmployer(null);
        affiliation.setIdProcedureType(1L);
        affiliation.setCodeContributorType(null);
        // Usar valores primitivos o verificar si los setters existen
        // affiliation.setNumHeadquarters(null);
        // affiliation.setNumWorkCenters(null);
        // affiliation.setInitialNumberWorkers(null);

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("ECO1");
        economicActivity.setDescription("Economic Activity");
        economicActivity.setClassRisk("1");

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        affiliateActivityEconomic.setIsPrimary(true);

        affiliation.setEconomicActivity(List.of(affiliateActivityEconomic));

        return affiliation;
    }

    private MainOffice createMockMainOffice() {
        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(1L);
        mainOffice.setCode("555");
        mainOffice.setMainOfficeName("Main Office Name");
        mainOffice.setAddress("Main Address");
        mainOffice.setIdDepartment(20L);
        mainOffice.setIdCity(30L);
        mainOffice.setMainOfficeZone("Zone1");
        mainOffice.setMainOfficePhoneNumber("1234567890");
        mainOffice.setMainOfficeEmail("mainoffice@example.com");

        UserMain officeManager = new UserMain();
        officeManager.setFirstName("ManagerFirst");
        officeManager.setSecondName("ManagerSecond");
        officeManager.setSurname("ManagerSurname");
        officeManager.setSecondSurname("ManagerSecondSurname");
        officeManager.setIdentificationType("TI");
        officeManager.setIdentification("ID123");
        officeManager.setEmail("manager@example.com");
        mainOffice.setOfficeManager(officeManager);

        return mainOffice;
    }

    private MainOffice createMockMainOfficeWithNulls() {
        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(1L);
        mainOffice.setCode(null);
        mainOffice.setMainOfficeName(null);
        mainOffice.setAddress(null);
        mainOffice.setIdDepartment(20L);
        mainOffice.setIdCity(30L);
        mainOffice.setMainOfficeZone(null);
        mainOffice.setMainOfficePhoneNumber(null);
        mainOffice.setMainOfficeEmail(null);

        UserMain officeManager = new UserMain();
        officeManager.setFirstName(null);
        officeManager.setSecondName(null);
        officeManager.setSurname(null);
        officeManager.setSecondSurname(null);
        officeManager.setIdentificationType(null);
        officeManager.setIdentification(null);
        officeManager.setEmail(null);
        mainOffice.setOfficeManager(officeManager);

        return mainOffice;
    }

    private Department createMockDepartment() {
        Department department = new Department();
        department.setDepartmentName("departmentName");
        return department;
    }

    private Municipality createMockMunicipality() {
        Municipality municipality = new Municipality();
        municipality.setMunicipalityName("municipalityName");
        municipality.setDivipolaCode("DIV123");
        return municipality;
    }

    private ContributorType createMockContributorType() {
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("CT1");
        contributorType.setDescription("Contributor Desc");
        return contributorType;
    }

    private WorkCenter createMockWorkCenter() {
        WorkCenter workCenter = new WorkCenter();
        workCenter.setCode("WC1");
        workCenter.setEconomicActivityCode("ECO1");
        workCenter.setTotalWorkers(10);
        workCenter.setRiskClass("Risk1");
        return workCenter;
    }

    private AlfrescoResponseDTO createMockAlfrescoResponse() {

        AlfrescoResponseDTO alfrescoResponse = mock(AlfrescoResponseDTO.class);
        ListDTO listDTO = mock(ListDTO.class);
        EntryDTO entryDTO = mock(EntryDTO.class);
        EntryDetailsDTO entryDetailsDTO = mock(EntryDetailsDTO.class);

        when(entryDetailsDTO.getId()).thenReturn("fileId");
        when(entryDTO.getEntry()).thenReturn(entryDetailsDTO);
        when(listDTO.getEntries()).thenReturn(List.of(entryDTO));
        when(alfrescoResponse.getList()).thenReturn(listDTO);

        return alfrescoResponse;
    }
}