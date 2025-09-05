package com.gal.afiliaciones.application.service.individualindependentaffiliation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation.IndividualIndependentAffiliationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import net.sf.jasperreports.engine.JRException;


class IndividualIndependentAffiliationServiceImplTest {

    private GenericWebClient genericWebClient;
    private IndividualIndependentAffiliationServiceImpl service;

    @BeforeEach
    void setUp() {
        genericWebClient = mock(GenericWebClient.class);
        service = new IndividualIndependentAffiliationServiceImpl(genericWebClient);
    }

    @Test
    void generatePdfReport_shouldCallGenerateReportCertificateWithCorrectParameters() throws JRException {
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();
        dto.setFiledNumber("12345");
        dto.setFilingDate("2023-04-01T00:00:00");
        dto.setAffiliationStartDate("2023-04-02T00:00:00");
        dto.setConsecutiveDoc("CON-001");

        dto.setIdentificationDocumentTypeGI("CC");
        dto.setIdentificationDocumentNumberGI("123456789");
        dto.setFullNameOrBusinessNameGI("John Doe");
        dto.setDateOfBirthGI("1990-01-01T00:00:00");
        dto.setGenderGI("M");
        dto.setNationalityGI("CO");
        dto.setCurrentHealthInsuranceGI("EPS");
        dto.setCurrentPensionFundGI("AFP");
        dto.setAddressGI("Address GI");
        dto.setDepartmentGI("Dept GI");
        dto.setCityOrDistrictGI("City GI");
        dto.setMobileOrLandlineGI("3001234567");
        dto.setEmailGI("email@gi.com");

        dto.setContractTypeACI("Type A");
        dto.setContractQualityACI("Quality A");
        dto.setTransportSupplyACI(true);
        dto.setContractStartDateACI("2023-04-03T00:00:00");
        dto.setContractEndDateACI("2023-05-03T00:00:00");
        dto.setNumberOfMonthsACI("12");
        dto.setEstablishedWorkShiftACI("Full time");
        dto.setTotalContractValueACI("10000");
        dto.setMonthlyContractValueACI("1000");
        dto.setBaseContributionIncomeACI("900");
        dto.setActivityCarriedACI("Activity A");
        dto.setEconomicActivityCodeACI("EAC123");
        dto.setJobPositionACI("Position A");
        dto.setTaxiDriverACI(true);
        dto.setAddressACI("Address ACI");
        dto.setDepartmentACI("Dept ACI");
        dto.setCityOrDistrictACI("City ACI");

        dto.setFullNameOrBusinessNameCI("Company CI");
        dto.setIdentificationDocumentTypeCI("NIT");
        dto.setIdentificationDocumentNumberCI("900123456");
        dto.setDvCI("1");
        dto.setEconomicActivityCodeCI("EACCI");
        dto.setAddressCI("Address CI");
        dto.setDepartmentCI("Dept CI");
        dto.setCityOrDistrictCI("City CI");
        dto.setMobileOrLandlineCI("3007654321");
        dto.setEmailCI("email@ci.com");

        dto.setFullNameOrBusinessNameICS("Signer ICS");
        dto.setIdentificationDocumentTypeICS("CC");
        dto.setIdentificationDocumentNumberICS("123123123");
        dto.setSignatureIndependent("signature");

        dto.setEconomicActivityCodeARL("EACARL");
        dto.setRiskClassARL("Class 3");
        dto.setFeeARL("0.52");

        // Create an instance of AffiliationIndependentVolunteerStep2DTO and set its fields accordingly
        AffiliationIndependentVolunteerStep2DTO step2DTO = new AffiliationIndependentVolunteerStep2DTO();
        dto.setAffiliationIndependentVolunteerStep2DTO(step2DTO);

        when(genericWebClient.generateReportCertificate(any())).thenReturn("pdf-content");

        String result = service.generatePdfReport(dto);

        assertEquals("pdf-content", result);

        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());

        CertificateReportRequestDTO capturedRequest = captor.getValue();
        assertEquals("19", capturedRequest.getIdReport());
        assertEquals("formulario afiliacion individual independientes", capturedRequest.getReportName());

        Map<String, Object> params = capturedRequest.getParameters();
        assertNotNull(params);
        assertEquals("12345", params.get("codeRadicated"));
        assertEquals("12345", params.get("radicado"));
        assertEquals("01/04/2023", params.get("fechaRadicado"));
        assertEquals("02/04/2023", params.get("fechaInicioAfiliacion"));
        assertEquals("CON-001", params.get(Constant.CONSECUTIVE_DOCUMENT));

