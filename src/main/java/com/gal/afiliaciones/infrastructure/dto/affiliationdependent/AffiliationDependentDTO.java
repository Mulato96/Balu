package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationDependentDTO {

    private Long idAffiliation;
    private String identificationTypeEmployer;
    private String identificationNumberEmployer;
    private Long idAffiliateEmployer;
    private Long idBondingType;
    private LocalDate coverageDate;
    private LocalDate practiceEndDate;
    private DependentWorkerDTO worker;
    private Long idHeadquarter;
    private Long idDepartmentWorkCenter;
    private Long idCityWorkCenter;
    private String addressWorkCenter;
    private String economicActivityCode;
    private Integer risk;
    @Builder.Default
    private Boolean fromPila = false;
    private Long idWorkCenter;

}
