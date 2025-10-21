package com.gal.afiliaciones.application.service.affiliationtaxidriverindependent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationIndependentTaxiDriverStep3DTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationtaxidriverindependent.impl.AffiliationTaxiDriverIndependentServiceImpl;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentDAO;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentCreateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentPreLoadDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ContextConfiguration(classes = {AffiliationTaxiDriverIndependentServiceImpl.class})
@ExtendWith(SpringExtension.class)
class AffiliationTaxiDriverIndependentServiceImplTest {

    @MockBean
    private AffiliationTaxiDriverIndependentDAO dao;

    @MockBean
    private AffiliateService affiliateService;

    @MockBean
    private GenericWebClient webClient;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    @MockBean
    private IDataDocumentRepository dataDocumentRepository;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private FiledService filedService;

    @MockBean
    private MainOfficeService mainOfficeService;

    @MockBean
    private WorkCenterService workCenterService;

    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private CollectProperties properties;

    @MockBean
    private IEconomicActivityRepository economicActivityRepository;

    @MockBean
    private IUserRegisterService userRegisterService;

    @MockBean
    private IEconomicActivityService economicActivityService;

    @MockBean
    private MessageErrorAge messageError;

    @MockBean
    private RiskFeeService riskFeeService;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private GenerateCardAffiliatedService cardAffiliatedService;

    @MockBean
    private AffiliationEmployerActivitiesMercantileService mercantileService;

    @Autowired
    private AffiliationTaxiDriverIndependentServiceImpl service;

    @BeforeEach
    void setUp() {
        // Shared setup for tests can go here
    }

