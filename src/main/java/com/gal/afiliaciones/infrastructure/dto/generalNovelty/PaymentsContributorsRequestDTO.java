package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsContributorsRequestDTO {
    private String startPeriod;
    private String endPeriod;
    private Integer page;
    private Integer size;
    private String documentType;
    private String documentNumber;
} 