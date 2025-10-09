package com.gal.afiliaciones.application.service.helper;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.dto.affiliate.SingleMembershipCertificateEmployerView;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CertificateServiceHelper {

    private static final String FORMAT_DATE_DDMMYYYY = "dd/MM/yyyy";

    private static final String CERTIFICATEOPSAND723 = "24";
    private static final String DEPENDENTWORKER = "25";
    private static final String EMPLOYERANDDOMESTICSERVICE = "23";

    private static final DateTimeFormatter expeditionDateFormatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_CERTIFICATE_EXPEDITION, 
                                                                                                 Locale.forLanguageTag("es-ES"));

    public CertificateReportRequestDTO transformToNoAffiliateCertificate(Certificate certificate) {
        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName(Constant.TYPE_NOT_AFFILLATE);
        reportRequestDTO.setIdReport(null);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constant.NIT, certificate.getNit());
        parameters.put(Constant.VALIDATOR_CODE, certificate.getValidatorCode());
        parameters.put(Constant.IDENTIFICATION, certificate.getNumberDocument());
        parameters.put(Constant.CITY, certificate.getCity());
        parameters.put(Constant.EXPEDITION_DATE, String.valueOf(certificate.getExpeditionDate()));
        parameters.put(Constant.NAME_SIGNATURE_ARL, certificate.getNameSignatureARL());
        parameters.put(Constant.NAME_ARL_LABEL, certificate.getNameARL());

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    /*
     * Certificado básico de afiliación trabajador independiente voluntario
     * Certificado básico de afiliación trabajador independiente ops
     * Certificado básico de afiliación trabajador independiente 723
     **/
    public CertificateReportRequestDTO transformIndependentWorkerAffiliationCertificateOpsAnd723(
            Certificate certificate) {
        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        if(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER.equals(certificate.getVinculationSubType())) {
            reportRequestDTO.setReportName(Constant.TYPE_CERTIFICATE_INDEPENDENT_VOLUNTEER);
        } else {
            reportRequestDTO.setReportName("certificado basico afiliacion trabajador independiente voluntario, ops y 723");
            reportRequestDTO.setIdReport(CERTIFICATEOPSAND723);
        }
        Map<String, Object> parameters = new HashMap<>();

        if(Constant.TYPE_AFFILLATE_INDEPENDENT.equals(certificate.getVinculationType()) ||
                Constant.BONDING_TYPE_INDEPENDENT.equals(certificate.getVinculationType())) {
            parameters.put(Constant.CONTRACTOR_DOCUMENT_TYPE, Constant.NIT.toUpperCase());
            parameters.put(Constant.CONTRACTOR_DOCUMENT_NUMBER, certificate.getNitContrator());
        }

        if (Constant.TYPE_AFFILIATE_EMPLOYER_OPS.equals(certificate.getVinculationType())) {
            parameters.put(Constant.CONTRACTOR_DOCUMENT_TYPE, certificate.getTypeDocument());
            parameters.put(Constant.CONTRACTOR_DOCUMENT_NUMBER, certificate.getNumberDocument());
        }

        parameters.put(Constant.NIT, certificate.getNit());
        parameters.put(Constant.VINCULATION_TYPE, certificate.getVinculationType());
        parameters.put(Constant.COVERAGE_DATE, localDateToDateFormatter(certificate.getCoverageDate()));
        parameters.put(Constant.STATUS_FIELD, certificate.getStatus());
        parameters.put(Constant.CITY, certificate.getCity());
        parameters.put(Constant.EXPEDITION_DATE, certificate.getExpeditionDate());
        parameters.put(Constant.VALIDATOR_CODE, certificate.getValidatorCode());
        parameters.put(Constant.NAME_SIGNATURE_ARL, certificate.getNameSignatureARL());
        parameters.put(Constant.NAME_ARL_LABEL, certificate.getNameARL());
        parameters.put(Constant.RISK, certificate.getRisk());
        parameters.put(Constant.INIT_CONTRACT_DATE, localDateToDateFormatter(certificate.getInitContractDate()));
        parameters.put(Constant.NAME_CONTRACTOR, certificate.getCompany());
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, certificate.getFiledNumber());
        parameters.put(Constant.ECONOMY_ACTIVITY, String.valueOf(certificate.getCodeActivityEconomicPrimary()));

        if(certificate.getEndContractDate() == null)
            parameters.put(Constant.END_CONTRACT_DATE, Constant.NO_RECORD_LABEL);
        else
            parameters.put(Constant.END_CONTRACT_DATE, stringToDateFormatter(certificate.getEndContractDate()));

        parameters.put(Constant.RETIREMENT_DATE, stringToDateFormatter(certificate.getRetirementDate()));
        parameters.put(Constant.IDENTIFICATION, certificate.getTypeDocument() + ". " + certificate.getNumberDocument());
        parameters.put(Constant.NAME, certificate.getName());
        parameters.put(Constant.ADDRESSED_TO_LABEL, certificate.getAddressedTo());

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    /*
     * Certificado básico de afiliación trabajador dependiente
     * Certificado básico de afiliación estudiante enpráctica
     * Certificado básico de afiliación aprendiz sena
     **/
    public CertificateReportRequestDTO transformToDependentWorkerCertificate(Certificate certificate) {
        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName("certificado basico afiliacion trabajador dependiente, estudiante enpráctica y aprendiz sena");
        reportRequestDTO.setIdReport(DEPENDENTWORKER);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constant.NIT, certificate.getNit());
        parameters.put(Constant.COMPANY_NAME, certificate.getCompany());
        parameters.put(Constant.COMPANY_NIT, certificate.getDocumentTypeContrator());
        parameters.put(Constant.COMPANY_NUMBER, certificate.getNitContrator());
        parameters.put(Constant.IDENTIFICATION,  certificate.getTypeDocument() + ". " + certificate.getNumberDocument());
        parameters.put(Constant.MEMBERSHIP_DATE, localDateToDateFormatter(certificate.getMembershipDate()));
        parameters.put(Constant.COVERAGE_DATE, localDateToDateFormatter(certificate.getCoverageDate()));
        parameters.put(Constant.RISK, certificate.getRisk());
        parameters.put(Constant.NAME, certificate.getName());
        parameters.put(Constant.STATUS_FIELD, certificate.getStatus());
        parameters.put(Constant.VINCULATION_TYPE, certificate.getVinculationType());
        parameters.put(Constant.RETIREMENT_DATE, stringToDateFormatter(certificate.getRetirementDate()));
        parameters.put(Constant.CITY, certificate.getCity());
        parameters.put(Constant.EXPEDITION_DATE, String.valueOf(certificate.getExpeditionDate()));
        parameters.put(Constant.VALIDATOR_CODE, certificate.getValidatorCode());
        parameters.put(Constant.NAME_SIGNATURE_ARL, certificate.getNameSignatureARL());
        parameters.put(Constant.NAME_ARL_LABEL, certificate.getNameARL());
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, certificate.getFiledNumber());
        parameters.put(Constant.ADDRESSED_TO_LABEL, certificate.getAddressedTo());
        parameters.put(Constant.ECONOMY_ACTIVITY, String.valueOf(certificate.getCodeActivityEconomicPrimary()));

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    /*
     * Certificado básicos de afiliaciones empleador
     * Certificado básicos de afiliaciones empleador servicio doméstico
     **/

    public CertificateReportRequestDTO transformToBasicAffiliationCertificateEmployerAndDomesticService(
            Certificate certificate,
            EconomicActivity economicActivity) {

        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName("certificado basico afiliacion empleador y servicio domestico");
        reportRequestDTO.setIdReport(EMPLOYERANDDOMESTICSERVICE);

        Map<String, Object> parameters = new HashMap<>();

        if (Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC.equals(certificate.getVinculationType())) {
            parameters.put(Constant.ECONOMY_ACTIVITY, Constant.CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC);
            parameters.put(Constant.NAME_ACTIVITY, trimText(economicActivity.getDescription()));

            parameters.put(Constant.RISK, economicActivity.getClassRisk());
            parameters.put(Constant.DOCUMENT_IDENTIFIER, certificate.getTypeDocument() + ". " + certificate.getNumberDocument());
            parameters.put(Constant.NAME, certificate.getName());
            parameters.put(Constant.CERT_PARAM_DEPENDENT_WORKERS_NUMBER, String.valueOf(certificate.getDependentWorkersNumber()));
            parameters.put(Constant.CERT_PARAM_INDEPENDENT_WORKERS_NUMBER, String.valueOf(certificate.getIndependentWorkersNumber()));
        }

        if(Constant.TYPE_AFFILIATE_EMPLOYER.equals(certificate.getVinculationType()) ||
                Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER.equals(certificate.getVinculationType())) {
            parameters.put(Constant.ECONOMY_ACTIVITY, String.valueOf(certificate.getCodeActivityEconomicPrimary()));
            parameters.put(Constant.NAME_ACTIVITY, trimText(certificate.getNameActivityEconomic()));
            parameters.put(Constant.RISK, certificate.getRisk());
            String documentTypeEmployer = certificate.getDocumentTypeContrator()==null ? Constant.NI : certificate.getDocumentTypeContrator();
            parameters.put(Constant.DOCUMENT_IDENTIFIER, documentTypeEmployer + ". " + certificate.getNitContrator());
            parameters.put(Constant.NAME, certificate.getCompany());
            parameters.put(Constant.CERT_PARAM_DEPENDENT_WORKERS_NUMBER, String.valueOf(certificate.getDependentWorkersNumber()));
            parameters.put(Constant.CERT_PARAM_INDEPENDENT_WORKERS_NUMBER, String.valueOf(certificate.getIndependentWorkersNumber()));
        }

        if (certificate.getEndContractDate() == null)
            parameters.put(Constant.INACTIVATION_DATE, Constant.NO_RECORD_LABEL);
        else
            parameters.put(Constant.INACTIVATION_DATE, stringToDateFormatter(certificate.getEndContractDate()));

        parameters.put(Constant.MEMBERSHIP_DATE, localDateToDateFormatter(certificate.getMembershipDate()));
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, certificate.getFiledNumber());
        parameters.put(Constant.NIT, certificate.getNit());
        parameters.put(Constant.VINCULATION_TYPE, certificate.getVinculationType());
        parameters.put(Constant.COVERAGE_DATE, localDateToDateFormatter(certificate.getCoverageDate()));
        parameters.put(Constant.STATUS_FIELD, certificate.getStatus());
        parameters.put(Constant.CITY, certificate.getCity());
        parameters.put(Constant.EXPEDITION_DATE, certificate.getExpeditionDate());
        parameters.put(Constant.VALIDATOR_CODE, certificate.getValidatorCode());
        parameters.put(Constant.NAME_SIGNATURE_ARL, certificate.getNameSignatureARL());
        parameters.put(Constant.NAME_ARL_LABEL, certificate.getNameARL());
        parameters.put(Constant.ADDRESSED_TO_LABEL, certificate.getAddressedTo());

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    // Nueva función para "certificado afiliaciones procesos judiciales"
    public CertificateReportRequestDTO transformToJudicialProcessesCertificate(Certificate certificate) {
        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName(Constant.TYPE_AFFILLATE_JUDICIAL_PROCESSES);
        reportRequestDTO.setIdReport(null);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, certificate.getFiledNumber());
        parameters.put(Constant.NIT, certificate.getNit());
        parameters.put(Constant.NAME, certificate.getName());
        parameters.put(Constant.IDENTIFICATION_TYPE_NAME,  certificate.getTypeDocument());
        parameters.put(Constant.IDENTIFICATION,  certificate.getNumberDocument());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        parameters.put(Constant.MEMBERSHIP_DATE, certificate.getInitContractDate()!=null ?
                certificate.getInitContractDate().format(formatter) : "");
        if(certificate.getEndContractDate()!=null) {
            LocalDate endContractDate = LocalDate.parse(certificate.getEndContractDate());
            parameters.put(Constant.INACTIVATION_DATE, endContractDate.format(formatter));
        }else{
            parameters.put(Constant.INACTIVATION_DATE, Constant.NO_RECORD_LABEL);
        }
        parameters.put(Constant.CITY, certificate.getCity());
        parameters.put(Constant.EXPEDITION_DATE, certificate.getExpeditionDate());
        parameters.put(Constant.VALIDATOR_CODE, certificate.getValidatorCode());
        parameters.put(Constant.NAME_SIGNATURE_ARL, certificate.getNameSignatureARL());
        parameters.put(Constant.NAME_ARL_LABEL, certificate.getNameARL());

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    public CertificateReportRequestDTO transformToSingleMembershipCertificate(SingleMembershipCertificateEmployerView employer,
                                                                              List<Map<String, Object>> employees,
                                                                              ArlInformation arlInformation,
                                                                              String consecutiveDocument,
                                                                              String validationCode) {
        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName(Constant.TYPE_CERTIFICATE_SINGLE_MEMBERSHIP);

        Map<String, Object> parameters = new HashMap<>();

        // ARL
        parameters.put("ARL_RAZON_SOCIAL", arlInformation.getName());
        parameters.put("ARL_NIT", arlInformation.getNit());

        // Parámetros del empleador
        parameters.put("filedNumber", consecutiveDocument);
        parameters.put("EMPLEADOR_TIPO_DOC", employer.getDocumentType());
        parameters.put("EMPLEADOR_NUM_DOC", employer.getNitCompany());
        parameters.put("EMPLEADOR_NOMBRE", employer.getCompany());
        parameters.put("EMPLEADOR_DIRECCION", employer.getAddress());
        parameters.put("EMPLEADOR_DEPARTAMENTO", employer.getDepartmentName());
        parameters.put("EMPLEADOR_CIUDAD", employer.getCityName());
        parameters.put("EMPLEADOR_CORREO", employer.getEmailContactCompany());
        parameters.put("EMPLEADOR_TELEFONO", employer.getPhone());
        parameters.put("EMPLEADOR_ACTIVIDAD", MessageFormat.format("{0} {1}", 
                                                    employer.getEconomicActivityCode(), 
                                                    employer.getEconomicActivityDescription()));
        parameters.put("EMPLEADOR_TARIFA", Objects.nonNull(employer.getRiskRate()) ? 
                                               employer.getRiskRate().toString() : "");

        //Afiliados
        parameters.put("AFILIADOS_DATA", employees);

        // Certificado
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, consecutiveDocument);
        parameters.put(Constant.EXPEDITION_DATE, expeditionDateFormatter.format(LocalDate.now()));
        parameters.put(Constant.VALIDATOR_CODE, validationCode);

        reportRequestDTO.setParameters(parameters);
        return reportRequestDTO;
    }

    private String stringToDateFormatter(String originalDate) {
        if(originalDate.equals(Constant.NO_RECORD_LABEL))
            return originalDate;
        
        LocalDate localDate = LocalDate.parse(originalDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY);

        return localDate.format(formatter);
    }

    private String localDateToDateFormatter(LocalDate originalDate) {
        if(originalDate == null)  return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_DDMMYYYY);

        return originalDate.format(formatter);
    }

    //Se limita el nombre de la actividad economica si supera los 135 caracteres
    private static String trimText(String text) {
        if (text == null || text.length() <= 135) {
            return text;
        }

        return text.substring(0, 135) + "...";
    }
}