package com.gal.afiliaciones.infrastructure.dto.alfresco;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorApiResponse {

    private List<String> codes;
    private List<Object> arguments;
    private String defaultMessage;
    private String objectName;
    private String code;

}
