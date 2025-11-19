package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO unificado para retornar detalles de trabajadores (dependientes e independientes)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerDetailDTO {

    private Long idAffiliate;
    private String filedNumber;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String completeName;

    private String contractType;
    private String contractQuality;
    private Boolean contractTransport;
    private String journeyEstablishment;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractDuration;
    private LocalDate coverageDate;

    private BigDecimal contractTotalValue;
    private BigDecimal contractMonthlyValue;
    private BigDecimal contractIbcValue;

}

