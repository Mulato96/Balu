package com.gal.afiliaciones.application.service.affiliatecompany.mapper;


import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AffiliateCompanyMapperTest {


    @Mock
    AffiliateDataService dataService;
    @InjectMocks
    AffiliateCompanyMapper service;

    @ParameterizedTest
    @CsvSource({
            "Activa, Activo",
            "Inactiva, Inactivo",
            "Pendiente, Pendiente",
            "'', ''"
    })
    void testMapAffiliateStatusToRaw(String input, String expected) {
        assertEquals(expected, service.mapAffiliateStatusToRaw(input));
    }

    @Test
    void mapAffiliateStatusToRaw_null(){
        String response = service.mapAffiliateStatusToRaw(null);
        assertNull(response);
    }

    @Test
    void mapSalary_null(){
        Double response = service.mapSalary(null);
        assertNull(response);
    }

    @Test
    void mapSalary_exception(){
        Double response =  service.mapSalary("");
        assertNull(response);
    }

    @Test
    void mapSalary(){
        Double response =  service.mapSalary(1.0);
        assertNotNull(response);
    }

    @ParameterizedTest
    @CsvSource({
            "null, 1",
            "1, 1",
            "texto, 1"
    })
    void parseIntSafely(String value, Integer number){
        assertEquals(number, service.parseIntSafely(value, number));
    }

    @Test
    void formatDate_null(){
        assertNull(service.formatDate(null));
    }

    @Test
    void formatDate(){
        assertNotNull(service.formatDate(LocalDate.now()));
    }

    @Test
    void formatDateTime_null(){
        assertNull(service.formatDateTime(null));
    }

    @Test
    void formatDateTime(){
        assertNotNull(service.formatDateTime(LocalDateTime.now()));
    }

    @Test
    void mapLocationData(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        Department department = new Department();
        Municipality municipality = new Municipality();

        when( dataService.findDepartment(1L))
                .thenReturn(Optional.of(department));
        when(dataService.findMunicipality(1L))
                .thenReturn(Optional.of(municipality));

        service.mapLocationData(dtoBuilder, 1L, 1L);
        verify(dataService, times(1)).findMunicipality( 1L);
    }

    @Test
    void mapOccupationData_id(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        Occupation occupation = new Occupation();

        when( dataService.findOccupation(1L))
                .thenReturn(Optional.of(occupation));

        service.mapOccupationData(dtoBuilder, 1L, "");
        verify(dataService, times(1)).findOccupation(1L);
    }

    @Test
    void mapOccupationData_name(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        Occupation occupation = new Occupation();
        occupation.setIdOccupation(1L);

        when( dataService.findOccupation(1L))
                .thenReturn(Optional.empty());
        when(dataService.findOccupationByName(""))
                .thenReturn(Optional.of(occupation));

        service.mapOccupationData(dtoBuilder, null, "");
        verify(dataService, times(0)).findOccupation(1L);
    }

    @Test
    void mapFinancialEntities(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        FundPension fundPension =  new FundPension();
        Health health = new Health();
        Arl arl = new Arl();

        when(dataService.findPensionFund(1L))
                .thenReturn(Optional.of(fundPension));
        when(dataService.findHealthEntity(1L))
                .thenReturn(Optional.of(health));
        when(dataService.findArlByCode(""))
                .thenReturn(Optional.of(arl));

        service.mapFinancialEntities(dtoBuilder, 1L, 1L, "");
        verify(dataService, times(1)).findHealthEntity(1L);
        verify(dataService, times(1)).findPensionFund(1L);
        verify(dataService, times(1)).findArlByCode("");
    }

    @Test
    void mapEconomicActivity(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        EconomicActivity economicActivity =  new EconomicActivity();

        when(dataService.findEconomicActivity("1"))
                .thenReturn(Optional.of(economicActivity));

        service.mapEconomicActivity(dtoBuilder,  "1");
        verify(dataService, times(1)).findEconomicActivity("1");
    }

    @Test
    void mapEconomicActivity_null(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        EconomicActivity economicActivity =  new EconomicActivity();

        when(dataService.findEconomicActivity(""))
                .thenReturn(Optional.of(economicActivity));

        service.mapEconomicActivity(dtoBuilder,  "");
        verify(dataService, times(0)).findEconomicActivity("");
    }

    @Test
    void mapAffiliateBasicData(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        Affiliate affiliate1 = new Affiliate();
        affiliate1.setCoverageStartDate(LocalDate.now());
        affiliate1.setRetirementDate(LocalDate.now());
        affiliate1.setCoverageStartDate(LocalDate.now());
        affiliate1.setNitCompany("123");
        affiliate1.setCompany("123");
        affiliate1.setAffiliationStatus("Otro");

        FundPension fundPension =  new FundPension();
        Health health = new Health();
        Arl arl = new Arl();

        when(dataService.findPensionFund(1L))
                .thenReturn(Optional.of(fundPension));
        when(dataService.findHealthEntity(1L))
                .thenReturn(Optional.of(health));
        when(dataService.findArlByCode(""))
                .thenReturn(Optional.of(arl));

        service.mapAffiliateBasicData(dtoBuilder, affiliate1);
        verifyNoInteractions(dataService);
    }


    @ParameterizedTest
    @CsvSource({
            ",1970001,",
            "1,1970001,1",
            "1,9999999,3",
            "2,1970001,34",
            "3,1970001,35",
            "4,1970001,0",
            "10,1970001,3"
    })
    void testConvertTipoVinculadoDependent(Long idBondingType, String economicActivity, Integer expected) {
        // when
        Integer result = service.convertTipoVinculadoDependent(idBondingType, economicActivity);

        // then
        assertEquals(expected, result);
    }

    @Test
    void mapMainOfficeData(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        UserMain userMain = new UserMain();

        MainOffice mainOffice = new MainOffice();
        mainOffice.setAddress("");
        mainOffice.setMainOfficePhoneNumber("");
        mainOffice.setMainOfficeEmail("");
        mainOffice.setMainOfficeZone("");
        mainOffice.setOfficeManager(userMain);
        mainOffice.setIdDepartment(1L);
        mainOffice.setIdCity(1L);

        when( dataService.findMainOfficeByAffiliate(1L))
                .thenReturn(Optional.of(mainOffice));

        service.mapMainOfficeData(dtoBuilder, 1L);
        verify(dataService, times(1)).findMainOfficeByAffiliate(1L);
    }

    @Test
    void mapMainOfficeData_null(){

        AffiliateCompanyDbApproxResponseDTO.AffiliateCompanyDbApproxResponseDTOBuilder dtoBuilder =
                AffiliateCompanyDbApproxResponseDTO.builder();

        service.mapMainOfficeData(dtoBuilder, null);
        verifyNoInteractions(dataService);
    }

}
