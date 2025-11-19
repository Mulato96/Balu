package com.gal.afiliaciones.infrastructure.dto;

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
public class ValueContractDTO {

    private String numContract;
    private String contractType;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractDuration;
    private BigDecimal contractTotalValue;
    private BigDecimal contractMonthlyValue;
    private BigDecimal contractIbcValue;
    private String contractStatus;
    private String contractTypeVinculation;
    private String typeContractUser;

}
