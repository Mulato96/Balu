package com.gal.afiliaciones.domain.model.retirement;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RetirementRequestDto {
    private String documentType;
    private String documentNumber;
    private LocalDate retirementDate;
    private Long contractId;
}