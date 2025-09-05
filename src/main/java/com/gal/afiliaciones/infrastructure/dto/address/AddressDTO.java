package com.gal.afiliaciones.infrastructure.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String address;
    private Long idDepartment;
    private Long idCity;
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
