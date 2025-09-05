package com.gal.afiliaciones.infrastructure.dto.typeemployerdocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDTO {

    private Long idDocument;
    private String file;
    private String name;

}

