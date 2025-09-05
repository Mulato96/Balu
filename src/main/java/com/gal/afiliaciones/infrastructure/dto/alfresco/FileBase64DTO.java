package com.gal.afiliaciones.infrastructure.dto.alfresco;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileBase64DTO {

    private String fileName;
    private String base64Image;

}
