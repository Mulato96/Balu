package com.gal.afiliaciones.infrastructure.dto.alfresco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlfrescoUploadResponse {

    private DataUpload data;
    private String message;
    private List<ErrorApiResponse> errors;

}
