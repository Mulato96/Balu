package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDataIndependentDTO {

    private String contractQuality;
    private String contractType;
    private boolean transportSupply;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private String journeyEstablished;
    private BigDecimal contractTotalValue;
    private BigDecimal contractMonthlyValue;
    private BigDecimal contractIbcValue;
    private String economicActivityCode;

}
