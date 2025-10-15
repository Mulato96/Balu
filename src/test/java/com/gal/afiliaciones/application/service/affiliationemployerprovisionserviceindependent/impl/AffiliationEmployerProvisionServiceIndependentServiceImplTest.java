package com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent.impl;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.ErrorAffiliationProvisionService;
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
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.*;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT) // evita UnnecessaryStubbing de stubs base
@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class AffiliationEmployerProvisionServiceIndependentServiceImplTest {

    @Mock IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock FiledService filedService;
    @Mock AlfrescoService alfrescoService;
    @Mock IUserPreRegisterRepository userPreRegisterRepository;
    @Mock IDataDocumentRepository dataDocumentRepository;
    @Mock AffiliateRepository affiliateRepository;
    @Mock CollectProperties properties;
    @Mock MessageErrorAge messageError;
    @Mock SendEmails sendEmails;
    @Mock GenerateCardAffiliatedService cardAffiliatedService;
    @Mock GenericWebClient webClient;

    @InjectMocks
    AffiliationEmployerProvisionServiceIndependentServiceImpl service;

    private ProvisionServiceAffiliationStep1DTO buildStep1DTO(boolean is723) {
        ProvisionServiceAffiliationStep1DTO dto = new ProvisionServiceAffiliationStep1DTO();
        dto.setId(0L);
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("123456");
        dto.setIs723(is723);

        ContractorDataStep1DTO contractor = new ContractorDataStep1DTO();
        contractor.setCompanyName("Empresa S.A.");
        contractor.setIdentificationDocumentTypeLegalRepresentative("CC");
        contractor.setIdentificationDocumentNumberContractorLegalRepresentative("10203040");
        contractor.setFirstNameContractor("Juan");
        contractor.setSurnameContractor("Perez");
        contractor.setEmailContractor("contacto@empresa.com");
        contractor.setCurrentARL("ARL_X");
        contractor.setIdentificationDocumentNumberContractor("900123456");
        dto.setContractorDataDTO(contractor);

        InformationIndependentWorkerDTO iw = new InformationIndependentWorkerDTO();
        iw.setFirstNameIndependentWorker("Ana");
        iw.setSecondNameIndependentWorker("Maria");
        iw.setSurnameIndependentWorker("Lopez");
        iw.setSecondSurnameIndependentWorker("Garcia");
        iw.setDateOfBirthIndependentWorker(LocalDate.now().minusYears(30));
        iw.setAge("30");
        iw.setGender("F");
        iw.setNationalityIndependentWorker(57L);
        iw.setHealthPromotingEntity(10L);
        iw.setPensionFundAdministrator(20L);
        iw.setPhone1IndependentWorker("3001112233");
        iw.setPhone2IndependentWorker("3100000000");
        iw.setEmailIndependentWorker("ana@example.com");

        AddressIndependentWorkerDTO addr = new AddressIndependentWorkerDTO();
        addr.setIdDepartmentIndependentWorker(11L);
        addr.setIdCityIndependentWorker(11001L);
        addr.setAddressIndependentWorker("Calle 1 # 2-3");
        iw.setAddressIndependentWorkerDTO(addr);

        dto.setInformationIndependentWorkerDTO(iw);
        return dto;
    }

    private UserMain buildUserMain() {
        UserMain u = new UserMain();
        u.setId(77L);
        u.setIdentificationType("CC");
        u.setIdentification("123456");
        u.setDateBirth(LocalDate.now().minusYears(30));
        return u;
    }

    @BeforeEach
    void baseStubs() {
        lenient().when(properties.getMinimumAge()).thenReturn(18);
        lenient().when(properties.getMaximumAge()).thenReturn(65);
    }

    @Test
    @DisplayName("Step1 OK")
    void createAffiliationProvisionServiceStep1_success() {
        ProvisionServiceAffiliationStep1DTO dto = buildStep1DTO(true);
        UserMain user = buildUserMain();

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC","123456"))
                .thenReturn(Optional.of(user));
        when(userPreRegisterRepository.findIdByUserName("CC-123456-EXT"))
                .thenReturn(Optional.of(999L));
        when(userPreRegisterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Affiliate savedAffiliate = new Affiliate();
        savedAffiliate.setIdAffiliate(555L);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(savedAffiliate);

        Affiliation savedAff = new Affiliation();
        savedAff.setId(1001L);
        when(repositoryAffiliation.save(any(Affiliation.class))).thenReturn(savedAff);

        ProvisionServiceAffiliationStep1DTO result = service.createAffiliationProvisionServiceStep1(dto);

        assertNotNull(result);
        assertEquals(1001L, result.getId());
        verify(userPreRegisterRepository).updateEPSandAFP(eq(77L), eq(10L), eq(20L));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

    @Test
    @DisplayName("Step1 - usuario no existe -> ErrorAffiliationProvisionService")
    void createAffiliationProvisionServiceStep1_userNotFound() {
        ProvisionServiceAffiliationStep1DTO dto = buildStep1DTO(false);
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC","123456"))
                .thenReturn(Optional.empty());

        assertThrows(ErrorAffiliationProvisionService.class, () -> service.createAffiliationProvisionServiceStep1(dto));
        verify(userPreRegisterRepository, never()).findIdByUserName(anyString());
    }

    @Test
    @DisplayName("Step2 OK, valida IBC y guarda")
    void createAffiliationProvisionServiceStep2_success() {
        SalaryDTO smlmv = new SalaryDTO();
        smlmv.setValue(1_300_000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(smlmv);

        Affiliation aff = new Affiliation();
        aff.setId(200L);
        when(repositoryAffiliation.findById(200L)).thenReturn(Optional.of(aff));
        when(repositoryAffiliation.save(any(Affiliation.class))).thenAnswer(inv -> inv.getArgument(0));

        ProvisionServiceAffiliationStep2DTO dto = new ProvisionServiceAffiliationStep2DTO();
        dto.setId(200L);
        dto.setCodeMainEconomicActivity(123L);

        AddressWorkDataCenterDTO work = new AddressWorkDataCenterDTO();
        work.setAddressWorkDataCenter("Dir");
        work.setIdCityWorkDataCenter(11001L);
        work.setIdDepartmentWorkDataCenter(11L);
        dto.setAddressWorkDataCenterDTO(work);

        ContractorDataStep2DTO c2 = new ContractorDataStep2DTO();
        c2.setStartDate(LocalDate.now());
        c2.setEndDate(LocalDate.now().plusMonths(6));
        c2.setDuration("6");
        c2.setContractMonthlyValue(new BigDecimal("2000000"));
        AddressContractDataStep2DTO addr2 = new AddressContractDataStep2DTO();
        addr2.setAddressContractDataStep2("X");
        addr2.setIdCityContractDataStep2(11001L);
        addr2.setIdDepartmentContractDataStep2(11L);
        c2.setAddressContractDataStep2DTO(addr2);
        dto.setContractorDataStep2DTO(c2);

        ProvisionServiceAffiliationStep2DTO out = service.createAffiliationProvisionServiceStep2(dto);
        assertEquals(200L, out.getId());
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

    @Test
    @DisplayName("Step2 - IBC inválido -> IllegalArgumentException")
    void createAffiliationProvisionServiceStep2_invalidIbc() {
        SalaryDTO smlmv = new SalaryDTO();
        smlmv.setValue(1_300_000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(smlmv);

        Affiliation aff = new Affiliation();
        aff.setId(201L);
        when(repositoryAffiliation.findById(201L)).thenReturn(Optional.of(aff));

        ProvisionServiceAffiliationStep2DTO dto = new ProvisionServiceAffiliationStep2DTO();
        dto.setId(201L);
        dto.setCodeMainEconomicActivity(999L);

        ContractorDataStep2DTO c2 = new ContractorDataStep2DTO();
        c2.setStartDate(LocalDate.now());
        c2.setEndDate(LocalDate.now().plusMonths(1));
        c2.setDuration("1");
        c2.setContractMonthlyValue(new BigDecimal("1000000")); // < SMLMV
        AddressContractDataStep2DTO addr2 = new AddressContractDataStep2DTO();
        addr2.setIdCityContractDataStep2(11001L);
        addr2.setIdDepartmentContractDataStep2(11L);
        c2.setAddressContractDataStep2DTO(addr2);
        dto.setContractorDataStep2DTO(c2);

        assertThrows(IllegalArgumentException.class, () -> service.createAffiliationProvisionServiceStep2(dto));
        verify(repositoryAffiliation, never()).save(any());
    }


    @Test
    @DisplayName("Step3 sin carpeta Alfresco no rompe")
    void createAffiliationProvisionServiceStep3_withoutFolder() throws Exception {
        Affiliation aff = new Affiliation();
        aff.setId(310L);
        aff.setIdentificationDocumentType("CC");
        aff.setIdentificationDocumentNumber("789000");
        when(repositoryAffiliation.findById(310L)).thenReturn(Optional.of(aff));
        when(repositoryAffiliation.save(any(Affiliation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userPreRegisterRepository.findIdByUserName("CC-789000-EXT")).thenReturn(Optional.of(42L));

        Affiliate affiliateSaved = new Affiliate();
        affiliateSaved.setIdAffiliate(1010L);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliateSaved);

        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025_0002");
        when(alfrescoService.getIdDocumentsFolder(nullable(String.class))).thenReturn(Optional.empty());

        ProvisionServiceAffiliationStep3DTO in = new ProvisionServiceAffiliationStep3DTO();
        in.setId(310L);

        ProvisionServiceAffiliationStep3DTO out = service.createAffiliationProvisionServiceStep3(in, Collections.emptyList());
        assertEquals("SOL_AFI_2025_0002", out.getFiledNumber());
        verify(dataDocumentRepository, never()).save(any());
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

    @Test
    @DisplayName("Step3 desde PILA - NOT_FOUND")
    void createProvisionServiceStep3FromPila_notFound() {
        when(repositoryAffiliation.findById(999L)).thenReturn(Optional.empty());
        ProvisionServiceAffiliationStep3DTO dto = new ProvisionServiceAffiliationStep3DTO();
        dto.setId(999L);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createProvisionServiceStep3FromPila(dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(cardAffiliatedService, sendEmails);
    }

    @Test
    @DisplayName("Step1 - edad fuera de rango -> ErrorAffiliationProvisionService")
    void createAffiliationProvisionServiceStep1_ageOutOfRange() {
        ProvisionServiceAffiliationStep1DTO dto = buildStep1DTO(false);
        UserMain user = buildUserMain();
        user.setDateBirth(LocalDate.now().minusYears(70));

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC","123456"))
                .thenReturn(Optional.of(user));
        when(messageError.messageError(anyString(), anyString())).thenReturn("edad inválida");

        assertThrows(ErrorAffiliationProvisionService.class, () -> service.createAffiliationProvisionServiceStep1(dto));
        verify(userPreRegisterRepository, never()).findIdByUserName(anyString());
    }
}
