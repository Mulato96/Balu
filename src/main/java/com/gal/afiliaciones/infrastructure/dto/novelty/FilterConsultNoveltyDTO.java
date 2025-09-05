package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterConsultNoveltyDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private Long noveltyTypeId;
    private Long noveltyStatusId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long requestChannelId;
    private int page;
    private int size;
    private String sortBy;
    private String sortOrder;

}
