package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationTaxiDriverContractResponseDTO {

    private WorkCenterAddressIndependentDTO workCenter;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractDuration;
    private BigDecimal contractTotalValue;
    private BigDecimal contractMonthlyValue;
    private BigDecimal contractIbcValue;
    private String codeMainEconomicActivity;

}
