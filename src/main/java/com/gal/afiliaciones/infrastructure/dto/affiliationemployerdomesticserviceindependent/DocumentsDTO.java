package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentsDTO {

    private Long id;
    private String idDocument;
    private boolean reject;
    private boolean revised;
    private String name;
    private String dateTime;
}
