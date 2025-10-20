package com.gal.afiliaciones.infrastructure.controller.massive_withdrawal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {
    private String idDocument;
    private int recordsTotal;
    private int recordsValid;
    private int recordsError;
    private DocumentDTO document;
}