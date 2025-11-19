package com.gal.afiliaciones.application.service.impl.certicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.economicactivity.impl.EconomicActivityServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.certificate.CertificateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCodeValidationExpired;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.CertificateAffiliate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateAffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployeesView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployerView;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;

@ContextConfiguration(classes = {CertificateServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class CertificateServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private AffiliationDependentRepository affiliationDependentRepository;

    @MockBean
    private ArlInformationDao arlInformationDao;

    @MockBean
    private CertificateRepository certificateRepository;

    @MockBean
    private CertificateServiceHelper certificateServiceHelper;

    @Autowired
    private CertificateServiceImpl certificateServiceImpl;

    @MockBean
    private CodeValidCertificationService codeValidCertificationService;

    @MockBean
    private EconomicActivityServiceImpl economicActivityServiceImpl;

    @MockBean
    private FiledService filedService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository
            iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private ICardRepository iCardRepository;

    @MockBean
    private ICertificateAffiliateRepository iCertificateAffiliateRepository;

    @MockBean
    private IEconomicActivityRepository iEconomicActivityRepository;

    @MockBean
    private IQrRepository iQrRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private OccupationRepository occupationRepository;

    @MockBean
    private OccupationDecree1563Repository occupationDecree1563Repository;

    @MockBean
    private RequestCollectionRequestRepository requestCollectionRequestRepository;

    @MockBean
    private RequestCorrectionRepository requestCorrectionRepository;

    private Affiliate affiliate;
    private Certificate certificate;
    private FindAffiliateReqDTO findAffiliateReqDTO;


    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setDocumentNumber("12345");
        affiliate.setDocumentType("CC");
        affiliate.setFiledNumber("F123");
        affiliate.setAffiliationDate(LocalDateTime.now());
        affiliate.setAffiliationType("Independiente");
        affiliate.setAffiliationStatus("Activo");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("900123456-1");

        certificate = new Certificate();
        certificate.setNumberDocument("12345");
        certificate.setValidatorCode("VALID123");
        certificate.setVinculationType("Independiente");

        findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setIdAffiliate(1);
    }

    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate() {
        // Arrange
        when(affiliateRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("Certificate Type");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate2() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");
        when(certificateRepository.save(Mockito.<Certificate>any())).thenReturn(certificate);
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployerView.getAddress()).thenReturn("42 Main St");
        when(singleMembershipCertificateEmployerView.getCityName()).thenReturn("Oxford");
        when(singleMembershipCertificateEmployerView.getCompany()).thenReturn("Company");
        when(singleMembershipCertificateEmployerView.getDepartmentName()).thenReturn("Department Name");
        when(singleMembershipCertificateEmployerView.getDocumentType()).thenReturn("Document Type");
        when(singleMembershipCertificateEmployerView.getEconomicActivityCode())
                .thenReturn("Economic Activity Code");
        when(singleMembershipCertificateEmployerView.getEconomicActivityDescription())
                .thenReturn("Economic Activity Description");
        when(singleMembershipCertificateEmployerView.getEmailContactCompany())
                .thenReturn("jane.doe@example.org");
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(singleMembershipCertificateEmployerView.getPhone()).thenReturn("6625550144");
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(new ArrayList<>());
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate()).thenReturn("42");
        when(iCertificateAffiliateRepository.saveAll(Mockito.<Iterable<CertificateAffiliate>>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployerView).getAddress();
        verify(singleMembershipCertificateEmployerView).getCityName();
        verify(singleMembershipCertificateEmployerView).getCompany();
        verify(singleMembershipCertificateEmployerView).getDepartmentName();
        verify(singleMembershipCertificateEmployerView).getDocumentType();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityCode();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityDescription();
        verify(singleMembershipCertificateEmployerView).getEmailContactCompany();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getNitCompany();
        verify(singleMembershipCertificateEmployerView).getPhone();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getRiskRate();
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository).save(isA(Certificate.class));
        verify(iCertificateAffiliateRepository).saveAll(isA(Iterable.class));
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate3() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(arlInformationDao.findAllArlInformation())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("Certificate Type");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(arlInformationDao).findAllArlInformation();
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate4() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");
        when(certificateRepository.save(Mockito.<Certificate>any())).thenReturn(certificate);
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployerView.getAddress()).thenReturn("42 Main St");
        when(singleMembershipCertificateEmployerView.getCityName()).thenReturn("Oxford");
        when(singleMembershipCertificateEmployerView.getCompany()).thenReturn("Company");
        when(singleMembershipCertificateEmployerView.getDepartmentName()).thenReturn("Department Name");
        when(singleMembershipCertificateEmployerView.getDocumentType()).thenReturn("Document Type");
        when(singleMembershipCertificateEmployerView.getEconomicActivityCode())
                .thenReturn("Economic Activity Code");
        when(singleMembershipCertificateEmployerView.getEconomicActivityDescription())
                .thenReturn("Economic Activity Description");
        when(singleMembershipCertificateEmployerView.getEmailContactCompany())
                .thenReturn("jane.doe@example.org");
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(singleMembershipCertificateEmployerView.getPhone()).thenReturn("6625550144");

        SingleMembershipCertificateEmployeesView singleMembershipCertificateEmployeesView =
                mock(SingleMembershipCertificateEmployeesView.class);
        when(singleMembershipCertificateEmployeesView.getCoverageDate())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        ArrayList<SingleMembershipCertificateEmployeesView>
                singleMembershipCertificateEmployeesViewList = new ArrayList<>();
        singleMembershipCertificateEmployeesViewList.add(singleMembershipCertificateEmployeesView);
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployeesViewList);
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate()).thenReturn("42");

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployeesView).getCoverageDate();
        verify(singleMembershipCertificateEmployerView).getAddress();
        verify(singleMembershipCertificateEmployerView).getCityName();
        verify(singleMembershipCertificateEmployerView).getCompany();
        verify(singleMembershipCertificateEmployerView).getDepartmentName();
        verify(singleMembershipCertificateEmployerView).getDocumentType();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityCode();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityDescription();
        verify(singleMembershipCertificateEmployerView).getEmailContactCompany();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getNitCompany();
        verify(singleMembershipCertificateEmployerView).getPhone();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getRiskRate();
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository).save(isA(Certificate.class));
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate5() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");
        when(certificateRepository.save(Mockito.<Certificate>any())).thenReturn(certificate);
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployerView.getAddress()).thenReturn("42 Main St");
        when(singleMembershipCertificateEmployerView.getCityName()).thenReturn("Oxford");
        when(singleMembershipCertificateEmployerView.getCompany()).thenReturn("Company");
        when(singleMembershipCertificateEmployerView.getDepartmentName()).thenReturn("Department Name");
        when(singleMembershipCertificateEmployerView.getDocumentType()).thenReturn("Document Type");
        when(singleMembershipCertificateEmployerView.getEconomicActivityCode())
                .thenReturn("Economic Activity Code");
        when(singleMembershipCertificateEmployerView.getEconomicActivityDescription())
                .thenReturn("Economic Activity Description");
        when(singleMembershipCertificateEmployerView.getEmailContactCompany())
                .thenReturn("jane.doe@example.org");
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(singleMembershipCertificateEmployerView.getPhone()).thenReturn("6625550144");

        SingleMembershipCertificateEmployeesView singleMembershipCertificateEmployeesView =
                mock(SingleMembershipCertificateEmployeesView.class);
        when(singleMembershipCertificateEmployeesView.getIdentificationType())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));
        when(singleMembershipCertificateEmployeesView.getCoverageDate())
                .thenReturn(LocalDate.of(1970, 1, 1));

        ArrayList<SingleMembershipCertificateEmployeesView>
                singleMembershipCertificateEmployeesViewList = new ArrayList<>();
        singleMembershipCertificateEmployeesViewList.add(singleMembershipCertificateEmployeesView);
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployeesViewList);
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate()).thenReturn("42");

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployeesView).getCoverageDate();
        verify(singleMembershipCertificateEmployeesView).getIdentificationType();
        verify(singleMembershipCertificateEmployerView).getAddress();
        verify(singleMembershipCertificateEmployerView).getCityName();
        verify(singleMembershipCertificateEmployerView).getCompany();
        verify(singleMembershipCertificateEmployerView).getDepartmentName();
        verify(singleMembershipCertificateEmployerView).getDocumentType();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityCode();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityDescription();
        verify(singleMembershipCertificateEmployerView).getEmailContactCompany();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getNitCompany();
        verify(singleMembershipCertificateEmployerView).getPhone();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getRiskRate();
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository).save(isA(Certificate.class));
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate6() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployerView).getNitCompany();
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate7() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate8() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getNitCompany())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployerView).getNitCompany();
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO)")
    void testCreateAndGenerateCertificate9() {
        // Arrange
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(new ArrayList<>());
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployerView).getNitCompany();
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <ul>
     *   <li>Given {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO); given 'null'")
    void testCreateAndGenerateCertificate_givenNull() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(
                Mockito.<Specification<Affiliation>>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo(null);
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("Certificate Type");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(arlInformationDao).findAllArlInformation();
        verify(iAffiliationEmployerDomesticServiceIndependentRepository)
                .findOne(isA(Specification.class));
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <ul>
     *   <li>Then calls {@link
     *       IAffiliationEmployerDomesticServiceIndependentRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName(
            "Test createAndGenerateCertificate(FindAffiliateReqDTO); then calls findOne(Specification)")
    void testCreateAndGenerateCertificate_thenCallsFindOne() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(
                Mockito.<Specification<Affiliation>>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("Certificate Type");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO));
        verify(arlInformationDao).findAllArlInformation();
        verify(iAffiliationEmployerDomesticServiceIndependentRepository)
                .findOne(isA(Specification.class));
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <ul>
     *   <li>Then calls {@link SingleMembershipCertificateEmployeesView#getFullName()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName("Test createAndGenerateCertificate(FindAffiliateReqDTO); then calls getFullName()")
    void testCreateAndGenerateCertificate_thenCallsGetFullName() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");
        when(certificateRepository.save(Mockito.<Certificate>any())).thenReturn(certificate);
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployerView.getAddress()).thenReturn("42 Main St");
        when(singleMembershipCertificateEmployerView.getCityName()).thenReturn("Oxford");
        when(singleMembershipCertificateEmployerView.getCompany()).thenReturn("Company");
        when(singleMembershipCertificateEmployerView.getDepartmentName()).thenReturn("Department Name");
        when(singleMembershipCertificateEmployerView.getDocumentType()).thenReturn("Document Type");
        when(singleMembershipCertificateEmployerView.getEconomicActivityCode())
                .thenReturn("Economic Activity Code");
        when(singleMembershipCertificateEmployerView.getEconomicActivityDescription())
                .thenReturn("Economic Activity Description");
        when(singleMembershipCertificateEmployerView.getEmailContactCompany())
                .thenReturn("jane.doe@example.org");
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(singleMembershipCertificateEmployerView.getPhone()).thenReturn("6625550144");

        SingleMembershipCertificateEmployeesView singleMembershipCertificateEmployeesView =
                mock(SingleMembershipCertificateEmployeesView.class);
        when(singleMembershipCertificateEmployeesView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployeesView.getFullName()).thenReturn("Dr Jane Doe");
        when(singleMembershipCertificateEmployeesView.getIdentificationNumber()).thenReturn("42");
        when(singleMembershipCertificateEmployeesView.getIdentificationType())
                .thenReturn("Identification Type");
        when(singleMembershipCertificateEmployeesView.getRisk()).thenReturn("Risk");
        when(singleMembershipCertificateEmployeesView.getCoverageDate())
                .thenReturn(LocalDate.of(1970, 1, 1));

        ArrayList<SingleMembershipCertificateEmployeesView>
                singleMembershipCertificateEmployeesViewList = new ArrayList<>();
        singleMembershipCertificateEmployeesViewList.add(singleMembershipCertificateEmployeesView);
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployeesViewList);
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(genericWebClient.generateReportCertificate(Mockito.<CertificateReportRequestDTO>any()))
                .thenReturn("Generate Report Certificate");
        when(certificateServiceHelper.transformToSingleMembershipCertificate(
                Mockito.<SingleMembershipCertificateEmployerView>any(),
                Mockito.<List<Map<String, Object>>>any(),
                Mockito.<ArlInformation>any(),
                Mockito.<String>any(),
                Mockito.<String>any()))
                .thenReturn(new CertificateReportRequestDTO());

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate()).thenReturn("42");
        when(iCertificateAffiliateRepository.saveAll(Mockito.<Iterable<CertificateAffiliate>>any()))
                .thenReturn(new ArrayList<>());

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act
        String actualCreateAndGenerateCertificateResult =
                certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO);

        // Assert
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(certificateServiceHelper)
                .transformToSingleMembershipCertificate(
                        isA(SingleMembershipCertificateEmployerView.class),
                        isA(List.class),
                        isA(ArlInformation.class),
                        eq("42"),
                        eq("Consult Code"));
        verify(genericWebClient).generateReportCertificate(isA(CertificateReportRequestDTO.class));
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getCoverageDate();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getFullName();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getIdentificationNumber();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getIdentificationType();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getRisk();
        verify(singleMembershipCertificateEmployeesView, atLeast(1)).getRiskRate();
        verify(singleMembershipCertificateEmployerView).getAddress();
        verify(singleMembershipCertificateEmployerView).getCityName();
        verify(singleMembershipCertificateEmployerView).getCompany();
        verify(singleMembershipCertificateEmployerView).getDepartmentName();
        verify(singleMembershipCertificateEmployerView).getDocumentType();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityCode();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityDescription();
        verify(singleMembershipCertificateEmployerView).getEmailContactCompany();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getNitCompany();
        verify(singleMembershipCertificateEmployerView).getPhone();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getRiskRate();
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository).save(isA(Certificate.class));
        verify(iCertificateAffiliateRepository).saveAll(isA(Iterable.class));
        assertEquals("Generate Report Certificate", actualCreateAndGenerateCertificateResult);
    }

    /**
     * Test {@link CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}.
     *
     * <ul>
     *   <li>Then return {@code Generate Report Certificate}.
     * </ul>
     *
     * <p>Method under test: {@link
     * CertificateServiceImpl#createAndGenerateCertificate(FindAffiliateReqDTO)}
     */
    @Test
    @DisplayName(
            "Test createAndGenerateCertificate(FindAffiliateReqDTO); then return 'Generate Report Certificate'")
    void testCreateAndGenerateCertificate_thenReturnGenerateReportCertificate() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");
        when(certificateRepository.save(Mockito.<Certificate>any())).thenReturn(certificate);
        when(codeValidCertificationService.consultCode(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("Consult Code");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);

        SingleMembershipCertificateEmployerView singleMembershipCertificateEmployerView =
                mock(SingleMembershipCertificateEmployerView.class);
        when(singleMembershipCertificateEmployerView.getRiskRate()).thenReturn(10.0f);
        when(singleMembershipCertificateEmployerView.getAddress()).thenReturn("42 Main St");
        when(singleMembershipCertificateEmployerView.getCityName()).thenReturn("Oxford");
        when(singleMembershipCertificateEmployerView.getCompany()).thenReturn("Company");
        when(singleMembershipCertificateEmployerView.getDepartmentName()).thenReturn("Department Name");
        when(singleMembershipCertificateEmployerView.getDocumentType()).thenReturn("Document Type");
        when(singleMembershipCertificateEmployerView.getEconomicActivityCode())
                .thenReturn("Economic Activity Code");
        when(singleMembershipCertificateEmployerView.getEconomicActivityDescription())
                .thenReturn("Economic Activity Description");
        when(singleMembershipCertificateEmployerView.getEmailContactCompany())
                .thenReturn("jane.doe@example.org");
        when(singleMembershipCertificateEmployerView.getNitCompany()).thenReturn("Nit Company");
        when(singleMembershipCertificateEmployerView.getPhone()).thenReturn("6625550144");
        when(affiliateRepository.findSingleMembershipCertificateEmployees(Mockito.<String>any()))
                .thenReturn(new ArrayList<>());
        when(affiliateRepository.findSingleMembershipCertificateEmployer(Mockito.<String>any()))
                .thenReturn(singleMembershipCertificateEmployerView);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(genericWebClient.generateReportCertificate(Mockito.<CertificateReportRequestDTO>any()))
                .thenReturn("Generate Report Certificate");
        when(certificateServiceHelper.transformToSingleMembershipCertificate(
                Mockito.<SingleMembershipCertificateEmployerView>any(),
                Mockito.<List<Map<String, Object>>>any(),
                Mockito.<ArlInformation>any(),
                Mockito.<String>any(),
                Mockito.<String>any()))
                .thenReturn(new CertificateReportRequestDTO());

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("certificado unico afiliacion");
        arlInformation.setDv("certificado unico afiliacion");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("certificado unico afiliacion");
        arlInformation.setNit("certificado unico afiliacion");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("certificado unico afiliacion");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(filedService.getNextFiledNumberCertificate()).thenReturn("42");
        when(iCertificateAffiliateRepository.saveAll(Mockito.<Iterable<CertificateAffiliate>>any()))
                .thenReturn(new ArrayList<>());

        FindAffiliateReqDTO findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setAddressedTo("42 Main St");
        findAffiliateReqDTO.setAffiliationType("Affiliation Type");
        findAffiliateReqDTO.setCertificateType("certificado unico afiliacion");
        findAffiliateReqDTO.setDocumentNumber("42");
        findAffiliateReqDTO.setDocumentType("Document Type");
        findAffiliateReqDTO.setIdAffiliate(1);

        // Act
        String actualCreateAndGenerateCertificateResult =
                certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO);

        // Assert
        verify(codeValidCertificationService).consultCode("42", "Document Type");
        verify(filedService).getNextFiledNumberCertificate();
        verify(certificateServiceHelper)
                .transformToSingleMembershipCertificate(
                        isA(SingleMembershipCertificateEmployerView.class),
                        isA(List.class),
                        isA(ArlInformation.class),
                        eq("42"),
                        eq("Consult Code"));
        verify(genericWebClient).generateReportCertificate(isA(CertificateReportRequestDTO.class));
        verify(affiliateRepository).findSingleMembershipCertificateEmployees("Nit Company");
        verify(affiliateRepository).findSingleMembershipCertificateEmployer("42");
        verify(arlInformationDao).findAllArlInformation();
        verify(singleMembershipCertificateEmployerView).getAddress();
        verify(singleMembershipCertificateEmployerView).getCityName();
        verify(singleMembershipCertificateEmployerView).getCompany();
        verify(singleMembershipCertificateEmployerView).getDepartmentName();
        verify(singleMembershipCertificateEmployerView).getDocumentType();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityCode();
        verify(singleMembershipCertificateEmployerView).getEconomicActivityDescription();
        verify(singleMembershipCertificateEmployerView).getEmailContactCompany();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getNitCompany();
        verify(singleMembershipCertificateEmployerView).getPhone();
        verify(singleMembershipCertificateEmployerView, atLeast(1)).getRiskRate();
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository).save(isA(Certificate.class));
        verify(iCertificateAffiliateRepository).saveAll(isA(Iterable.class));
        assertEquals("Generate Report Certificate", actualCreateAndGenerateCertificateResult);
    }

    /**
     * Test {@link CertificateServiceImpl#createCertificate(Affiliate, String)}.
     *
     * <p>Method under test: {@link CertificateServiceImpl#createCertificate(Affiliate, String)}
     */
    @Test
    @DisplayName("Test createCertificate(Affiliate, String)")
    void testCreateCertificate() {
        // Arrange
        when(arlInformationDao.findAllArlInformation())
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> certificateServiceImpl.createCertificate(affiliate, "42 Main St"));
        verify(arlInformationDao).findAllArlInformation();
    }

    /**
     * Test {@link CertificateServiceImpl#createCertificate(Affiliate, String)}.
     *
     * <ul>
     *   <li>Given {@link ArlInformation#ArlInformation()} Address is {@code 42 Main St}.
     *   <li>Then calls {@link
     *       IAffiliationEmployerDomesticServiceIndependentRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link CertificateServiceImpl#createCertificate(Affiliate, String)}
     */
    @Test
    @DisplayName(
            "Test createCertificate(Affiliate, String); given ArlInformation() Address is '42 Main St'; then calls findOne(Specification)")
    void testCreateCertificate_givenArlInformationAddressIs42MainSt_thenCallsFindOne() {
        // Arrange
        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setAddress("42 Main St");
        arlInformation.setCode("Code");
        arlInformation.setDv("Dv");
        arlInformation.setEmail("jane.doe@example.org");
        arlInformation.setId(1L);
        arlInformation.setName("Name");
        arlInformation.setNit("Nit");
        arlInformation.setOtherPhoneNumbers("6625550144");
        arlInformation.setPhoneNumber("6625550144");
        arlInformation.setWebsite("Website");

        ArrayList<ArlInformation> arlInformationList = new ArrayList<>();
        arlInformationList.add(arlInformation);
        when(arlInformationDao.findAllArlInformation()).thenReturn(arlInformationList);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(
                Mockito.<Specification<Affiliation>>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> certificateServiceImpl.createCertificate(affiliate, "42 Main St"));
        verify(arlInformationDao).findAllArlInformation();
        verify(iAffiliationEmployerDomesticServiceIndependentRepository)
                .findOne(isA(Specification.class));
    }

    /**
     * Test {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String, String)}.
     *
     * <ul>
     *   <li>Then return size is one.
     * </ul>
     *
     * <p>Method under test: {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String,
     * String)}
     */
    @Test
    @DisplayName("Test findByTypeDocumentAndNumberDocument(String, String); then return size is one")
    void testFindByTypeDocumentAndNumberDocument_thenReturnSizeIsOne() {
        // Arrange
        Certificate certificate = new Certificate();
        certificate.setAddress("42 Main St");
        certificate.setAddressedTo("42 Main St");
        certificate.setCity("Oxford");
        certificate.setCodeActivityEconomicPrimary("Code Activity Economic Primary");
        certificate.setCompany("Company");
        certificate.setCoverageDate(LocalDate.of(1970, 1, 1));
        certificate.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
        certificate.setDepartment("Department");
        certificate.setDependentWorkersNumber(3);
        certificate.setDocumentTypeContrator("Document Type Contrator");
        certificate.setEmail("jane.doe@example.org");
        certificate.setEndContractDate("2020-03-01");
        certificate.setExpeditionDate("2020-03-01");
        certificate.setFiledNumber("42");
        certificate.setId(1L);
        certificate.setIndependentWorkersNumber(3);
        certificate.setInitContractDate(LocalDate.of(1970, 1, 1));
        certificate.setMembershipDate(LocalDate.of(1970, 1, 1));
        certificate.setName("Name");
        certificate.setNameARL("Name ARL");
        certificate.setNameActivityEconomic("Name Activity Economic");
        certificate.setNameSignatureARL("Name Signature ARL");
        certificate.setNit("Nit");
        certificate.setNitContrator("Nit Contrator");
        certificate.setNumberDocument("42");
        certificate.setPhone("6625550144");
        certificate.setPosition("Position");
        certificate.setRetirementDate("2020-03-01");
        certificate.setRisk("Risk");
        certificate.setRiskRate("Risk Rate");
        certificate.setStatus("Status");
        certificate.setTypeDocument("Type Document");
        certificate.setValidatorCode("Validator Code");
        certificate.setVinculationSubType("Vinculation Sub Type");
        certificate.setVinculationType("Vinculation Type");

        ArrayList<Certificate> certificateList = new ArrayList<>();
        certificateList.add(certificate);
        when(certificateRepository.findByTypeDocumentAndNumberDocument(
                Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(certificateList);

        // Act
        List<Certificate> actualFindByTypeDocumentAndNumberDocumentResult =
                certificateServiceImpl.findByTypeDocumentAndNumberDocument(
                        "Type Document", "Identification");

        // Assert
        verify(certificateRepository)
                .findByTypeDocumentAndNumberDocument("Type Document", "Identification");
        assertEquals(1, actualFindByTypeDocumentAndNumberDocumentResult.size());
        assertSame(certificate, actualFindByTypeDocumentAndNumberDocumentResult.get(0));
        assertSame(certificateList, actualFindByTypeDocumentAndNumberDocumentResult);
    }

    /**
     * Test {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String, String)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliateNotFoundException}.
     * </ul>
     *
     * <p>Method under test: {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String,
     * String)}
     */
    @Test
    @DisplayName(
            "Test findByTypeDocumentAndNumberDocument(String, String); then throw AffiliateNotFoundException")
    void testFindByTypeDocumentAndNumberDocument_thenThrowAffiliateNotFoundException() {
        // Arrange
        when(certificateRepository.findByTypeDocumentAndNumberDocument(
                Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliateNotFoundException("An error occurred"));

        // Act and Assert
        assertThrows(
                AffiliateNotFoundException.class,
                () ->
                        certificateServiceImpl.findByTypeDocumentAndNumberDocument(
                                "Type Document", "Identification"));
        verify(certificateRepository)
                .findByTypeDocumentAndNumberDocument("Type Document", "Identification");
    }

    /**
     * Test {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String, String)}.
     *
     * <ul>
     *   <li>Then throw {@link CertificateNotFoundException}.
     * </ul>
     *
     * <p>Method under test: {@link CertificateServiceImpl#findByTypeDocumentAndNumberDocument(String,
     * String)}
     */
    @Test
    @DisplayName(
            "Test findByTypeDocumentAndNumberDocument(String, String); then throw CertificateNotFoundException")
    void testFindByTypeDocumentAndNumberDocument_thenThrowCertificateNotFoundException() {
        // Arrange
        when(certificateRepository.findByTypeDocumentAndNumberDocument(
                Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(
                CertificateNotFoundException.class,
                () ->
                        certificateServiceImpl.findByTypeDocumentAndNumberDocument(
                                "Type Document", "Identification"));
        verify(certificateRepository)
                .findByTypeDocumentAndNumberDocument("Type Document", "Identification");
    }

    /**
     * Test {@link CertificateServiceImpl#formatDate(LocalDate)}.
     *
     * <ul>
     *   <li>Then return {@code 1 días del mes de enero del 1970}.
     * </ul>
     *
     * <p>Method under test: {@link CertificateServiceImpl#formatDate(LocalDate)}
     */
    @Test
    @DisplayName("Test formatDate(LocalDate); then return '1 días del mes de enero del 1970'")
    void testFormatDate_thenReturn1DAsDelMesDeEneroDel1970() {
        // Arrange, Act and Assert
        assertEquals(
                "1 días del mes de enero del 1970",
                CertificateServiceImpl.formatDate(LocalDate.of(1970, 1, 1)));
    }

    @Test
    void createAndGenerateCertificate_whenAffiliateNotFound_shouldReturnMessage() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.empty());

        String result = certificateServiceImpl.createAndGenerateCertificate(findAffiliateReqDTO);

        assertEquals("Affiliate not found with the provided criteria.", result);
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void findByTypeDocumentAndNumberDocument_whenCertificatesFound_shouldReturnList() {
        when(certificateRepository.findByTypeDocumentAndNumberDocument("CC", "12345"))
                .thenReturn(Collections.singletonList(certificate));

        List<Certificate> result = certificateServiceImpl.findByTypeDocumentAndNumberDocument("CC", "12345");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(certificateRepository).findByTypeDocumentAndNumberDocument("CC", "12345");
    }

    @Test
    void findByTypeDocumentAndNumberDocument_whenNoCertificatesFound_shouldThrowException() {
        when(certificateRepository.findByTypeDocumentAndNumberDocument("CC", "12345"))
                .thenReturn(Collections.emptyList());

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateServiceImpl.findByTypeDocumentAndNumberDocument("CC", "12345");
        });
    }

    @Test
    void generateReportCertificate_whenCertificateNotFound_shouldThrowException() {
        when(certificateRepository.findByNumberDocumentAndValidatorCode(anyString(), anyString())).thenReturn(null);

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateServiceImpl.generateReportCertificate("12345", "INVALID");
        });
    }

    @Test
    void getValidateCodeCerticate_whenCodeIsInvalid_shouldThrowException() {
        when(certificateRepository.findByValidatorCode(anyString())).thenReturn(null);

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateServiceImpl.getValidateCodeCerticate("INVALID");
        });
    }

    @Test
    void getValidateCodeCerticate_whenCodeIsExpired_shouldThrowException() {
        String expiredCode = "VA"
                + LocalDate.now().minusMonths(2).format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"))
                + "123";
        when(certificateRepository.findByValidatorCode(expiredCode)).thenReturn(new Certificate());

        assertThrows(ErrorCodeValidationExpired.class, () -> {
            certificateServiceImpl.getValidateCodeCerticate(expiredCode);
        });
    }

    @Test
    void formatDate_shouldReturnCorrectFormat() {
        LocalDate date = LocalDate.of(2024, 7, 23);
        String formattedDate = CertificateServiceImpl.formatDate(date);
        assertEquals("23 días del mes de julio del 2024", formattedDate);
    }

    @Test
    void getValidateCodeQR_whenQrNotFound_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        when(iQrRepository.findById(qrId)).thenReturn(Optional.empty());

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateServiceImpl.getValidateCodeQR(qrId.toString());
        });
    }

    @Test
    void getValidateCodeQR_whenQrIsExpired_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        QrDocument qrDocument = new QrDocument();
        qrDocument.setIssueDate(LocalDateTime.now().minusDays(31));
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        CertificateNotFoundException exception = assertThrows(CertificateNotFoundException.class, () -> {
            certificateServiceImpl.getValidateCodeQR(qrId.toString());
        });
        assertEquals("Qr vencido", exception.getMessage());
    }


    @Test
    void getValidateCodeQR_forCertificateType_whenCertificateFound_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "VALID123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        Certificate certificate = new Certificate();
        certificate.setValidatorCode(validatorCode);
        certificate.setName("Test User");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("12345");
        certificate.setCompany("Test Company");
        certificate.setNitContrator("900123456-1");
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(certificate);

        QrDTO result = certificateServiceImpl.getValidateCodeQR(qrId.toString());

        assertEquals("certificado", result.getName());
        assertEquals("certificado", result.getType());
        assertEquals("Test User", result.getData().get("nombre"));
        assertEquals(validatorCode, result.getData().get("cod_validacion"));
        verify(iQrRepository).findById(qrId);
        verify(certificateRepository).findByValidatorCode(validatorCode);
    }

    @Test
    void getValidateCodeQR_forCertificateType_whenCertificateNotFound_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "INVALID123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(null);

        assertThrows(UserNotFoundInDataBase.class, () -> {
            certificateServiceImpl.getValidateCodeQR(qrId.toString());
        });
    }

    @Test
    void getValidateCodeQR_forNoAffiliateCertificate_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "NOAFFILIATE123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado no afiliado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIdentificationType("CC");
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(null);

        QrDTO result = certificateServiceImpl.getValidateCodeQR(qrId.toString());

        assertEquals("certificado no afiliado", result.getName());
        assertEquals("certificado", result.getType());
        assertEquals(validatorCode, result.getData().get("cod_validacion"));
        assertEquals("certificado no afiliado", result.getData().get("nombre"));
        verify(iQrRepository).findById(qrId);
        verify(certificateRepository).findByValidatorCode(validatorCode);
    }

    @Test
    void getValidateCodeQR_forFormularioType_whenAffiliationFound_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String filedNumber = "F12345";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("formulario de afiliacion");
        qrDocument.setIdentificationNumber(filedNumber);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("John");
        affiliation.setSecondName("Fitzgerald");
        affiliation.setSurname("Kennedy");
        affiliation.setSecondSurname(null);
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFiledNumber(filedNumber);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));

        QrDTO result = certificateServiceImpl.getValidateCodeQR(qrId.toString());

        assertEquals("formulario de afiliacion", result.getName());
        assertEquals("formulario", result.getType());
        assertEquals(filedNumber, result.getData().get("cod_validacion"));
        verify(iQrRepository).findById(qrId);
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findByFiledNumber(filedNumber);
    }

    @Test
    void saveMercantileCertificate_whenDataIsValid_shouldSaveAndReturnCertificate() throws Exception {
        // Arrange
        Long userPreRegisterId = 1L;
        Long economicActivityId = 10L;
        String documentNumber = "123456789";
        String documentType = "CC";
        String filedNumber = "F-CERT-001";
        String validationCode = "VC12345";

        Certificate certificate = new Certificate();

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber(documentNumber);
        affiliate.setDocumentType(documentType);
        affiliate.setAffiliationType("Mercantil");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("900123456-1");
        affiliate.setRetirementDate(null);
        affiliate.setAffiliationStatus("Activo");
        affiliate.setAffiliationDate(LocalDateTime.now().minusDays(5));

        UserMain userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSurname("Doe");

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setId(economicActivityId);
        economicActivity.setDescription("Test Activity");
        economicActivity.setClassRisk("IV");
        economicActivity.setCodeCIIU("1234");
        economicActivity.setAdditionalCode("A");

        AffiliateActivityEconomic primaryActivity = new AffiliateActivityEconomic();
        primaryActivity.setIsPrimary(true);
        primaryActivity.setActivityEconomic(economicActivity);

        AffiliateMercantile affiliation = new AffiliateMercantile();
        affiliation.setIdUserPreRegister(userPreRegisterId);
        affiliation.setEconomicActivity(Collections.singletonList(primaryActivity));

        when(iUserPreRegisterRepository.findById(userPreRegisterId)).thenReturn(Optional.of(userMain));
        when(iEconomicActivityRepository.findById(economicActivityId)).thenReturn(Optional.of(economicActivity));
        when(codeValidCertificationService.consultCode(documentNumber, documentType)).thenReturn(validationCode);
        when(filedService.getNextFiledNumberCertificate()).thenReturn(filedNumber);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        java.lang.reflect.Method method = CertificateServiceImpl.class.getDeclaredMethod("saveMercantileCertificate",
                Certificate.class, Affiliate.class, AffiliateMercantile.class);
        method.setAccessible(true);
        Certificate result = (Certificate) method.invoke(certificateServiceImpl, certificate, affiliate, affiliation);

        // Assert
        verify(iUserPreRegisterRepository).findById(userPreRegisterId);
        verify(iEconomicActivityRepository, org.mockito.Mockito.times(3)).findById(economicActivityId);
        verify(codeValidCertificationService).consultCode(documentNumber, documentType);
        verify(filedService).getNextFiledNumberCertificate();
        verify(certificateRepository, org.mockito.Mockito.times(2)).save(any(Certificate.class));

        assertEquals(documentType, result.getTypeDocument());
        assertEquals("John Doe", result.getName());
        assertEquals("Mercantil", result.getVinculationType());
        assertEquals("Test Company", result.getCompany());
        assertEquals("900123456-1", result.getNitContrator());
        assertEquals("No registra", result.getRetirementDate());
        assertEquals("Activo", result.getStatus());
        assertEquals("IV", result.getRisk());
        assertEquals("Test Activity", result.getNameActivityEconomic());
        assertEquals("IV1234A", result.getCodeActivityEconomicPrimary());
        assertEquals(filedNumber, result.getFiledNumber());
    }

    @Test
    void createCertificate_whenNoAffiliationFound_shouldThrowException() {
        // Arrange
        String addressedTo = "Fonodo Nacional del Ahorro";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F-UNKNOWN-001");
        affiliate.setDocumentNumber("99999");
        affiliate.setDocumentType("CC");

        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setName("ARL Test");
        arlInfo.setNit("900123456-1");

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            certificateServiceImpl.createCertificate(affiliate, addressedTo);
        });

        assertEquals(AffiliateNotFoundException.class, exception.getCause().getClass());

        verify(arlInformationDao).findAllArlInformation();
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findOne(any(Specification.class));
        verify(affiliateMercantileRepository).findOne(any(Specification.class));
        verify(affiliationDependentRepository).findOne(any(Specification.class));
        verify(certificateRepository, never()).save(any(Certificate.class));
    }
}
