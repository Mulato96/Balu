package com.gal.afiliaciones.infrastructure.dto.contractextension;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ContractExtensionRequest {

    private String filedNumber;
    private LocalDate contractEndDate;
    private BigDecimal contractTotalValue;
    private String contractDuration;

}
