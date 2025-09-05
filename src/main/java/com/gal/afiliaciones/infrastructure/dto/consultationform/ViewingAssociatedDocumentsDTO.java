package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewingAssociatedDocumentsDTO implements Serializable {

    private Long id;
    private String createdDate;
    private String responseDocumentType;
    private String documentId;
}