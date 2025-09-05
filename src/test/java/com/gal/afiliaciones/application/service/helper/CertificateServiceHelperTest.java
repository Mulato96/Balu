package com.gal.afiliaciones.application.service.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class CertificateServiceHelperTest {

    @InjectMocks
    private CertificateServiceHelper certificateServiceHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void transformToNoAffiliateCertificate() {
        Certificate certificate = new Certificate();
        certificate.setNit("123456789");
        certificate.setValidatorCode("VAL123");
        certificate.setNumberDocument("987654321");
        certificate.setCity("Bogota");
        certificate.setExpeditionDate("2023-01-01");
        certificate.setNameSignatureARL("Firma ARL");
        certificate.setNameARL("ARL Sura");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToNoAffiliateCertificate(certificate);

        assertEquals(Constant.TYPE_NOT_AFFILLATE, reportRequestDTO.getReportName());
        assertNull(reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("123456789", parameters.get(Constant.NIT));
        assertEquals("VAL123", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("987654321", parameters.get(Constant.IDENTIFICATION));
        assertEquals("Bogota", parameters.get(Constant.CITY));
        assertEquals("2023-01-01", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("Firma ARL", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("ARL Sura", parameters.get(Constant.NAME_ARL_LABEL));
    }

    @Test
    void transformIndependentWorkerAffiliationCertificateOpsAnd723() {
        Certificate certificate = new Certificate();
        certificate.setVinculationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        certificate.setNitContrator("999999999");
        certificate.setNit("111111111");
        certificate.setCoverageDate(LocalDate.of(2023, 2, 1));
        certificate.setStatus("Activo");
        certificate.setCity("Medellin");
        certificate.setExpeditionDate("2023-02-02");
        certificate.setValidatorCode("VAL456");
        certificate.setNameSignatureARL("Firma ARL 2");
        certificate.setNameARL("ARL Colpatria");
        certificate.setRisk("Riesgo 3");
        certificate.setInitContractDate(LocalDate.of(2023, 2, 3));
        certificate.setCompany("Empresa XYZ");
        certificate.setFiledNumber("123");
        certificate.setEndContractDate("2023-02-04");
        certificate.setRetirementDate("2023-02-05");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("444444444");
        certificate.setName("Juan Perez");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformIndependentWorkerAffiliationCertificateOpsAnd723(certificate);

        assertEquals("certificado basico afiliacion trabajador independiente voluntario, ops y 723", reportRequestDTO.getReportName());
        assertEquals("24", reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("NIT", parameters.get(Constant.CONTRACTOR_DOCUMENT_TYPE));
        assertEquals("999999999", parameters.get(Constant.CONTRACTOR_DOCUMENT_NUMBER));
        assertEquals("111111111", parameters.get(Constant.NIT));
        assertEquals(certificate.getVinculationType(), parameters.get(Constant.VINCULATION_TYPE));
        assertEquals("01/02/2023", parameters.get(Constant.COVERAGE_DATE));
        assertEquals("Activo", parameters.get(Constant.STATUS_FIELD));
        assertEquals("Medellin", parameters.get(Constant.CITY));
        assertEquals("2023-02-02", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("VAL456", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("Firma ARL 2", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("ARL Colpatria", parameters.get(Constant.NAME_ARL_LABEL));
        assertEquals("Riesgo 3", parameters.get(Constant.RISK));
        assertEquals("03/02/2023", parameters.get(Constant.INIT_CONTRACT_DATE));
        assertEquals("Empresa XYZ", parameters.get(Constant.NAME_CONTRACTOR));
        assertEquals("123", parameters.get(Constant.CONSECUTIVE_DOCUMENT));
        assertEquals("04/02/2023", parameters.get(Constant.END_CONTRACT_DATE));
        assertEquals("05/02/2023", parameters.get(Constant.RETIREMENT_DATE));
        assertEquals("CC. 444444444", parameters.get(Constant.IDENTIFICATION));
        assertEquals("Juan Perez", parameters.get(Constant.NAME));
    }

    @Test
    void transformToDependentWorkerCertificate() {
        Certificate certificate = new Certificate();
        certificate.setNit("777777777");
        certificate.setCompany("Company ABC");
        certificate.setDocumentTypeContrator("NIT");
        certificate.setNitContrator("888888888");
        certificate.setTypeDocument("TI");
        certificate.setNumberDocument("555555555");
        certificate.setMembershipDate(LocalDate.of(2023, 3, 1));
        certificate.setInitContractDate(LocalDate.of(2023, 3, 2));
        certificate.setCoverageDate(LocalDate.of(2023, 3, 3));
        certificate.setRisk("Riesgo 2");
        certificate.setName("Maria Rodriguez");
        certificate.setStatus("Inactivo");
        certificate.setVinculationType("Dependiente");
        certificate.setRetirementDate("2023-03-03");
        certificate.setCity("Cali");
        certificate.setExpeditionDate("2023-03-04");
        certificate.setValidatorCode("VAL789");
        certificate.setNameSignatureARL("Firma ARL 3");
        certificate.setNameARL("ARL AXA");
        certificate.setFiledNumber("456");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToDependentWorkerCertificate(certificate);

        assertEquals("certificado basico afiliacion trabajador dependiente, estudiante enpráctica y aprendiz sena", reportRequestDTO.getReportName());
        assertEquals("25", reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("777777777", parameters.get(Constant.NIT));
        assertEquals("Company ABC", parameters.get(Constant.COMPANY_NAME));
        assertEquals("NIT", parameters.get(Constant.COMPANY_NIT));
        assertEquals("888888888", parameters.get(Constant.COMPANY_NUMBER));
        assertEquals("TI. 555555555", parameters.get(Constant.IDENTIFICATION));
        assertEquals("01/03/2023", parameters.get(Constant.MEMBERSHIP_DATE));
        assertEquals("03/03/2023", parameters.get(Constant.COVERAGE_DATE));
        assertEquals("Riesgo 2", parameters.get(Constant.RISK));
        assertEquals("Maria Rodriguez", parameters.get(Constant.NAME));
        assertEquals("Inactivo", parameters.get(Constant.STATUS_FIELD));
        assertEquals("Dependiente", parameters.get(Constant.VINCULATION_TYPE));
        assertEquals("03/03/2023", parameters.get(Constant.RETIREMENT_DATE));
        assertEquals("Cali", parameters.get(Constant.CITY));
        assertEquals("2023-03-04", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("VAL789", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("Firma ARL 3", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("ARL AXA", parameters.get(Constant.NAME_ARL_LABEL));
        assertEquals("456", parameters.get(Constant.CONSECUTIVE_DOCUMENT));
    }

    @Test
    void transformToBasicAffiliationCertificateEmployerAndDomesticService_Domestic() {
        Certificate certificate = new Certificate();
        certificate.setVinculationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("10101010");
        certificate.setName("Domestica Maria");
        certificate.setEndContractDate("2024-01-01");
        certificate.setMembershipDate(LocalDate.of(2023, 1, 1));
        certificate.setInitContractDate(LocalDate.of(2023, 1, 2));
        certificate.setStatus("Activo");
        certificate.setCity("Pereira");
        certificate.setExpeditionDate("2023-01-03");
        certificate.setCoverageDate(LocalDate.of(2023, 1, 3));
        certificate.setValidatorCode("VAL000");
        certificate.setNameSignatureARL("Firma ARL Domestica");
        certificate.setNameARL("ARL Bolivar");
        certificate.setFiledNumber("789");
        certificate.setNit("900000000");

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setDescription("Actividad Economica Domestica");
        economicActivity.setClassRisk("Riesgo I");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToBasicAffiliationCertificateEmployerAndDomesticService(certificate, economicActivity);

        assertEquals("certificado basico afiliacion empleador y servicio domestico", reportRequestDTO.getReportName());
        assertEquals("23", reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("1970001", parameters.get(Constant.ECONOMY_ACTIVITY));
        assertEquals("Actividad Economica Domestica", parameters.get(Constant.NAME_ACTIVITY));
        assertEquals("Riesgo I", parameters.get(Constant.RISK));
        assertEquals("CC. 10101010", parameters.get(Constant.DOCUMENT_IDENTIFIER));
        assertEquals("Domestica Maria", parameters.get(Constant.NAME));
        assertEquals("01/01/2024", parameters.get(Constant.INACTIVATION_DATE));
        assertEquals("01/01/2023", parameters.get(Constant.MEMBERSHIP_DATE));
        assertEquals("789", parameters.get(Constant.CONSECUTIVE_DOCUMENT));
        assertEquals("900000000", parameters.get(Constant.NIT));
        assertEquals(certificate.getVinculationType(), parameters.get(Constant.VINCULATION_TYPE));
        assertEquals("03/01/2023", parameters.get(Constant.COVERAGE_DATE));
        assertEquals("Activo", parameters.get(Constant.STATUS_FIELD));
        assertEquals("Pereira", parameters.get(Constant.CITY));
        assertEquals("2023-01-03", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("VAL000", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("Firma ARL Domestica", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("ARL Bolivar", parameters.get(Constant.NAME_ARL_LABEL));
    }

    @Test
    void transformToBasicAffiliationCertificateEmployerAndDomesticService_Employer() {
        Certificate certificate = new Certificate();
        certificate.setVinculationType(Constant.TYPE_AFFILIATE_EMPLOYER);
        certificate.setCodeActivityEconomicPrimary("1234");
        certificate.setNameActivityEconomic("Actividad Economica Principal");
        certificate.setRisk("Riesgo V");
        certificate.setDocumentTypeContrator("NI");
        certificate.setNitContrator("900000001");
        certificate.setCompany("Empresa Grande");
        certificate.setEndContractDate(null);
        certificate.setMembershipDate(LocalDate.of(2023, 4, 1));
        certificate.setInitContractDate(LocalDate.of(2023, 4, 2));
        certificate.setCoverageDate(LocalDate.of(2023, 4, 3));
        certificate.setStatus("Activo");
        certificate.setCity("Manizales");
        certificate.setExpeditionDate("2023-04-03");
        certificate.setValidatorCode("VAL111");
        certificate.setNameSignatureARL("Firma ARL Empleador");
        certificate.setNameARL("ARL Allianz");
        certificate.setFiledNumber("999");
        certificate.setNit("800000000");

        EconomicActivity economicActivity = new EconomicActivity();

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToBasicAffiliationCertificateEmployerAndDomesticService(certificate, economicActivity);

        assertEquals("certificado basico afiliacion empleador y servicio domestico", reportRequestDTO.getReportName());
        assertEquals("23", reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("1234", parameters.get(Constant.ECONOMY_ACTIVITY));
        assertEquals("Actividad Economica Principal", parameters.get(Constant.NAME_ACTIVITY));
        assertEquals("Riesgo V", parameters.get(Constant.RISK));
        assertEquals("NI. 900000001", parameters.get(Constant.DOCUMENT_IDENTIFIER));
        assertEquals("Empresa Grande", parameters.get(Constant.NAME));
        assertEquals("No registra", parameters.get(Constant.INACTIVATION_DATE));
        assertEquals("01/04/2023", parameters.get(Constant.MEMBERSHIP_DATE));
        assertEquals("999", parameters.get(Constant.CONSECUTIVE_DOCUMENT));
        assertEquals("800000000", parameters.get(Constant.NIT));
        assertEquals(certificate.getVinculationType(), parameters.get(Constant.VINCULATION_TYPE));
        assertEquals("03/04/2023", parameters.get(Constant.COVERAGE_DATE));
        assertEquals("Activo", parameters.get(Constant.STATUS_FIELD));
        assertEquals("Manizales", parameters.get(Constant.CITY));
        assertEquals("2023-04-03", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("VAL111", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("Firma ARL Empleador", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("ARL Allianz", parameters.get(Constant.NAME_ARL_LABEL));
    }

    @Test
    void transformToJudicialProcessesCertificate() {
        Certificate certificate = new Certificate();
        certificate.setFiledNumber("111");
        certificate.setNit("222");
        certificate.setName("Judicial Name");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("333");
        certificate.setInitContractDate(LocalDate.of(2024, 1, 1));
        certificate.setEndContractDate("2024-02-01");
        certificate.setCity("Judicial City");
        certificate.setExpeditionDate("Judicial Expedition");
        certificate.setValidatorCode("Judicial Validator");
        certificate.setNameSignatureARL("Judicial Signature");
        certificate.setNameARL("Judicial ARL");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToJudicialProcessesCertificate(certificate);

        assertEquals(Constant.TYPE_AFFILLATE_JUDICIAL_PROCESSES, reportRequestDTO.getReportName());
        assertNull(reportRequestDTO.getIdReport());

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("111", parameters.get(Constant.CONSECUTIVE_DOCUMENT));
        assertEquals("222", parameters.get(Constant.NIT));
        assertEquals("Judicial Name", parameters.get(Constant.NAME));
        assertEquals("CC", parameters.get(Constant.IDENTIFICATION_TYPE_NAME));
        assertEquals("333", parameters.get(Constant.IDENTIFICATION));
        assertEquals("2024/01/01", parameters.get(Constant.MEMBERSHIP_DATE));
        assertEquals("2024/02/01", parameters.get(Constant.INACTIVATION_DATE));
        assertEquals("Judicial City", parameters.get(Constant.CITY));
        assertEquals("Judicial Expedition", parameters.get(Constant.EXPEDITION_DATE));
        assertEquals("Judicial Validator", parameters.get(Constant.VALIDATOR_CODE));
        assertEquals("Judicial Signature", parameters.get(Constant.NAME_SIGNATURE_ARL));
        assertEquals("Judicial ARL", parameters.get(Constant.NAME_ARL_LABEL));
    }

    @Test
    void transformToJudicialProcessesCertificate_nullEndContractDate() {
        Certificate certificate = new Certificate();
        certificate.setFiledNumber("111");
        certificate.setNit("222");
        certificate.setName("Judicial Name");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("333");
        certificate.setInitContractDate(LocalDate.of(2024, 1, 1));
        certificate.setEndContractDate(null);
        certificate.setCity("Judicial City");
        certificate.setExpeditionDate("Judicial Expedition");
        certificate.setValidatorCode("Judicial Validator");
        certificate.setNameSignatureARL("Judicial Signature");
        certificate.setNameARL("Judicial ARL");

        CertificateReportRequestDTO reportRequestDTO = certificateServiceHelper.transformToJudicialProcessesCertificate(certificate);

        Map<String, Object> parameters = reportRequestDTO.getParameters();
        assertEquals("No registra", parameters.get(Constant.INACTIVATION_DATE));
    }

    @Test
    void stringToDateFormatter() {
        String originalDate = "2023-01-15";
        java.lang.reflect.Method method = null;
            try {
                method = CertificateServiceHelper.class.getDeclaredMethod("stringToDateFormatter", String.class);
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        String formattedDate = null;
            try {
                formattedDate = (String) method.invoke(certificateServiceHelper, originalDate);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        assertEquals("15/01/2023", formattedDate);
    }

    @Test
    void stringToDateFormatter_SinRetiro() {
        String originalDate = "Sin retiro";
        java.lang.reflect.Method method = null;
        try {
            method = CertificateServiceHelper.class.getDeclaredMethod("stringToDateFormatter", String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String formattedDate = null;
        try {
            formattedDate = (String) method.invoke(certificateServiceHelper, originalDate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals("Sin retiro", formattedDate);
    }

    @Test
    void localDateToDateFormatter() {
        LocalDate originalDate = LocalDate.of(2023, 5, 20);
        java.lang.reflect.Method method = null;
        try {
            method = CertificateServiceHelper.class.getDeclaredMethod("localDateToDateFormatter", LocalDate.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String formattedDate = null;
        try {
            formattedDate = (String) method.invoke(certificateServiceHelper, originalDate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals("20/05/2023", formattedDate);
    }

    @Test
    void localDateToDateFormatter_nullDate() {
        java.lang.reflect.Method method = null;
        try {
            method = CertificateServiceHelper.class.getDeclaredMethod("localDateToDateFormatter", LocalDate.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        String formattedDate = null;
        try {
            formattedDate = (String) method.invoke(certificateServiceHelper, (LocalDate) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals("", formattedDate);
    }

    @Test
    void trimText_underLimit() throws Exception {
        String text = "Short text";
        Method method = CertificateServiceHelper.class.getDeclaredMethod("trimText", String.class);
        method.setAccessible(true);
        String trimmedText = (String) method.invoke(certificateServiceHelper, text);
        assertEquals("Short text", trimmedText);
    }

    @Test
    void trimText_overLimit() throws Exception {
        String text = "This is a very long text that exceeds the limit of 135 characters. It should be trimmed to fit within the allowed length, with an ellip...";
        Method method = CertificateServiceHelper.class.getDeclaredMethod("trimText", String.class);
        method.setAccessible(true);
        String trimmedText = (String) method.invoke(certificateServiceHelper, text);
        assertEquals("This is a very long text that exceeds the limit of 135 characters. It should be trimmed to fit within the allowed length, with an ellip...", trimmedText);
    }

    @Test
    void trimText_nullText() throws Exception {
        Method method = CertificateServiceHelper.class.getDeclaredMethod("trimText", String.class);
        method.setAccessible(true);
        String trimmedText = (String) method.invoke(certificateServiceHelper, (String) null);
        assertNull(trimmedText);
    }
}