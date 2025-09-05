package com.gal.afiliaciones.infrastructure.dto.novelty;

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
public class NoveltyIndependentRequestDTO {

    private String IdentificationDocumentType;
    private String IdentificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private Long pensionFundAdministrator;
    private Long healthPromotingEntity;
    private Long department;
    private Long cityMunicipality;
    private String address;
    private String phone1;
    private String email;
    private String occupation;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private BigDecimal contractMonthlyValue;
    private String codeMainEconomicActivity;
    private String risk;
    private BigDecimal price;
    private String contributorTypeCode;
    private Integer contributantTypeCode;
    private Integer contributantSubtypeCode;

}
