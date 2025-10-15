package com.gal.afiliaciones.infrastructure.dto.registraduria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistraduriaErrorDTO {
    private String errorCode;
    private String errorMessage;
    private String documentNumber;
    private LocalDateTime timestamp;
    private String errorType;

    public static RegistraduriaErrorDTO networkError(String documentNumber, String errorMessage) {
        return RegistraduriaErrorDTO.builder()
                .errorCode("NETWORK_ERROR")
                .errorMessage(errorMessage)
                .documentNumber(documentNumber)
                .timestamp(LocalDateTime.now())
                .errorType("NETWORK")
                .build();
    }

    public static RegistraduriaErrorDTO parsingError(String documentNumber, String errorMessage) {
        return RegistraduriaErrorDTO.builder()
                .errorCode("PARSING_ERROR")
                .errorMessage(errorMessage)
                .documentNumber(documentNumber)
                .timestamp(LocalDateTime.now())
                .errorType("PARSING")
                .build();
    }

    public static RegistraduriaErrorDTO timeoutError(String documentNumber) {
        return RegistraduriaErrorDTO.builder()
                .errorCode("TIMEOUT_ERROR")
                .errorMessage("Request timeout - service not responding")
                .documentNumber(documentNumber)
                .timestamp(LocalDateTime.now())
                .errorType("TIMEOUT")
                .build();
    }

    public static RegistraduriaErrorDTO validationError(String documentNumber, String errorMessage) {
        return RegistraduriaErrorDTO.builder()
                .errorCode("VALIDATION_ERROR")
                .errorMessage(errorMessage)
                .documentNumber(documentNumber)
                .timestamp(LocalDateTime.now())
                .errorType("VALIDATION")
                .build();
    }
} 