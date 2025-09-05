package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCenterAddressIndependentDTO {

    private Long idDepartmentWorkDataCenter;
    private Long idCityWorkDataCenter;
    private Long idMainStreetWorkDataCenter;
    private Long idNumberMainStreetWorkDataCenter;
    private Long idLetter1MainStreetWorkDataCenter;
    private Boolean isBisWorkDataCenter;
    private Long idLetter2MainStreetWorkDataCenter;
    private Long idCardinalPointMainStreetWorkDataCenter;
    private Long idNum1SecondStreetWorkDataCenter;
    private Long idLetterSecondStreetWorkDataCenter;
    private Long idNum2SecondStreetWorkDataCenter;
    private Long idCardinalPoint2WorkDataCenter;
    private Long idHorizontalProperty1WorkDataCenter;
    private Long idNumHorizontalProperty1WorkDataCenter;
    private Long idHorizontalProperty2WorkDataCenter;
    private Long idNumHorizontalProperty2WorkDataCenter;
    private Long idHorizontalProperty3WorkDataCenter;
    private Long idNumHorizontalProperty3WorkDataCenter;
    private Long idHorizontalProperty4WorkDataCenter;
    private Long idNumHorizontalProperty4WorkDataCenter;
    private String addressWorkDataCenter;
    private String phone1WorkDataCenter;
    private String phone2WorkDataCenter;

}
