package com.gal.afiliaciones.application.service.individualindependentaffiliation.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.individualindependentaffiliation.IndividualIndependentAffiliationService;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation.IndividualIndependentAffiliationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndividualIndependentAffiliationServiceImpl implements IndividualIndependentAffiliationService {

    private final GenericWebClient genericWebClient;

    @Override
    public String generatePdfReport(IndividualIndependentAffiliationDTO dto) throws JRException {
        // Parámetros para el reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("codeRadicated", dto.getFiledNumber());

        //Encabezados de trámite
        headersData(parameters, dto);

        //Datos generales del trabajador independiente
        generalInformationData(parameters, dto);

        //Información de la afiliación o del contrato
        affiliateContractInformation(parameters, dto);

        /*Información del contratante, empresa transportadora
        (habilitada por el ministerio de transporte para taxistas)
        o contratante para independiente voluntario.*/
        informationContractSignerData(parameters, dto);

        //ARL
        arl(parameters, dto);

        //Dangers identification format
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> dangersMap = objectMapper.convertValue(dto.getAffiliationIndependentVolunteerStep2DTO(), Map.class);
        parameters.put("riesgos", dangersMap);

        CertificateReportRequestDTO certificateReportRequestDTO = new CertificateReportRequestDTO();
        certificateReportRequestDTO.setIdReport("19");
        certificateReportRequestDTO.setReportName("formulario afiliacion individual independientes");
        certificateReportRequestDTO.setParameters(parameters);

        // Exportar el reporte a PDF
        return genericWebClient.generateReportCertificate(certificateReportRequestDTO);
    }

    private void headersData(Map<String, Object> parameters, IndividualIndependentAffiliationDTO dto){
        parameters.put("radicado", defaultIfNullOrEmpty(dto.getFiledNumber()));
        parameters.put("fechaRadicado", defaultIfNullOrEmpty(dateFormatter(dto.getFilingDate())));
        parameters.put("fechaInicioAfiliacion", dateFormatter(dto.getAffiliationStartDate()));
        parameters.put(Constant.CONSECUTIVE_DOCUMENT, dto.getConsecutiveDoc());
    }

    private void generalInformationData(Map<String, Object> parameters, IndividualIndependentAffiliationDTO dto){
        parameters.put("tipoDocumentoIdentificacionGI", defaultIfNullOrEmpty(dto.getIdentificationDocumentTypeGI()));
        parameters.put("numeroDocumentoIdentificacionGI", defaultIfNullOrEmpty(dto.getIdentificationDocumentNumberGI()));
        parameters.put("nombresApellidosRazonSocialGI", defaultIfNullOrEmpty(dto.getFullNameOrBusinessNameGI()));
        parameters.put("fechaNacimientoGI", defaultIfNullOrEmpty(dateFormatter(dto.getDateOfBirthGI())));
        parameters.put("sexoGI", defaultIfNullOrEmpty(dto.getGenderGI()));
        parameters.put("nacionalidadGI", defaultIfNullOrEmpty(dto.getNationalityGI()));
        parameters.put("epsActualGI", defaultIfNullOrEmpty(dto.getCurrentHealthInsuranceGI()));
        parameters.put("afpActualGI", defaultIfNullOrEmpty(dto.getCurrentPensionFundGI()));
        parameters.put("direccionResidenciaGI", defaultIfNullOrEmpty(dto.getAddressGI()));
        parameters.put("departamentoGI", defaultIfNullOrEmpty(dto.getDepartmentGI()));
        parameters.put("municipioDistritoGI", defaultIfNullOrEmpty(dto.getCityOrDistrictGI()));
        parameters.put("celularTelefonoFijoGI", defaultIfNullOrEmpty(dto.getMobileOrLandlineGI()));
        parameters.put("correoElectronicoGI", defaultIfNullOrEmpty(dto.getEmailGI()));
    }

    private void affiliateContractInformation(Map<String, Object> parameters, IndividualIndependentAffiliationDTO dto) {
        parameters.put("tipoContratoACI", dto.getContractTypeACI());
        parameters.put("calidadContratoACI", dto.getContractQualityACI());
        parameters.put("suministroTransporteACI", dto.getTransportSupplyACI());
        parameters.put("fechaInicioContratoACI", defaultIfNullOrEmpty(dateFormatter(dto.getContractStartDateACI())));
        parameters.put("fechaFinContratoACI", !dto.getContractEndDateACI().equals(Constant.DOES_NOT_APPLY) &&
                dto.getContractEndDateACI() != null ? dateFormatter(dto.getContractEndDateACI()) : Constant.DOES_NOT_APPLY);
        parameters.put("numeroMesesACI", defaultIfNullOrEmpty(dto.getNumberOfMonthsACI()));
        parameters.put("jornadaEstablecidaACI", dto.getEstablishedWorkShiftACI());
        parameters.put("valorTotalContratoACI", defaultIfNullOrEmpty(dto.getTotalContractValueACI()));
        parameters.put("valorMensualContratoACI", defaultIfNullOrEmpty(dto.getMonthlyContractValueACI()));
        parameters.put("ingresoBaseCotizacionACI", defaultIfNullOrEmpty(dto.getBaseContributionIncomeACI()));
        parameters.put("actividadEjecutarACI", defaultIfNullOrEmpty(dto.getActivityCarriedACI()));
        parameters.put("codigoActividadEconomicaACI", defaultIfNullOrEmpty(dto.getEconomicActivityCodeACI()));
        parameters.put("cargoOcupacionACI", defaultIfNullOrEmpty(dto.getJobPositionACI()));
        parameters.put("taxistaACI", dto.getTaxiDriverACI());
        parameters.put("direccionACI", defaultIfNullOrEmpty(dto.getAddressACI()));
        parameters.put("departamentoACI", defaultIfNullOrEmpty(dto.getDepartmentACI()));
        parameters.put("municipioDistritoACI", defaultIfNullOrEmpty(dto.getCityOrDistrictACI()));
    }

    private void informationContractSignerData(Map<String, Object> parameters, IndividualIndependentAffiliationDTO dto) {
        parameters.put("nombresApellidosRazonSocialCI", defaultIfNullOrEmpty(dto.getFullNameOrBusinessNameCI()));
        parameters.put("tipoDocumentoIdentificacionCI", defaultIfNullOrEmpty(dto.getIdentificationDocumentTypeCI()));
        parameters.put("numeroDocumentoIdentificacionCI", defaultIfNullOrEmpty(dto.getIdentificationDocumentNumberCI()));
        parameters.put("dvCI", defaultIfNullOrEmpty(dto.getDvCI()));

        parameters.put("codigoActividadEconomicaCI", defaultIfNullOrEmpty(dto.getEconomicActivityCodeCI()));
        parameters.put("direccionCI", defaultIfNullOrEmpty(dto.getAddressCI()));
        parameters.put("departamentoCI", defaultIfNullOrEmpty(dto.getDepartmentCI()));
        parameters.put("municipioDistritoCI", defaultIfNullOrEmpty(dto.getCityOrDistrictCI()));
        parameters.put("celularTelefonoFijoCI", defaultIfNullOrEmpty(dto.getMobileOrLandlineCI()));

        parameters.put("correoElectronicoCI", defaultIfNullOrEmpty(dto.getEmailCI()));

        parameters.put("nombresApellidosRazonSocialICS", defaultIfNullOrEmpty(dto.getFullNameOrBusinessNameICS()));
        parameters.put("tipoDocumentoIdentificacionICS", defaultIfNullOrEmpty(dto.getIdentificationDocumentTypeICS()));
        parameters.put("numeroDocumentoIdentificacionICS", defaultIfNullOrEmpty(dto.getIdentificationDocumentNumberICS()));

        parameters.put("firmaIndependiente", dto.getSignatureIndependent());
    }

    private void arl(Map<String, Object> parameters, IndividualIndependentAffiliationDTO dto){
        parameters.put("codigoActividadEconomicaARL", defaultIfNullOrEmpty(dto.getEconomicActivityCodeARL()));
        parameters.put("claseRiesgo", defaultIfNullOrEmpty(dto.getRiskClassARL()));
        parameters.put("tarifa", defaultIfNullOrEmpty(dto.getFeeARL()));
    }

    private String dateFormatter(String date){
        if(date != null) {
            date = date.substring(0, 10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return String.valueOf(LocalDate.parse(date).format(formatter));
        }
        return "";
    }

    public static String defaultIfNullOrEmpty(String value) {
        return (value == null || value.isEmpty()) ? "N/A" : value;
    }
}