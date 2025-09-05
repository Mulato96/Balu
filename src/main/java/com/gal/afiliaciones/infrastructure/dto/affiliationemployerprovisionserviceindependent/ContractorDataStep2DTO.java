package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorDataStep2DTO {
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
    private AddressContractDataStep2DTO addressContractDataStep2DTO;
}
