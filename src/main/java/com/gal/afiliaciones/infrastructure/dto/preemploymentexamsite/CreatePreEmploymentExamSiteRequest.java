package com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreEmploymentExamSiteRequest {

    @NotNull(message = "Required field")
    private String nameSite;
    @NotNull(message = "Required field")
    private Long phoneNumber;
    private String webSite;
    @NotNull(message = "Required field")
    private Long idDepartment;
    @NotNull(message = "Required field")
    private Long idMunicipality;
    @NotNull(message = "Required field")
    private String latitude;
    @NotNull(message = "Required field")
    private String longitude;
    @NotNull(message = "Required field")
    private String address;
    private Long idMainStreet;
    private Long idNumberMainStreet;
    private Long idLetter1MainStreet;
    private Boolean isBis;
    private Long idLetter2MainStreet;
    private Long idCardinalPointMainStreet;
    private Long idNum1SecondStreet;
    private Long idLetterSecondStreet;
    private Long idNum2SecondStreet;
    private Long idCardinalPoint2;
    private Long idHorizontalProperty1;
    private Long idNumHorizontalProperty1;
    private Long idHorizontalProperty2;
    private Long idNumHorizontalProperty2;
    private Long idHorizontalProperty3;
    private Long idNumHorizontalProperty3;
    private Long idHorizontalProperty4;
    private Long idNumHorizontalProperty4;

}
