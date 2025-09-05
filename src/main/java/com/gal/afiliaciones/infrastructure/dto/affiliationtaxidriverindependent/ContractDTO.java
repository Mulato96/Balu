package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

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
public class ContractDTO {

    private String contractType;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    private String contractDuration;

    private BigDecimal contractTotalValue;

    private BigDecimal contractMonthlyValue;

    private BigDecimal contractIbcValue;

}
