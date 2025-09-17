package com.gal.afiliaciones.infrastructure.dto.registraduria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistraduriaSoapResponseDTO {

    private String rawXmlResponse;
    private boolean isSuccess;
    private String errorMessage;

    public static RegistraduriaSoapResponseDTO success(String xmlResponse) {
        return RegistraduriaSoapResponseDTO.builder()
                .rawXmlResponse(xmlResponse)
                .isSuccess(true)
                .build();
    }

    public static RegistraduriaSoapResponseDTO error(String errorMessage) {
        return RegistraduriaSoapResponseDTO.builder()
                .isSuccess(false)
                .errorMessage(errorMessage)
                .build();
    }

}