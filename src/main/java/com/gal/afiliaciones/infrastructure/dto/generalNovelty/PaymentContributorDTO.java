package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

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
public class PaymentContributorDTO {
    private String payrollNumber;
    private String contributorName;
    private String affiliateName;
    private String affiliateIdentification;
    private LocalDate paymentDate;
    private String paymentPeriod;
    private Integer workedDays;
    private BigDecimal ibc;
    private BigDecimal contributionAmount;
    private String rate;
    private String refundAmount;
    private String payrollType;
    private Integer affiliateType;
    private Long idContributor;
} 