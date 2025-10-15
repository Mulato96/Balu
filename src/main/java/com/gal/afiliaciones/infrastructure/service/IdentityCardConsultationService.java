package com.gal.afiliaciones.infrastructure.service;

import com.gal.afiliaciones.infrastructure.client.registraduria.RegistraduriaClient;
import com.gal.afiliaciones.infrastructure.dto.registraduria.RegistraduriaResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.registraduria.RegistraduriaSoapResponseDTO;
import com.gal.afiliaciones.infrastructure.utils.RegistraduriaXmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityCardConsultationService {

    private final RegistraduriaClient registraduriaClient;

    /**
     * Consult identity card information from Registraduria
     * @param documentNumber Document number to consult
     * @return Clean JSON response with identity card data
     */
    public Mono<RegistraduriaResponseDTO> consultIdentityCard(String documentNumber) {
        log.info("Starting identity card consultation for document: {}", documentNumber);

        return registraduriaClient.consultIdentityCard(documentNumber)
                .map(RegistraduriaSoapResponseDTO::success)
                .onErrorResume(error -> {
                    log.error("Error in registraduria client call for document {}: {}", documentNumber, error.getMessage());
                    return Mono.just(RegistraduriaSoapResponseDTO.error(error.getMessage()));
                })
                .flatMap(this::processSoapResponse)
                .doOnSuccess(response -> log.info("Identity card consultation completed successfully for document: {}", documentNumber))
                .doOnError(error -> log.error("Error in identity card consultation for document {}: {}", documentNumber, error.getMessage()));
    }

    /**
     * Process SOAP response and convert to clean JSON
     */
    private Mono<RegistraduriaResponseDTO> processSoapResponse(RegistraduriaSoapResponseDTO soapResponse) {
        if (!soapResponse.isSuccess()) {
            return Mono.error(new RuntimeException("SOAP service error: " + soapResponse.getErrorMessage()));
        }

        try {
            if (!RegistraduriaXmlParser.isValidRegistraduriaResponse(soapResponse.getRawXmlResponse())) {
                return Mono.error(new RuntimeException("Invalid XML response structure from Registraduria service"));
            }

            RegistraduriaResponseDTO response = parseSoapResponse(soapResponse.getRawXmlResponse());
            if ("1".equals(response.getIdentityCardData().getErrorCode())){
                return Mono.empty();
            }
            return Mono.just(response);
        } catch (Exception e) {
            log.error("Error parsing SOAP response: {}", e.getMessage());
            return Mono.error(new RuntimeException("Error parsing identity card consultation response", e));
        }
    }

    /**
     * Parse SOAP XML response to clean JSON structure
     */
    private RegistraduriaResponseDTO parseSoapResponse(String soapResponse) {
        String cleanXml = RegistraduriaXmlParser.cleanXml(soapResponse);

        // Parse consultation status
        RegistraduriaResponseDTO.ConsultationStatusDTO status = RegistraduriaResponseDTO.ConsultationStatusDTO.builder()
                .controlNumber(RegistraduriaXmlParser.extractXmlValue(cleanXml, "numeroControl"))
                .errorCode(extractConsultationErrorCode(cleanXml))
                .errorDescription(RegistraduriaXmlParser.extractXmlValue(cleanXml, "descripcionError"))
                .consultationDateTime(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaHoraConsulta"))
                .build();

        // Parse identity card data
        RegistraduriaResponseDTO.IdentityCardDataDTO data = RegistraduriaResponseDTO.IdentityCardDataDTO.builder()
                .nuip(RegistraduriaXmlParser.extractXmlValue(cleanXml, "nuip"))
                .errorCode(extractIdentityCardErrorCode(cleanXml))
                .firstSurname(RegistraduriaXmlParser.extractXmlValue(cleanXml, "primerApellido"))
                .particle(RegistraduriaXmlParser.extractXmlValue(cleanXml, "particula"))
                .secondSurname(RegistraduriaXmlParser.extractXmlValue(cleanXml, "segundoApellido"))
                .firstName(RegistraduriaXmlParser.extractXmlValue(cleanXml, "primerNombre"))
                .secondName(RegistraduriaXmlParser.extractXmlValue(cleanXml, "segundoNombre"))
                .expeditionMunicipality(RegistraduriaXmlParser.extractXmlValue(cleanXml, "municipioExpedicion"))
                .expeditionDepartment(RegistraduriaXmlParser.extractXmlValue(cleanXml, "departamentoExpedicion"))
                .expeditionDate(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaExpedicion"))
                .identityCardStatus(RegistraduriaXmlParser.extractXmlValue(cleanXml, "estadoCedula"))
                .resolutionNumber(RegistraduriaXmlParser.extractXmlValue(cleanXml, "numResolucion"))
                .resolutionYear(RegistraduriaXmlParser.extractXmlValue(cleanXml, "anoResolucion"))
                .gender(RegistraduriaXmlParser.extractXmlValue(cleanXml, "genero"))
                .birthDate(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaNacimiento"))
                .informant(RegistraduriaXmlParser.extractXmlValue(cleanXml, "informante"))
                .serial(RegistraduriaXmlParser.extractXmlValue(cleanXml, "serial"))
                .deathDate(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaDefuncion"))
                .referenceDate(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaReferencia"))
                .affectationDate(RegistraduriaXmlParser.extractXmlValue(cleanXml, "fechaAfectacion"))
                .build();

        return RegistraduriaResponseDTO.builder()
                .consultationStatus(status)
                .identityCardData(data)
                .build();
    }

    /**
     * Extract consultation error code (from estadoConsulta/codError)
     */
    private String extractConsultationErrorCode(String xml) {
        // Look for codError within estadoConsulta section
        String startMarker = "<estadoConsulta>";
        String endMarker = "</estadoConsulta>";

        int startIndex = xml.indexOf(startMarker);
        if (startIndex == -1) {
            log.warn("estadoConsulta section not found in XML");
            return null;
        }

        int endIndex = xml.indexOf(endMarker, startIndex);
        if (endIndex == -1) {
            log.warn("estadoConsulta end tag not found in XML");
            return null;
        }

        String estadoConsultaSection = xml.substring(startIndex, endIndex + endMarker.length());
        String errorCode = RegistraduriaXmlParser.extractXmlValue(estadoConsultaSection, "codError");
        log.debug("Extracted consultation error code: '{}'", errorCode);
        return errorCode;
    }

    /**
     * Extract identity card error code (from datosCedulas/datos/codError)
     */
    private String extractIdentityCardErrorCode(String xml) {
        // Look for codError within datosCedulas/datos section
        String startMarker = "<datos>";
        String endMarker = "</datos>";

        int startIndex = xml.indexOf(startMarker);
        if (startIndex == -1) {
            log.warn("datos section not found in XML");
            return null;
        }

        int endIndex = xml.indexOf(endMarker, startIndex);
        if (endIndex == -1) {
            log.warn("datos end tag not found in XML");
            return null;
        }

        String datosSection = xml.substring(startIndex, endIndex + endMarker.length());
        String errorCode = RegistraduriaXmlParser.extractXmlValue(datosSection, "codError");
        log.debug("Extracted identity card error code: '{}'", errorCode);
        return errorCode;
    }
} 
