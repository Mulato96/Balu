package com.gal.afiliaciones.infrastructure.dto.affiliationdetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliationDetailDTO {

    private Long id;

    private String documentType;

    private String documentNumber;

    private String risk;

    private String fee;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    private String stageManagement;

    private Long codeContributantType;

    private String economicActivity;

}
