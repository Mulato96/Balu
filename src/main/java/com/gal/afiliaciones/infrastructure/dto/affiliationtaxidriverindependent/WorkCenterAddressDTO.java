package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCenterAddressDTO {

    // Datos de departamento y ciudad (empresa)
    private Long idDepartment;
    private Long idCity;

    // Dirección de la empresa (Calle principal, Número, Letras, Bis, etc.)
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

    // Propiedad Horizontal (empresa)
    private Long idHorizontalProperty1;
    private Long idNumHorizontalProperty1;
    private Long idHorizontalProperty2;
    private Long idNumHorizontalProperty2;
    private Long idHorizontalProperty3;
    private Long idNumHorizontalProperty3;
    private Long idHorizontalProperty4;
    private Long idNumHorizontalProperty4;

    // Dirección completa de la empresa
    private String fullAddress;

    // Datos de departamento y ciudad (contacto de la empresa)
    private Long idDepartmentContactCompany;
    private Long idCityContactCompany;

    // Dirección de contacto de la empresa (Calle principal, Número, Letras, Bis, etc.)
    private Long idMainStreetContactCompany;
    private Long idNumberMainStreetContactCompany;
    private Long idLetter1MainStreetContactCompany;
    private Boolean isBisContactCompany;
    private Long idLetter2MainStreetContactCompany;
    private Long idCardinalPointMainStreetContactCompany;

    private Long idNum1SecondStreetContactCompany;
    private Long idLetterSecondStreetContactCompany;
    private Long idNum2SecondStreetContactCompany;
    private Long idCardinalPoint2ContactCompany;

    // Propiedad Horizontal (contacto de la empresa)
    private Long idHorizontalProperty1ContactCompany;
    private Long idNumHorizontalProperty1ContactCompany;
    private Long idHorizontalProperty2ContactCompany;
    private Long idNumHorizontalProperty2ContactCompany;
    private Long idHorizontalProperty3ContactCompany;
    private Long idNumHorizontalProperty3ContactCompany;
    private Long idHorizontalProperty4ContactCompany;
    private Long idNumHorizontalProperty4ContactCompany;

    // Información adicional del contacto de la empresa
    private String addressContactCompany;

}
