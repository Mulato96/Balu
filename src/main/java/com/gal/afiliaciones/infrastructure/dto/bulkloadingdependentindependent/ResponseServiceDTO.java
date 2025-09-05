package com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseServiceDTO {

    private String totalRecord;
    private String recordSuccessful;
    private String recordError;
    private ExportDocumentsDTO document;
}
