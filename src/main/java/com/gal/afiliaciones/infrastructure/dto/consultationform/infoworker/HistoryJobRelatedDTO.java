package com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryJobRelatedDTO {

    private String affiliationStatus;
    private String typeOfLinkage;
    private LocalDate linkDate;
    private String lastCoverageDate;
    private String retirementDate;
    private String economicActivity;
    private int riskLevel;
    private BigDecimal rate;
    private String paymentStatus;
}