    @Test
    void testCreateAffiliationStep3FromPila_Success() {
        AffiliationIndependentTaxiDriverStep3DTO dto = new AffiliationIndependentTaxiDriverStep3DTO();
        dto.setIdAffiliation(1L);
        dto.setRisk("I");
        dto.setPrice(BigDecimal.valueOf(100));

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("FN123");

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("FN123");


        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findByFiledNumber("FN123")).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(affiliation);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);

        Affiliation result = service.createAffiliationStep3FromPila(dto);

        assertNotNull(result);
        assertEquals("I", result.getRisk());
        verify(cardAffiliatedService).createCardWithoutOtp("FN123");
        verify(sendEmails).welcome(any(), any(), any(), any());
    }

    @Test
    void testCreateAffiliationStep3FromPila_AffiliateNotFound() {
        AffiliationIndependentTaxiDriverStep3DTO dto = new AffiliationIndependentTaxiDriverStep3DTO();
        dto.setIdAffiliation(1L);

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("FN123");

        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findByFiledNumber("FN123")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createAffiliationStep3FromPila(dto));
    }

    @Test
    void testPreloadMercantileNotExists_WithDecreeError() {
        String identificationType = "NIT";
        String identification = "900123456";
        String error = "Some error containing Decreto 1072 de 2015";

        AffiliationTaxiDriverIndependentPreLoadDTO result = service.preloadMercantileNotExists(identificationType, identification, error);

        assertNotNull(result);
        assertEquals(identificationType, result.getContractorIdentificationType());
        assertEquals(identification, result.getContractorIdentificationNumber());
        assertEquals(true, result.getIs723());
    }

    @Test
    void testPreloadMercantileNotExists_WithoutDecreeError() {
        String identificationType = "NIT";
        String identification = "900123456";
        String error = "Some other error";

        AffiliationTaxiDriverIndependentPreLoadDTO result = service.preloadMercantileNotExists(identificationType, identification, error);

        assertNotNull(result);
        assertEquals(identificationType, result.getContractorIdentificationType());
        assertEquals(identification, result.getContractorIdentificationNumber());
        assertEquals(false, result.getIs723());
    }


    @Test
    void testCreateAffiliationUserNotFound() {
        AffiliationTaxiDriverIndependentCreateDTO dto = new AffiliationTaxiDriverIndependentCreateDTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("123456");

        when(dao.findPreloadedData(anyString(), anyString())).thenReturn(Collections.emptyList());

        assertThrows(NullPointerException.class, () -> service.createAffiliation(dto));
    }

    @Test
    void testPreloadAffiliationDataSuccess() {
        String identificationType = "NI";
        String identification = "600123123";
        String independentType = "Taxista";
        String identificationTypeIndependent = "CC";
        String identificationIndependent = "123456";

        ConsultIndependentWorkerDTO consultIndependentWorkerDTO = new ConsultIndependentWorkerDTO();
        consultIndependentWorkerDTO.setWorkerDocumentType(identificationTypeIndependent);
        consultIndependentWorkerDTO.setWorkerDocumentNumber(identificationIndependent);

        ResponseConsultWorkerDTO consultWorkerDTO = new ResponseConsultWorkerDTO();
        consultWorkerDTO.setCausal(99L); // Ensure this value is not in arrayCausal

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);
        affiliateMercantile.setNumberIdentification("600123123");
        affiliateMercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliateMercantile.setAddress("123 Main St"); // Mock valid address

        EconomicActivity economicActivity1 = new EconomicActivity();
        economicActivity1.setId(101L);
        AffiliateActivityEconomic affiliateActivityEconomic =  new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity1);

        affiliateMercantile.setEconomicActivity(List.of(affiliateActivityEconomic));


        EconomicActivityDTO activityTaxiDriver = new EconomicActivityDTO();
        activityTaxiDriver.setId(1L); // Match the economic activity ID

        // Mock the list returned by affiliateMercantileRepository.findAll
        List<AffiliateMercantile> validMercantileList = Collections.singletonList(affiliateMercantile);

        // Mock the repository calls
        when(webClient.consultWorkerDTO(any(ConsultIndependentWorkerDTO.class))).thenReturn(consultWorkerDTO);
        when(affiliateMercantileRepository.findByTypeDocumentIdentificationAndNumberIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(economicActivityService.getEconomicActivityByCode(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER))
                .thenReturn(activityTaxiDriver);
        when(economicActivityRepository.findById(1L)).thenReturn(Optional.of(new EconomicActivity()));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(validMercantileList);
        when(affiliateMercantileRepository.findById(1L)).thenReturn(Optional.of(affiliateMercantile)); // Mock for getWorkCenterAddress

        AffiliationTaxiDriverIndependentPreLoadDTO result = service.preloadAffiliationData(
                identificationType, identification, independentType, identificationTypeIndependent, identificationIndependent, 0L);

        assertNotNull(result);
        assertEquals(identification, result.getContractorIdentificationNumber());
        assertFalse(result.getIs723());

        verify(economicActivityService, atLeastOnce()).getEconomicActivityByCode(Constant.CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER);
        verify(affiliateMercantileRepository, atLeastOnce()).findAll(any(Specification.class));
        verify(affiliateMercantileRepository, atLeastOnce()).findById(1L); // Verify call for getWorkCenterAddress
    }


    @Test
    void testPreloadAffiliationDataUserNotPreRegistered() {
        String identificationType = "NI";
        String identification = "600111111";
        String independentType = "Taxista";
        String identificationTypeIndependent = "CC";
        String identificationIndependent = "123456";

        UserMain mockUserMain = new UserMain();
        mockUserMain.setStatusPreRegister(false); // Simulando que el usuario no está pre-registrado

        when(dao.findPreloadedData(anyString(), anyString())).thenReturn(Collections.singletonList(mockUserMain));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.preloadAffiliationData(identificationType, identification, independentType,
                        identificationTypeIndependent, identificationIndependent, 0L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }



    @Test
    void testUpdateAffiliationSuccess() {
        // Arrange
        WorkCenterAddressIndependentDTO workCenterAddressIndependent = new WorkCenterAddressIndependentDTO();
        workCenterAddressIndependent.setAddressWorkDataCenter("123 Main St");

        AffiliationTaxiDriverIndependentUpdateDTO dto = new AffiliationTaxiDriverIndependentUpdateDTO();
        dto.setId(1L);
        dto.setWorkCenter(new WorkCenterAddressIndependentDTO());
        dto.setEconomicActivity(new EconomicActivityDTO());
        dto.setMonthlyContractValue(new BigDecimal("3000000"));
        dto.setBaseIncome(new BigDecimal("1200000")); // Use contractIbcValue instead of baseIncome
        dto.setStartDate(LocalDate.of(2023, 1, 1));
        dto.setEndDate(LocalDate.of(2023, 12, 31));
        dto.setDuration("12 months");
        dto.setTotalContractValue(new BigDecimal("36000000"));
        dto.setWorkCenter(workCenterAddressIndependent);

        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(100L);

        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(200L);

        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1160000L);

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("CIIU123");

        when(dao.findAffiliationById(Mockito.<Long>any())).thenReturn(Optional.of(affiliation));
        when(mainOfficeService.findCode()).thenReturn("MO123");
        when(mainOfficeService.saveMainOffice(any(MainOffice.class))).thenReturn(mainOffice);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenReturn(workCenter);
        when(webClient.getSmlmvByYear(any(Integer.class))).thenReturn(salaryDTO);
        when(economicActivityRepository.findById(Mockito.<Long>any())).thenReturn(Optional.of(economicActivity));
        when(dao.updateAffiliation(Mockito.<Affiliation>any())).thenReturn(Optional.of(affiliation));

        // Act
        service.updateAffiliation(dto);

        // Assert
        verify(dao, times(1)).findAffiliationById(dto.getId());
        verify(mainOfficeService, times(1)).saveMainOffice(any(MainOffice.class));
        verify(webClient, times(1)).getSmlmvByYear(any(Integer.class));

        // Now we validate the assigned contractIbcValue directly from DTO
        assertEquals(new BigDecimal("1200000"), affiliation.getContractIbcValue());
        assertEquals(LocalDate.of(2023, 1, 1), affiliation.getContractStartDate());
        assertEquals(LocalDate.of(2023, 12, 31), affiliation.getContractEndDate());
        assertEquals("12 months", affiliation.getContractDuration());
        assertEquals(new BigDecimal("36000000"), affiliation.getContractTotalValue());
    }


    @Test
    void testUpdateAffiliationNotFound() {
        AffiliationTaxiDriverIndependentUpdateDTO dto = new AffiliationTaxiDriverIndependentUpdateDTO();
        dto.setId(1L);

        when(dao.findAffiliationById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateAffiliation(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Afiliación no encontrada", exception.getReason());
    }


}
