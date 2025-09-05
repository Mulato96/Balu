package com.gal.afiliaciones.infrastructure.dto.alfrescoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplacedDocumentDTO {

    private String documentName;
    private String documentId;

}
