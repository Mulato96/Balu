package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
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
public class AffiliationTaxiDriverIndependentUpdateDTO {

    private Long id;  // ID de la afiliación
    private WorkCenterAddressIndependentDTO workCenter;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private BigDecimal totalContractValue;
    private BigDecimal monthlyContractValue;
    private BigDecimal baseIncome;  // DTO para WorkCenter
    private EconomicActivityDTO economicActivity;  // DTO para EconomicActivity

}
