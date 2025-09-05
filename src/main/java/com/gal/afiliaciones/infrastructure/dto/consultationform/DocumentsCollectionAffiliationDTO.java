package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsCollectionAffiliationDTO {

    private Long filedNumber;
    private LocalDate dateReceived;
    private String typeOfUpdate;

    private List<ViewingAssociatedDocumentsDTO> documents;

}
