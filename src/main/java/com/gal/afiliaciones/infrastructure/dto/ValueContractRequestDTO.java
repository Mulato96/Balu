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
public class ValueContractRequestDTO {
    private String numContract;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private BigDecimal contractTotalValue;
    private BigDecimal contractMonthlyValue;
    private String typeContractUser;
}
