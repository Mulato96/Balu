package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataContributionVolunteerDTO {

    private String occupation;
    private String risk;
    private BigDecimal price;
    private BigDecimal contractIbcValue;
    private BigDecimal ibcPercentage;

}
