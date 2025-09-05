package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermanentNoveltyDTO {

    private Long id;
    private Long noveltyTypeId;
    private LocalDateTime registryDate;
    private String contributorTypeCode;
    private String contributorIdentificationType;
    private String contributorIdentification;
    private Integer contributorDv;
    private String nameOrCompanyName;
    private Integer contributantTypeCode;
    private Integer contributantSubtypeCode;
    private String contributantIdentificationType;
    private String contributantIdentification;
    private String contributantFirstName;
    private String contributantSecondName;
    private String contributantSurname;
    private String contributantSecondSurname;
    private String departmentCode;
    private String municipalityCode;
    private BigDecimal salary;
    private String risk;
    private BigDecimal riskRate;
    private String epsCode;
    private String workCenterCode;
    private String arlCode;
    private String economicActivityCode;
    private LocalDate initNoveltyDate;
    private String payrollType;
    private Long payrollNumber;
    private String noveltyValue;
    private String addressContributor;
    private String phoneContributor;
    private String emailContributor;
    private boolean noveltyRetirementIncome;
    private Integer daysContributed;
    private LocalDate finishNoveltyDate;
    private String paymentPeriod;

}
