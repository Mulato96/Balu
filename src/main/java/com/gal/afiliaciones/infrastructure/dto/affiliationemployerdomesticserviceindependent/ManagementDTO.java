package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManagementDTO {

    private Long id;
    private List<DocumentsDTO> documents;
    private Object affiliation;
    private DataDailyDTO dataDailyDTO;
}
