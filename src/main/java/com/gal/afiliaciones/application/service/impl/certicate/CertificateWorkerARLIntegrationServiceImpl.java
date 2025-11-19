package com.gal.afiliaciones.application.service.impl.certicate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.application.service.CertificateWorkerARLIntegrationService;
import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateWorkerByEmployerResponse;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateWorkerDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class CertificateWorkerARLIntegrationServiceImpl implements CertificateWorkerARLIntegrationService {

    // Constantes para literales duplicados
    private static final String RETIREMENT_DATE_KEY = "retirementDate";
    private static final String ECONOMY_ACTIVITY_KEY = "economyActivity";
    private static final String DECENTRALIZED_CONSECUTIVE_KEY = "decentralizedConsecutive";

    private final ConsultAffiliateCompanyClient consultAffiliateCompanyClient;
    private final GenericWebClient genericWebClient;
    private final CodeValidCertificationService codeValidCertificateService;
    private final FiledService filedService;
    private final CertificateRepository certificateRepository;

    private static final DateTimeFormatter expeditionDateFormatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT_CERTIFICATE_EXPEDITION, 
                                                                                                 Locale.forLanguageTag("es-ES"));

    @Override
    @Transactional
    public CertificateWorkerByEmployerResponse getCertificatesWorkerArlIntegration(String documentType, String documentNumber) {
        CertificateWorkerByEmployerResponse response = new CertificateWorkerByEmployerResponse();
        List<CertificateWorkerDTO> certificateWorkerList = new ArrayList<>();

        Mono<List<AffiliateCompanyResponse>> workersMono = consultAffiliateCompanyClient.consultAffiliate(documentType, documentNumber);

        if(workersMono != null) {
            List<AffiliateCompanyResponse> workerList = workersMono.block();

            if(workerList!=null && !workerList.isEmpty()) {
                List<AffiliateCompanyResponse> activeWorkerList = workerList.stream()
                        .sorted(Comparator.comparing(
                                (AffiliateCompanyResponse w) -> !"Activo".equalsIgnoreCase(String.valueOf(w.getEstadoRl()))
                        ))
                        .toList();

                if(!activeWorkerList.isEmpty()) {
                    activeWorkerList.forEach(activeWorker -> {
                        CertificateWorkerDTO certificateWorker = new CertificateWorkerDTO();
                        certificateWorker.setTipoDoc(activeWorker.getTipoDoc());
                        certificateWorker.setIdPersona(activeWorker.getIdPersona());
                        certificateWorker.setTipoVinculacion(activeWorker.getNomVinLaboral());
                        certificateWorker.setNombreEmpresa(activeWorker.getRazonSocial());
                        CertificateReportRequestDTO reportRequest = determineReportRequest(activeWorker);
                        String base64Pdf = genericWebClient.getTransversalCertificateWorkerArlIntegration(reportRequest);
                        certificateWorker.setCertificado(base64Pdf);
                        
                        // Persistir el Certificate entity utilizando los parámetros del reporte
                        Certificate certificate = buildCertificateFromJasperParameters(reportRequest.getParameters(), activeWorker);
                        certificateRepository.save(certificate);
                        
                        certificateWorkerList.add(certificateWorker);
                    });

                    response.setTpDocEmpresa(documentType);
                    response.setIdEmpresa(documentNumber);
                    response.setCertificados(certificateWorkerList);
                    return response;
                }
            }
        }

        throw new AffiliateNotFoundException(Constant.AFFILIATE_NOT_FOUND);
    }

    /**
     * Determina el ReportRequestDTO con el nombre de plantilla correspondiente
     * basado en el tipo de vinculación del trabajador
     */
    private CertificateReportRequestDTO determineReportRequest(AffiliateCompanyResponse worker) {
        String vinculationType = worker.getNomVinLaboral();
        String cargo = worker.getCargo();
        String razonSocial = worker.getRazonSocial();

        String reportName;

        if ("Dependiente".equalsIgnoreCase(vinculationType)) {
            reportName = Constant.CERTIFICATE_TEMPLATE_DEPENDIENTE;
        } else if ("Independiente".equalsIgnoreCase(vinculationType)) {
            if ("AFILIACION VOLUNTARIA".equalsIgnoreCase(cargo) ||
                (razonSocial != null && razonSocial.toUpperCase().contains("VOLUNTARIO"))) {
                reportName = Constant.CERTIFICATE_TEMPLATE_INDEPENDIENTE_VOLUNTARIO;
            } else {
                reportName = Constant.CERTIFICATE_TEMPLATE_INDEPENDIENTE_NO_VOLUNTARIO;
            }
        } else {
            reportName = Constant.CERTIFICATE_TEMPLATE_INDEPENDIENTE_NO_VOLUNTARIO;
        }

        Map<String, Object> jasperParameters = buildJasperParameters(worker, reportName);
        
        return CertificateReportRequestDTO.builder()
                .reportName(reportName)
                .parameters(jasperParameters)
                .build();
    }

    private Map<String, Object> buildJasperParameters(AffiliateCompanyResponse worker, String reportName) {
        if (Constant.CERTIFICATE_TEMPLATE_DEPENDIENTE.equals(reportName)) {
            return buildJasperParametersForDependiente(worker);
        } else if (Constant.CERTIFICATE_TEMPLATE_INDEPENDIENTE_VOLUNTARIO.equals(reportName)) {
            return buildJasperParametersForIndependienteVoluntario(worker);
        } else {
            return buildJasperParametersForIndependienteNoVoluntario(worker);
        }
    }

    /**
     * Construye los parámetros Jasper comunes a todos los tipos de certificado
     */
    private Map<String, Object> buildCommonJasperParameters(AffiliateCompanyResponse worker) {
        Map<String, Object> parameters = new HashMap<>();
        
        parameters.put("validatorCode",
            codeValidCertificateService.consultCodeWorkerArlIntegration(worker.getTipoDoc(), worker.getIdPersona(),
                    worker.getNombre1(), worker.getApellido1()));
        parameters.put("identification", worker.getTipoDoc() + ". " + worker.getIdPersona());
        parameters.put("expeditionDate", expeditionDateFormatter.format(LocalDate.now()));
        parameters.put("nameARL", worker.getNombreArl());
        parameters.put("coverageDate", formatDateToDDMMYYYY(worker.getFechaInicioVinculacion()));
        parameters.put("risk", extractRiskCode(worker.getIdActEconomica()));
        parameters.put("name", concatCompleName(worker));
        parameters.put("status", worker.getEstadoRl());
        parameters.put("vinculationType", worker.getNomVinLaboral());
        parameters.put("consecutivoDoc", filedService.getNextFiledNumberCertificate());
        
        return parameters;
    }

    private Map<String, Object> buildJasperParametersForDependiente(AffiliateCompanyResponse worker) {
        Map<String, Object> parameters = buildCommonJasperParameters(worker);
        
        parameters.put("companyName", worker.getRazonSocial());
        parameters.put("nitCompany", worker.getTpDocEmpresa());
        parameters.put("numberCompany", worker.getIdEmpresa());
        parameters.put(RETIREMENT_DATE_KEY, formatRetirementDate(worker.getFechaFinVinculacion()));
        parameters.put(ECONOMY_ACTIVITY_KEY, Objects.nonNull(worker.getIdActEconomica()) ? String.valueOf(worker.getIdActEconomica()) : null);
        
        return parameters;
    }

    private Map<String, Object> buildJasperParametersForIndependienteNoVoluntario(AffiliateCompanyResponse worker) {
        Map<String, Object> parameters = buildCommonJasperParameters(worker);
        
        parameters.put("nameContractor", worker.getRazonSocial());
        parameters.put("typeDocumentContract", worker.getTpDocEmpresa());
        parameters.put("numberDocumentContract", worker.getIdEmpresa());
        parameters.put("initContractDate", formatDateToDDMMYYYY(worker.getFechaInicioContrato()));
        parameters.put("endContractDate", formatDateToDDMMYYYY(worker.getFechaFinContrato()));
        parameters.put(RETIREMENT_DATE_KEY, formatDateToDDMMYYYY(worker.getFechaFinVinculacion()));
        parameters.put(ECONOMY_ACTIVITY_KEY, Objects.nonNull(worker.getIdActEconomica()) ? String.valueOf(worker.getIdActEconomica()) : null);
        parameters.put(DECENTRALIZED_CONSECUTIVE_KEY, worker.getCodigoSubempresa());
        parameters.put("nameActivity", worker.getNomActEco());
        parameters.put("contractStatus", toCamelCase(getStatusMaped(toUpperCase(worker.getEstadoContrato()))));
        
        return parameters;
    }

    private Map<String, Object> buildJasperParametersForIndependienteVoluntario(AffiliateCompanyResponse worker) {
        Map<String, Object> parameters = buildCommonJasperParameters(worker);
        
        parameters.put("position", Objects.nonNull(worker.getOcupacionVoluntario()) ? worker.getOcupacionVoluntario() : worker.getOcupacion());
        parameters.put("occupationCode", Objects.nonNull(worker.getCodigoOcupacion()) ? worker.getCodigoOcupacion() : worker.getIdOcupacion());
        
        return parameters;
    }

    private String formatDateToDDMMYYYY(String date) {
        if (date == null) return "";
        try {
            LocalDate localDate = LocalDate.parse(date.substring(0, 10));
            return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return date.substring(0, 10); // Retorna YYYY-MM-DD como fallback
        }
    }

    private String formatRetirementDate(String fechaFinVinculacion) {
        return fechaFinVinculacion == null ? "No registra" : formatDateToDDMMYYYY(fechaFinVinculacion);
    }

    private String extractRiskCode(Long idActEconomica) {
        return idActEconomica != null ? String.valueOf(idActEconomica).substring(0, 1) : "";
    }

    private String concatCompleName(AffiliateCompanyResponse worker){
        String completeName = worker.getNombre1();
    
        if(worker.getNombre2()!=null && !worker.getNombre2().isBlank())
            completeName = completeName + " " + worker.getNombre2();
    
        completeName = completeName + " " + worker.getApellido1();
    
        if(worker.getApellido2()!=null && !worker.getApellido2().isBlank())
            completeName = completeName + " " + worker.getApellido2();
    
        return completeName;
    }
    
    private String formatDate(String date) {
        if (date == null) return "";
        return date.length() >= 10 ? date.substring(0, 10) : date;
    }

    private String toCamelCase(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        text = text.trim();
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private String toUpperCase(String text){
        return Objects.nonNull(text) ? text.toUpperCase() : text;
    }

    private String getStatusMaped(String originalStatus) {
        Map<String, String> statusMappings = Map.of(
            "FINALIZADO", "RETIRADO"
        );
        
        return statusMappings.getOrDefault(originalStatus, originalStatus);
    }

    /**
     * Construye un Certificate entity a partir de los parámetros de Jasper y datos del trabajador
     */
    private Certificate buildCertificateFromJasperParameters(Map<String, Object> jasperParameters, AffiliateCompanyResponse worker) {
        Certificate certificate = new Certificate();
        
        // Mapear los campos principales desde los parámetros de Jasper
        certificate.setValidatorCode((String) jasperParameters.get("validatorCode"));
        certificate.setName(concatCompleName(worker));
        certificate.setTypeDocument(worker.getTipoDoc());
        certificate.setNumberDocument(worker.getIdPersona());
        certificate.setExpeditionDate((String) jasperParameters.get("expeditionDate"));
        certificate.setNameARL((String) jasperParameters.get("nameARL"));
        certificate.setRisk((String) jasperParameters.get("risk"));
        certificate.setStatus((String) jasperParameters.get("status"));
        certificate.setVinculationType((String) jasperParameters.get("vinculationType"));
        certificate.setFiledNumber((String) jasperParameters.get("consecutivoDoc"));
        
        // Mapear fechas de cobertura si están disponibles
        String coverageDate = (String) jasperParameters.get("coverageDate");
        if (coverageDate != null && !coverageDate.isEmpty()) {
            try {
                certificate.setCoverageDate(LocalDate.parse(formatDate(worker.getFechaInicioVinculacion())));
            } catch (Exception e) {
                // Si hay error en parsing, mantener null
                log.warn("Error parsing coverage date: {}", coverageDate, e);
            }
        }
        
        // Mapear campos específicos dependiendo del tipo de vinculación
        String vinculationType = worker.getNomVinLaboral();
        
        if ("Dependiente".equalsIgnoreCase(vinculationType)) {
            certificate.setCompany((String) jasperParameters.get("companyName"));
            certificate.setDocumentTypeContrator((String) jasperParameters.get("nitCompany"));
            certificate.setNitContrator((String) jasperParameters.get("numberCompany"));
            certificate.setRetirementDate((String) jasperParameters.get(RETIREMENT_DATE_KEY));
            certificate.setCodeActivityEconomicPrimary((String) jasperParameters.get(ECONOMY_ACTIVITY_KEY));
        } else if ("Independiente".equalsIgnoreCase(vinculationType)) {
            // Para independientes
            certificate.setCompany((String) jasperParameters.get("nameContractor"));
            certificate.setDocumentTypeContrator((String) jasperParameters.get("typeDocumentContract"));
            certificate.setNitContrator((String) jasperParameters.get("numberDocumentContract"));
            certificate.setDecentralizedConsecutive(Objects.nonNull(jasperParameters.get(DECENTRALIZED_CONSECUTIVE_KEY)) ?
                                                    Integer.valueOf(jasperParameters.get(DECENTRALIZED_CONSECUTIVE_KEY).toString()) : null);
            certificate.setNameActivityEconomic((String) jasperParameters.get("nameActivity"));
            certificate.setContractStatus((String) jasperParameters.get("contractStatus"));
            certificate.setPosition((String) jasperParameters.get("position"));
            certificate.setOccupationCode((String) jasperParameters.get("occupationCode"));
            
            // Fechas de contrato para independientes no voluntarios
            String initContractDate = (String) jasperParameters.get("initContractDate");
            if (initContractDate != null && !initContractDate.isEmpty()) {
                try {
                    certificate.setInitContractDate(LocalDate.parse(formatDate(worker.getFechaInicioContrato())));
                } catch (Exception e) {
                    log.warn("Error parsing init contract date: {}", initContractDate, e);
                }
            }
            
            certificate.setEndContractDate((String) jasperParameters.get("endContractDate"));
            certificate.setRetirementDate((String) jasperParameters.get(RETIREMENT_DATE_KEY));
            certificate.setCodeActivityEconomicPrimary((String) jasperParameters.get(ECONOMY_ACTIVITY_KEY));
        }
        
        return certificate;
    }
}