        // Check some general info
        assertEquals("CC", params.get("tipoDocumentoIdentificacionGI"));
        assertEquals("123456789", params.get("numeroDocumentoIdentificacionGI"));
        assertEquals("John Doe", params.get("nombresApellidosRazonSocialGI"));
        assertEquals("01/01/1990", params.get("fechaNacimientoGI"));
        assertEquals("M", params.get("sexoGI"));
        assertEquals("CO", params.get("nacionalidadGI"));
        assertEquals("EPS", params.get("epsActualGI"));
        assertEquals("AFP", params.get("afpActualGI"));
        assertEquals("Address GI", params.get("direccionResidenciaGI"));
        assertEquals("Dept GI", params.get("departamentoGI"));
        assertEquals("City GI", params.get("municipioDistritoGI"));
        assertEquals("3001234567", params.get("celularTelefonoFijoGI"));
        assertEquals("email@gi.com", params.get("correoElectronicoGI"));

        // Check affiliate contract info
        assertEquals("Type A", params.get("tipoContratoACI"));
        assertEquals("Quality A", params.get("calidadContratoACI"));
        assertEquals("03/04/2023", params.get("fechaInicioContratoACI"));
        assertEquals("03/05/2023", params.get("fechaFinContratoACI"));
        assertEquals("12", params.get("numeroMesesACI"));
        assertEquals("Full time", params.get("jornadaEstablecidaACI"));
        assertEquals("10000", params.get("valorTotalContratoACI"));
        assertEquals("1000", params.get("valorMensualContratoACI"));
        assertEquals("900", params.get("ingresoBaseCotizacionACI"));
        assertEquals("Activity A", params.get("actividadEjecutarACI"));
        assertEquals("EAC123", params.get("codigoActividadEconomicaACI"));
        assertEquals("Position A", params.get("cargoOcupacionACI"));
        assertEquals(true, params.get("taxistaACI"));
        assertEquals("Address ACI", params.get("direccionACI"));
        assertEquals("Dept ACI", params.get("departamentoACI"));
        assertEquals("City ACI", params.get("municipioDistritoACI"));

        // Check contract signer info
        assertEquals("Company CI", params.get("nombresApellidosRazonSocialCI"));
        assertEquals("NIT", params.get("tipoDocumentoIdentificacionCI"));
        assertEquals("900123456", params.get("numeroDocumentoIdentificacionCI"));
        assertEquals("1", params.get("dvCI"));
        assertEquals("EACCI", params.get("codigoActividadEconomicaCI"));
        assertEquals("Address CI", params.get("direccionCI"));
        assertEquals("Dept CI", params.get("departamentoCI"));
        assertEquals("City CI", params.get("municipioDistritoCI"));
        assertEquals("3007654321", params.get("celularTelefonoFijoCI"));
        assertEquals("email@ci.com", params.get("correoElectronicoCI"));
        assertEquals("Signer ICS", params.get("nombresApellidosRazonSocialICS"));
        assertEquals("CC", params.get("tipoDocumentoIdentificacionICS"));
        assertEquals("123123123", params.get("numeroDocumentoIdentificacionICS"));
        assertEquals("signature", params.get("firmaIndependiente"));

        // Check ARL info
        assertEquals("EACARL", params.get("codigoActividadEconomicaARL"));
        assertEquals("Class 3", params.get("claseRiesgo"));
        assertEquals("0.52", params.get("tarifa"));

        // Check dangers map
        Object dangers = params.get("riesgos");
        assertNotNull(dangers);
        assertTrue(dangers instanceof Map);
    }

    @Test
    void dateFormatter_shouldReturnFormattedDate() throws Exception {
        String input = "2023-06-15T12:00:00";
        String expected = "15/06/2023";

        // Using reflection to test private method
        var method = IndividualIndependentAffiliationServiceImpl.class.getDeclaredMethod("dateFormatter", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, input);

        assertEquals(expected, result);
    }

    @Test
    void dateFormatter_shouldReturnEmptyStringForNull() throws Exception {
        var method = IndividualIndependentAffiliationServiceImpl.class.getDeclaredMethod("dateFormatter", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, (String) null);

        assertEquals("", result);
    }

    @Test
    void defaultIfNullOrEmpty_shouldReturnNAForNullOrEmpty() {
        assertEquals("N/A", IndividualIndependentAffiliationServiceImpl.defaultIfNullOrEmpty(null));
        assertEquals("N/A", IndividualIndependentAffiliationServiceImpl.defaultIfNullOrEmpty(""));
    }

    @Test
    void defaultIfNullOrEmpty_shouldReturnValueIfNotEmpty() {
        assertEquals("value", IndividualIndependentAffiliationServiceImpl.defaultIfNullOrEmpty("value"));
    }
}