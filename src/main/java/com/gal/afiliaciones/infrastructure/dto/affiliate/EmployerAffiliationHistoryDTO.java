package com.gal.afiliaciones.infrastructure.dto.affiliate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EmployerAffiliationHistoryDTO {

    private Long idAffiliate;
    private LocalDateTime affiliationDate;
    private LocalDate retirementDate;
    private String affiliationStatus;

}
