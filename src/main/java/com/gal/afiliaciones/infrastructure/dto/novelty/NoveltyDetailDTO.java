package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoveltyDetailDTO {

    private Long id;
    private String noveltyType;
    private String contributorIdentificationType;
    private String contributorIdentification;
    private Integer contributorDv;
    private String nameOrCompanyName;
    private String contributorType;
    private String contributantIdentificationType;
    private String contributantIdentification;
    private String completeContributantName;
    private String contributantType;
    private String contributantSubtype;
    private String department;
    private String municipality;
    private BigDecimal salary;
    private String risk;
    private BigDecimal riskRate;
    private String healthPromotingEntity;
    private String workCenterCode;
    private String occupationalRiskManager;
    private String economicActivity;
    private String initNoveltyDate;
    private String payrollType;
    private Long payrollNumber;
    private String causal;
    private Boolean isReview;
    private String noveltyIdentity;

}
