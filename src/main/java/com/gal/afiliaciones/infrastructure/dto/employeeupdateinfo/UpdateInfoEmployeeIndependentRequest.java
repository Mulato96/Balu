package com.gal.afiliaciones.infrastructure.dto.employeeupdateinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInfoEmployeeIndependentRequest {

    private Long id;
    private String filedNumer;

    private String typeIdentification;
    private String identification;

    @NotNull
    private Long department;
    @NotNull
    private Long city;
    @NotNull
    private Long mainStreet;

    private Long mainStreetLetter1;
    private boolean bis;
    private Long mainStreetLetter2;
    private Long mainStreetDirection;

    @NotNull
    private Long idNum1SecondStreet;
    private Long idLetterSecondStreet;
    @NotNull
    private Long idNum2SecondStreet;
    private Long idCardinalPoint2;

    @NotNull
    private Long mainStreetNumber;
    private Long mainStreetNumberDirection;
    private Long additionalHorizontalProperty1;
    private Long additionalHorizontalProperty1Number;
    private Long additionalHorizontalProperty2;
    private Long additionalHorizontalProperty2Number;
    private Long additionalHorizontalProperty3;
    private Long additionalHorizontalProperty3Number;
    private Long additionalHorizontalProperty4;
    private Long additionalHorizontalProperty4Number;
    private String fullAddress;
    @NotNull
    private String primaryPhone;
    private String secondaryPhone;

    @NotNull
    private String email;
    @NotNull
    private Long eps;
    @NotNull
    private Long afp;

}
