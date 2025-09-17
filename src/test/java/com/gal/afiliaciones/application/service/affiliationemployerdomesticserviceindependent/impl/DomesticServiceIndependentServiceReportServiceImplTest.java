
package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.domain.model.ApplicationForm;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
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
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;

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
    void testCapitalize() {
        assertEquals("Hello", DomesticServiceIndependentServiceReportServiceImpl.capitalize("hello"));
        assertEquals("Hello", DomesticServiceIndependentServiceReportServiceImpl.capitalize("HELLO"));
        assertEquals("H", DomesticServiceIndependentServiceReportServiceImpl.capitalize("h"));
        assertEquals("", DomesticServiceIndependentServiceReportServiceImpl.capitalize(null));
        assertEquals("", DomesticServiceIndependentServiceReportServiceImpl.capitalize(""));
    }

    @Test
    void testDefaultIfNullOrEmpty() {
        assertEquals("N/A", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty(null));
        assertEquals("N/A", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty(""));
        assertEquals("value", DomesticServiceIndependentServiceReportServiceImpl.defaultIfNullOrEmpty("value"));
    }

    /*@Test
    void testGeneratePdfReport() {
        Long idAffiliate = 1L;
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(idAffiliate);
        affiliate.setFiledNumber("FN123");

        // Prepare Affiliation mock
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
        affiliation.setCodeMainEconomicActivity("ECO1");
        affiliation.setNumHeadquarters(2);
        affiliation.setNumWorkCenters(3);
        affiliation.setInitialNumberWorkers(5);

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setIdWorkCenter(101L);
        affiliateActivityEconomic.setAffiliation(affiliation);
        affiliateActivityEconomic.setIsPrimary(true);

        affiliation.setEconomicActivity(List.of(affiliateActivityEconomic));

        when(domesticServicesAffiliationDao.findByIdAffiliate(idAffiliate)).thenReturn(affiliation);

        // FiledService mock
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC123");

        // ContributorType mock
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("CT1");
        contributorType.setDescription("Contributor Desc");
        when(contributorTypeRepository.findByCode("CT1")).thenReturn(Optional.of(contributorType));

        // MainOffice mock
        MainOffice mainOffice = new MainOffice();
        mainOffice.setCode("555");
        mainOffice.setMainOfficeName("Main Office Name");
        mainOffice.setAddress("Main Address");
        mainOffice.setIdDepartment(20L);
        mainOffice.setIdCity(30L);
        mainOffice.setMainOfficeZone("Zone1");
        mainOffice.setMainOfficePhoneNumber("1234567890");
        mainOffice.setMainOfficeEmail("mainoffice@example.com");

        // OfficeManager for MainOffice
        UserMain officeManager = new UserMain();
        officeManager.setFirstName("ManagerFirst");
        officeManager.setSecondName("ManagerSecond");
        officeManager.setSurname("ManagerSurname");
        officeManager.setSecondSurname("ManagerSecondSurname");
        officeManager.setIdentificationType("TI");
        officeManager.setIdentification("ID123");
        officeManager.setEmail("manager@example.com");
        mainOffice.setOfficeManager(officeManager);

        when(mainOfficeDao.findMainOfficeById(10L)).thenReturn(mainOffice);

        // Department mock
        Department department = new Department();
        department.setDepartmentName("departmentName");
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(department));

        // Municipality mock
        Municipality municipality = new Municipality();
        municipality.setMunicipalityName("municipalityName");
        municipality.setDivipolaCode("DIV123");
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(municipality));

        // WorkCenters mocks
        WorkCenter wc1 = new WorkCenter();
        wc1.setCode("WC1");
        wc1.setEconomicActivityCode("ECO1");
        wc1.setTotalWorkers(10);
        wc1.setRiskClass("Risk1");

        WorkCenter wc2 = new WorkCenter();
        wc2.setCode("WC2");
        wc2.setEconomicActivityCode("ECO2");
        wc2.setTotalWorkers(20);
        wc2.setRiskClass("Risk2");

        WorkCenter wc3 = new WorkCenter();
        wc3.setCode("WC3");
        wc3.setEconomicActivityCode("ECO3");
        wc3.setTotalWorkers(30);
        wc3.setRiskClass("Risk3");

        WorkCenter wc4 = new WorkCenter();
        wc4.setCode("WC4");
        wc4.setEconomicActivityCode("ECO4");
        wc4.setTotalWorkers(40);
        wc4.setRiskClass("Risk4");

        when(workCenterDao.findWorkCenterById(101L)).thenReturn(wc1);
        when(workCenterDao.findWorkCenterById(102L)).thenReturn(wc2);
        when(workCenterDao.findWorkCenterById(103L)).thenReturn(wc3);
        when(workCenterDao.findWorkCenterById(104L)).thenReturn(wc4);

        // Alfresco folderExists and getChildrenNode mocks for signature
        when(genericWebClient.folderExistsByName(anyString(), eq("123456789"))).thenReturn(Optional.of("folderId"));

        AlfrescoResponseDTO alfrescoResponseDTO = new AlfrescoResponseDTO();

        when(genericWebClient.folderExistsByName("folderParentId", "123456789")).thenReturn(Optional.of("folderId"));
        when(genericWebClient.getChildrenNode("folderId")).thenReturn(alfrescoResponseDTO);
        when(genericWebClient.getFileBase64("fileId")).thenReturn(Mono.just("base64Signature"));

        // genericWebClient.generateReportCertificate mock
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class))).thenReturn("pdfReportContent");

        // Run method under test
        String result = service.generatePdfReport(idAffiliate);

        // Verify results
        assertNotNull(result);
        assertEquals("pdfReportContent", result);

        // Verify saveFormRegistry called with correct ApplicationForm saved
        ArgumentCaptor<ApplicationForm> appFormCaptor = ArgumentCaptor.forClass(ApplicationForm.class);
        verify(applicationFormDao).saveFormRegistry(appFormCaptor.capture());
        ApplicationForm savedForm = appFormCaptor.getValue();
        assertEquals("FN123", savedForm.getFiledNumberAffiliation());
        assertEquals("CC", savedForm.getIdentificationType());
        assertEquals("123456789", savedForm.getIdentificationNumber());
        assertEquals("DOC123", savedForm.getFiledNumberDocument());
        assertNotNull(savedForm.getExpeditionDate());

        // Verify that genericWebClient.generateReportCertificate was called once
        verify(genericWebClient, times(1)).generateReportCertificate(any(CertificateReportRequestDTO.class));
    }

    // Helper class to simulate Mono.just for reactive calls in tests
    static class MonoJust<T> {
        private final T value;

        private MonoJust(T value) {
            this.value = value;
        }

        public static <T> MonoJust<T> just(T value) {
            return new MonoJust<>(value);
        }

        public T block() {
            return value;
        }
    }*/
}
