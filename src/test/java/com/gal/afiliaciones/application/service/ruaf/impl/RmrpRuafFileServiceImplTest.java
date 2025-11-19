package com.gal.afiliaciones.application.service.ruaf.impl;

import com.gal.afiliaciones.application.service.ruaf.RuafFilesHelper;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.StagesCollection;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.stagescollection.StagesCollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RmrpRuafFileServiceImplTest {

    @Mock private RuafFilesHelper ruafFilesHelper;
    @Mock private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock private OccupationRepository occupationRepository;
    @Mock private StagesCollectionRepository stagesCollectionRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private MunicipalityRepository municipalityRepository;
    @Mock private AffiliationDetailRepository affiliationDetailRepository;
    @Mock private AffiliateRepository affiliateRepository;
    @Mock private AffiliationDependentRepository affiliationDependentRepository;
    @Mock private CollectProperties properties;

    @InjectMocks private RmrpRuafFileServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getFolderIdRuafRmrp()).thenReturn("some-folder-id");
        lenient().when(ruafFilesHelper.buildFileName(anyString())).thenReturn("test-file-name");
        lenient().when(ruafFilesHelper.findArlInformation()).thenReturn(ArlInformation.builder().code("14-23").build());
    }

    @Test
    void testGenerateRmrpFile_HappyPath() throws IOException {
        Affiliate affiliate = Affiliate.builder()
                .filedNumber("123")
                .affiliationDate(LocalDateTime.now())
                .build();
        when(affiliateRepository.findAllByAffiliationDateBetweenAndAffiliationTypeNotIn(any(), any(), anyList()))
                .thenReturn(List.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(anyString()))
                .thenReturn(Optional.of(new AffiliationDependent()));

        service.generateRmrpFile();

        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), any(), eq(true), any());
    }

    @Test
    void testBuildRegistersTypeOne() {
        int quantity = 5;
        String ruafFileType = "RMRP";
        ArlInformation arlInformation = ArlInformation.builder()
                .code("14-23")
                .build();

        LocalDateTime firstDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDateTime lastDate = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        when(ruafFilesHelper.findArlInformation()).thenReturn(arlInformation);
        when(ruafFilesHelper.buildFileName(ruafFileType)).thenReturn("RUA250RMRP20250711NI000860011153CO014-23.txt");

        byte[] bytes = ReflectionTestUtils.invokeMethod(service, "buildRegistersTypeOne", quantity,  firstDate.toLocalDate(), lastDate.toLocalDate());
        String result = new String(bytes);

        assertTrue(result.startsWith("1,14-23,"));
        assertTrue(result.contains("," + quantity + ","));
        assertTrue(result.endsWith(ruafFilesHelper.buildFileName(ruafFileType)));
    }

    @Test
    void testBuildRegistersTypeTwo() {
        List<String> fields = List.of("a","b","c");
        byte[] bytes = (byte[]) ReflectionTestUtils.invokeMethod(service, "buildRegistersTypeTwo", fields);
        String result = new String(bytes);
        assertEquals("2,a,b,c", result);
    }

    @Test
    void testValidateRealWorkers() {
        AffiliateMercantile mercantileLow = new AffiliateMercantile();
        mercantileLow.setRealNumberWorkers(100L);
        String resultLow = (String) ReflectionTestUtils.invokeMethod(service, "validateRealWorkers", mercantileLow);
        assertEquals("B", resultLow);

        AffiliateMercantile mercantileHigh = new AffiliateMercantile();
        mercantileHigh.setRealNumberWorkers(200L);
        String resultHigh = (String) ReflectionTestUtils.invokeMethod(service, "validateRealWorkers", mercantileHigh);
        assertEquals("A", resultHigh);
    }

    @Test
    void testValidateStageCollectionByAffiliation_NoDelay() {
        Affiliate affiliate = Affiliate.builder().documentType("DT").documentNumber("DN").build();
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("DT","DN"))
                .thenReturn(List.of());
        String result = (String) ReflectionTestUtils.invokeMethod(service, "validateStageCollectionByAffiliation", affiliate);
        assertEquals("1", result);
    }

    @Test
    void testValidateStageCollectionByAffiliation_WithDelay() {
        Affiliate affiliate = Affiliate.builder().documentType("DT").documentNumber("DN").build();
        StagesCollection sc = new StagesCollection();
        sc.setDaysOfDelay(5);
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("DT","DN"))
                .thenReturn(List.of(sc));
        String result = (String) ReflectionTestUtils.invokeMethod(service, "validateStageCollectionByAffiliation", affiliate);
        assertEquals("2", result);
    }

    @Test
    void testGenerateAffiliationDependentData_WithAffiliateMercantileOccupationAndDelay() throws Exception {
        AffiliationDependent dependent = AffiliationDependent.builder()
                .identificationDocumentType("IDT")
                .identificationDocumentNumber("IDN")
                .gender("F")
                .dateOfBirth(LocalDate.of(2000,1,1))
                .surname("S")
                .secondSurname("SS")
                .firstName("FN")
                .secondName("SN")
                .codeContributantType(2L)
                .economicActivityCode("EAC")
                .idDepartment(3L)
                .idCity(4L)
                .codeContributantSubtype("CS")
                .idWorkModality(1L)
                .codeContributantType(1L)
                .build();
        Affiliate affiliate = Affiliate.builder()
                .affiliationDate(LocalDateTime.of(2025,7,14,0,0))
                .nitCompany("NIT")
                .documentType("DT")
                .documentNumber("DN")
                .build();
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setTypeDocumentIdentification("TDI");
        merc.setNumberIdentification("NI");
        merc.setDigitVerificationDV(9);
        merc.setBusinessName("BN");
        merc.setRealNumberWorkers(250L);

        ArlInformation arlInformation = ArlInformation.builder()
                .code("14-23")
                .build();

        ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", dependent);
        ReflectionTestUtils.invokeMethod(service, "validateGenres", dependent);
        when(ruafFilesHelper.findArlInformation()).thenReturn(arlInformation);
        when(affiliateMercantileRepository.findFirstByNumberIdentification("NIT")).thenReturn(Optional.of(merc));
        Occupation occupation = new Occupation();
        occupation.setCodeOccupation("OCC");
        when(occupationRepository.findById(dependent.getIdOccupation())).thenReturn(Optional.of(occupation));
        StagesCollection sc = new StagesCollection();
        sc.setDaysOfDelay(3);
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("DT","DN")).thenReturn(List.of(sc));

        byte[] bytes = (byte[]) ReflectionTestUtils.invokeMethod(service, "generateAffiliationDependentData", dependent, affiliate);
        String result = new String(bytes);
        String[] fields = result.split(",");
        assertEquals(24, fields.length);
        assertEquals("TDI", fields[13]);
        assertEquals("NI", fields[14]);
        assertEquals("9", fields[15]);
        assertEquals("BN", fields[16]);
        assertEquals("A", fields[17]);
        assertEquals("OCC", fields[18]);
        assertEquals("2", fields[21]);
    }

    @Test
    void testGenerateAffiliationDependentData_NoAffiliateMercantileNoOccupationNoDelay() throws Exception {
        AffiliationDependent dependent = AffiliationDependent.builder()
                .identificationDocumentType("IDT")
                .identificationDocumentNumber("IDN")
                .gender("M")
                .dateOfBirth(LocalDate.of(2000,1,1))
                .surname("S")
                .secondSurname("SS")
                .firstName("FN")
                .secondName("SN")
                .codeContributantType(2L)
                .economicActivityCode("EAC")
                .idDepartment(3L)
                .idCity(4L)
                .codeContributantSubtype("CS")
                .idWorkModality(2L)
                .build();

        Affiliate affiliate = Affiliate.builder()
                .affiliationDate(LocalDateTime.of(2025,7,14,0,0))
                .nitCompany("NIT")
                .documentType("IDT")
                .documentNumber("IDN")
                .build();

        ArlInformation arlInformation = ArlInformation.builder()
                .code("14-23")
                .build();

        Affiliation domestic = Affiliation.builder()
                .firstName("FN")
                .secondName("SN")
                .surname("S")
                .secondSurname("SS")
                .build();

        when(affiliationDetailRepository.findByIdentificationDocumentNumber("NIT")).thenReturn(Optional.of(domestic));
        when(affiliateMercantileRepository.findFirstByNumberIdentification("NIT")).thenReturn(Optional.empty());
        ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", dependent);
        ReflectionTestUtils.invokeMethod(service, "validateGenres", dependent);
        when(ruafFilesHelper.findArlInformation()).thenReturn(arlInformation);
        when(municipalityRepository.findById(4L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(3L)).thenReturn(Optional.empty());
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("IDT","IDN")).thenReturn(List.of());
        when(ruafFilesHelper.buildName(dependent.getFirstName(), dependent.getSecondName(), dependent.getSurname(), dependent.getSecondSurname())).thenReturn("FN2 SN2 SUR SS2");

        byte[] bytes = ReflectionTestUtils.invokeMethod(service, "generateAffiliationDependentData", dependent, affiliate);
        String result = new String(bytes);
        String[] fields = result.split(",");
        assertEquals(24, fields.length);
        assertEquals("2", fields[0]);
        assertEquals("IDT", fields[1]);
        assertEquals("IDN", fields[2]);
        assertEquals("2000-01-01", fields[4]);
        assertEquals("", fields[13]);
        assertEquals("B", fields[17]);
        assertEquals("1", fields[21]);
        assertEquals("", fields[22]);
        assertEquals("2", fields[23]);
    }

    @Test
    void testGenerateAffiliationDetailData_NoOccupationNoDelay() throws Exception {
        Affiliation affiliationDetail = Affiliation.builder()
                .identificationDocumentType("IDT2")
                .identificationDocumentNumber("IDN2")
                .gender("F")
                .dateOfBirth(LocalDate.of(1990,2,2))
                .surname("SUR")
                .secondName("SN2")
                .firstName("FN2")
                .secondSurname("SS2")
                .occupation("occ")
                .codeContributantType(7L)
                .codeMainEconomicActivity("MEA")
                .idDepartmentIndependentWorker(1L)
                .idCityIndependentWorker(1L)
                .codeContributantSubtype("CS2")
                .codeContributantType(55L)
                .department(20L)
                .build();

        Affiliate affiliate = Affiliate.builder()
                .affiliationDate(LocalDateTime.of(2025,7,14,0,0))
                .documentType("DT2")
                .documentNumber("DN2")
                .build();

        ArlInformation arlInformation = ArlInformation.builder()
                .code("14-23")
                .build();

        ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", affiliationDetail);
        ReflectionTestUtils.invokeMethod(service, "validateGenres", affiliationDetail);
        when(ruafFilesHelper.findArlInformation()).thenReturn(arlInformation);
        when(municipalityRepository.findById(1L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        when(occupationRepository.findByNameOccupation("OCC")).thenReturn(Optional.empty());
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("DT2","DN2")).thenReturn(List.of());
        when(ruafFilesHelper.buildName(affiliationDetail.getFirstName(), affiliationDetail.getSecondName(), affiliationDetail.getSurname(), affiliationDetail.getSecondSurname())).thenReturn("FN2 SN2 SUR SS2");

        byte[] bytes = ReflectionTestUtils.invokeMethod(service, "generateAffiliationDetailData", affiliationDetail, affiliate);
        String result = new String(bytes);
        String[] fields = result.split(",");

        assertEquals("IDT2", fields[1]);
        assertEquals("IDN2", fields[2]);
        assertEquals("1990-02-02", fields[4]);
        assertEquals("55", fields[11]);
        assertEquals("MEA", fields[12]);
        assertEquals("IDT2", fields[13]);
        assertEquals("IDN2", fields[14]);
        assertEquals("", fields[15]);
        assertEquals("FN2 SN2 SUR SS2", fields[16]);
        assertEquals("I", fields[17]);
        assertEquals("", fields[18]);
        assertEquals("1", fields[21]);
    }

    @Test
    void testGenerateAffiliationDetailData_WithOccupationAndDelay() throws Exception {
        ArlInformation arlInformation = ArlInformation.builder()
                .code("14-23")
                .build();

        Affiliation affiliationDetail = Affiliation.builder()
                .identificationDocumentType("IDT3")
                .identificationDocumentNumber("IDN3")
                .gender("M")
                .dateOfBirth(LocalDate.of(1985,3,3))
                .surname("SUR3")
                .secondName("SN3")
                .firstName("FN3")
                .secondSurname("SS3")
                .occupation("occ")
                .codeContributantType(8L)
                .codeMainEconomicActivity("MEA3")
                .idDepartmentIndependentWorker(1L)
                .idCityIndependentWorker(1L)
                .codeContributantSubtype("CS3")
                .codeContributantType(55L)
                .build();

        Affiliate affiliate = Affiliate.builder()
                .affiliationDate(LocalDateTime.of(2025,7,14,0,0))
                .documentType("DT3")
                .documentNumber("DN3")
                .build();

        Occupation occupation = new Occupation();
        occupation.setCodeOccupation("OCCODE");
        when(occupationRepository.findByNameOccupation("OCC")).thenReturn(Optional.of(occupation));
        StagesCollection sc = new StagesCollection();
        sc.setDaysOfDelay(4);
        when(stagesCollectionRepository.findByContributorIdentificationTypeAndContributorIdentificationNumber("DT3","DN3")).thenReturn(List.of(sc));
        ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", affiliationDetail);
        ReflectionTestUtils.invokeMethod(service, "validateGenres", affiliationDetail);
        when(ruafFilesHelper.findArlInformation()).thenReturn(arlInformation);
        when(municipalityRepository.findById(1L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        byte[] bytes = (byte[]) ReflectionTestUtils.invokeMethod(service, "generateAffiliationDetailData", affiliationDetail, affiliate);
        String result = new String(bytes);
        String[] fields = result.split(",");
        assertEquals("OCCODE", fields[18]);
        assertEquals("2", fields[21]);
    }

    @Test
    void testValidateContributantCodes() {
        AffiliationDependent dependentValid = new AffiliationDependent();
        dependentValid.setCodeContributantType(1L);
        boolean resultValid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", dependentValid);
        assertTrue(resultValid);

        AffiliationDependent dependentInvalid = new AffiliationDependent();
        dependentInvalid.setCodeContributantType(99L);
        boolean resultInvalid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateContributantCodes", dependentInvalid);
        assertEquals(false, resultInvalid);
    }

    @Test
    void testValidateGenres() {
        Affiliation affiliationValid = new Affiliation();
        affiliationValid.setGender("F");
        boolean resultValid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateGenres", affiliationValid);
        assertTrue(resultValid);

        Affiliation affiliationInvalid = new Affiliation();
        affiliationInvalid.setGender("X");
        boolean resultInvalid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateGenres", affiliationInvalid);
        assertEquals(false, resultInvalid);
    }

    @Test
    void testValidateWorkModality() {
        AffiliationDependent dependentValid = new AffiliationDependent();
        dependentValid.setIdWorkModality(1L);
        boolean resultValid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateWorkModality", dependentValid);
        assertTrue(resultValid);

        AffiliationDependent dependentInvalid = new AffiliationDependent();
        dependentInvalid.setIdWorkModality(3L);
        boolean resultInvalid = (boolean) ReflectionTestUtils.invokeMethod(service, "validateWorkModality", dependentInvalid);
        assertEquals(false, resultInvalid);
    }

    @Test
    void testGenerateSubTypeContributant() {
        Affiliation affiliationValid = new Affiliation();
        affiliationValid.setCodeContributantSubtype("9");
        String resultValid = (String) ReflectionTestUtils.invokeMethod(service, "generateSubTypeContributant", affiliationValid);
        assertEquals("9", resultValid);

        Affiliation affiliationInvalid = new Affiliation();
        affiliationInvalid.setCodeContributantSubtype("99");
        String resultInvalid = (String) ReflectionTestUtils.invokeMethod(service, "generateSubTypeContributant", affiliationInvalid);
        assertEquals(null, resultInvalid);
    }
}