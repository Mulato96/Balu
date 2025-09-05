package com.gal.afiliaciones.infrastructure.dto.certificate;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBulkDTO {

    private String idDocument = UUID.randomUUID().toString();
    private long recordsTotal = 0;
    private long recordsValid = 0;
    private long recordsError = 0;
    private ExportDocumentsDTO document;

    public void calculateRecordsValid(){
        recordsValid = (recordsTotal - recordsError);
    }
}
