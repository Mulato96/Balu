package com.gal.afiliaciones.infrastructure.dto.typeemployerdocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeEmployerDocumentDTO {

    private Long idDocument;
    private String nameDocument;
    private Boolean requestedDocument;
    private Long idSubTypeEmployer;
    private String nameSubTypeEmployer;
    private Long idTypeEmployer;
    private String nameTypeEmployer;
}
