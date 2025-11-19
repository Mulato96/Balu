package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSearchFilterDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;

}