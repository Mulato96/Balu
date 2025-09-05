package com.gal.afiliaciones.infrastructure.dto.contractextension;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractExtensionInfoDTO {

    private String contractQuality;
    private String contractType;
    private Boolean contractTrasnport;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractDuration;
    private BigDecimal contractMonthlyValue;
    private BigDecimal contractTotalValue;
    private BigDecimal contractIBC;
    private String journeyEstablishment;

}
