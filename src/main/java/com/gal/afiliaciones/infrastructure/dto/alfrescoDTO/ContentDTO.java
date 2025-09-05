package com.gal.afiliaciones.infrastructure.dto.alfrescoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDTO {
    private String mimeType;
    private String mimeTypeName;
    private int sizeInBytes;
    private String encoding;
}
