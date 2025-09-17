package com.gal.afiliaciones.infrastructure.service;

import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.registraduria.RegistraduriaResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistraduriaUnifiedService {

    private final IdentityCardConsultationService identityCardConsultationService;
    private final RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService;

    /**
     * Search user in national registry using the new Registraduria service
     * This method replaces the old searchUserInNationalRegistry implementations
     * @param identificationNumber Document number to search
     * @return List of RegistryOfficeDTO (maintains compatibility with existing code)
     */
    public List<RegistryOfficeDTO> searchUserInNationalRegistry(String identificationNumber) {
        log.info("Searching user in national registry using new Registraduria service for document: {}", identificationNumber);

        try {
            // Get access token using the unified service
            String token = registraduriaKeycloakTokenService.getAccessToken();
            log.debug("Access token obtained successfully for document: {}", identificationNumber);

            // Consult identity card using the new service
            RegistraduriaResponseDTO response = identityCardConsultationService.consultIdentityCard(identificationNumber)
                    .block(); // Convert Mono to blocking call for compatibility

            if (response == null || response.getIdentityCardData() == null) {
                log.warn("No data found for document: {}", identificationNumber);
                return new ArrayList<>();
            }

            // Map the new response to the expected RegistryOfficeDTO structure
            RegistryOfficeDTO registryOfficeDTO = mapToRegistryOfficeDTO(response, identificationNumber);
            List<RegistryOfficeDTO> result = new ArrayList<>();
            result.add(registryOfficeDTO);

            log.info("Successfully retrieved data for document: {}", identificationNumber);
            return result;

        } catch (Exception e) {
            log.error("Error searching user in national registry for document {}: {}", identificationNumber, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Map RegistraduriaResponseDTO to RegistryOfficeDTO for compatibility
     */
    private RegistryOfficeDTO mapToRegistryOfficeDTO(RegistraduriaResponseDTO response, String identificationNumber) {
        RegistraduriaResponseDTO.IdentityCardDataDTO data = response.getIdentityCardData();

        RegistryOfficeDTO registryOfficeDTO = new RegistryOfficeDTO();

        // Map basic identification data
        mapBasicIdentificationData(registryOfficeDTO, identificationNumber);

        // Map personal data
        mapPersonalData(registryOfficeDTO, data);

        // Map expedition data
        mapExpeditionData(registryOfficeDTO, data);

        // Map resolution data
        mapResolutionData(registryOfficeDTO, data);

        // Map status and other data
        mapStatusAndOtherData(registryOfficeDTO, data);

        // Map dates
        mapDates(registryOfficeDTO, data);

        // Set error code based on consultation status
        mapErrorCode(registryOfficeDTO, response);

        return registryOfficeDTO;
    }

    /**
     * Map basic identification data
     */
    private void mapBasicIdentificationData(RegistryOfficeDTO registryOfficeDTO, String identificationNumber) {
        registryOfficeDTO.setIdentificationNumber(Integer.parseInt(identificationNumber));
    }

    /**
     * Map personal data (names, birth date, gender)
     */
    private void mapPersonalData(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO.IdentityCardDataDTO data) {
        // Map name data
        registryOfficeDTO.setFirstName(getSafeString(data.getFirstName()));
        registryOfficeDTO.setSecondName(getSafeString(data.getSecondName()));
        registryOfficeDTO.setFirstLastName(getSafeString(data.getFirstSurname()));
        registryOfficeDTO.setSecondLastName(getSafeString(data.getSecondSurname()));

        if (data.getBirthDate() != null && !data.getBirthDate().trim().isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthDate = LocalDate.parse(data.getBirthDate(), inputFormatter);
                registryOfficeDTO.setBirthDate(birthDate.format(outputFormatter));
            } catch (Exception e) {
                registryOfficeDTO.setBirthDate("");
            }
        } else {
            registryOfficeDTO.setBirthDate("");
        }

        // Map gender with proper mapping
        log.debug("Raw gender from Registraduria service: '{}'", data.getGender());
        String mappedGender = mapGender(data.getGender());
        log.debug("Mapped gender result: '{}'", mappedGender);
        registryOfficeDTO.setGender(mappedGender);
    }

    /**
     * Map expedition data
     */
    private void mapExpeditionData(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO.IdentityCardDataDTO data) {
        registryOfficeDTO.setIssuingMunicipality(getSafeString(data.getExpeditionMunicipality()));
        registryOfficeDTO.setIssuingDepartment(getSafeString(data.getExpeditionDepartment()));
        registryOfficeDTO.setExpeditionDate(getSafeString(data.getExpeditionDate()));
    }

    /**
     * Map resolution data
     */
    private void mapResolutionData(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO.IdentityCardDataDTO data) {
        registryOfficeDTO.setResolutionNumber(getSafeString(data.getResolutionNumber()));
        registryOfficeDTO.setResolutionYear(getSafeString(data.getResolutionYear()));
    }

    /**
     * Map status and other data
     */
    private void mapStatusAndOtherData(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO.IdentityCardDataDTO data) {
        registryOfficeDTO.setIdStatus(getSafeString(data.getIdentityCardStatus()));
        registryOfficeDTO.setSerialNumber(getSafeString(data.getSerial()));
        registryOfficeDTO.setInformate(getSafeString(data.getInformant()));
    }

    /**
     * Map dates
     */
    private void mapDates(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO.IdentityCardDataDTO data) {
        registryOfficeDTO.setDeathDate(getSafeString(data.getDeathDate()));
        registryOfficeDTO.setReferenceDate(getSafeString(data.getReferenceDate()));
        registryOfficeDTO.setAffectingDate(getSafeString(data.getAffectationDate()));
    }

    /**
     * Map error code based on consultation status
     */
    private void mapErrorCode(RegistryOfficeDTO registryOfficeDTO, RegistraduriaResponseDTO response) {
        if (response.getConsultationStatus() != null && response.getConsultationStatus().getErrorCode() != null) {
            try {
                registryOfficeDTO.setErrorCode(Integer.parseInt(response.getConsultationStatus().getErrorCode()));
            } catch (NumberFormatException e) {
                registryOfficeDTO.setErrorCode(0);
            }
        } else {
            registryOfficeDTO.setErrorCode(0);
        }
    }

    /**
     * Get safe string value (null-safe)
     */
    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    /**
     * Map gender from Spanish to abbreviated format
     * @param gender Gender value from Registraduria service
     * @return Mapped gender value (M for MASCULINO, F for FEMENINO, original value for others)
     */
    public static String mapGender(String gender) {
        log.debug("Mapping gender: '{}'", gender);

        if (gender == null || gender.trim().isEmpty()) {
            log.debug("Gender is null or empty, returning empty string");
            return "";
        }

        String normalizedGender = gender.trim().toUpperCase();
        log.debug("Normalized gender: '{}'", normalizedGender);

        switch (normalizedGender) {
            case "MASCULINO":
                log.debug("Mapping MASCULINO to M");
                return "M";
            case "FEMENINO":
                log.debug("Mapping FEMENINO to F");
                return "F";
            default:
                log.warn("Unknown gender value: '{}' (normalized: '{}'), returning original value", gender, normalizedGender);
                return gender;
        }
    }

}
