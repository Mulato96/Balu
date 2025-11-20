package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Data;

@Data
public class MassiveUpdateResponseDTO {
    private String idDocument;
    private int recordsTotal;
    private int recordsValid;
    private int recordsError;
    private DocumentDTO document;
}
