package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationIndependentStep1DTO {

    private Long idAffiliation;
    private String identificationTypeEmployer;
    private String identificationNumberEmployer;
    private Long idBondingType;
    private LocalDate coverageDate;
    private IndependentWorkerDTO worker;
    private Long idHeadquarter;
    private Long idDepartmentWorkCenter;
    private Long idCityWorkCenter;
    private String addressWorkCenter;

}
