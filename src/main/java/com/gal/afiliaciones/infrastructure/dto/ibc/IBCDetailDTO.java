package com.gal.afiliaciones.infrastructure.dto.ibc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IBCDetailDTO {

    private Long id;
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractDuration;
    private BigDecimal totalContractValue;
    private BigDecimal monthlyContractValue;
    private BigDecimal ibcValue;

}
