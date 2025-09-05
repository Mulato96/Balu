package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "family_member")
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identification_document_type")
    private String idDocumentTypeFamilyMember;

    @Column(name = "identification_document_number")
    private String idDocumentNumberFamilyMember;

    @Column(name = "first_name")
    private String firstNameFamilyMember;

    @Column(name = "second_name")
    private String secondNameFamilyMember;

    @Column(name = "surname")
    private String surnameFamilyMember;

    @Column(name = "second_surname")
    private String secondSurnameFamilyMember;

    @Column(name = "department")
    private Long department;

    @Column(name = "city_municipality")
    private Long cityMunicipality;

    @Column(name = "address")
    private String address;

    @Column(name = "id_main_street")
    private Long idMainStreet;

    @Column(name = "id_number_main_street")
    private Long idNumberMainStreet;

    @Column(name = "id_letter1_main_street")
    private Long idLetter1MainStreet;

    @Column(name = "bis")
    private Boolean isBis;

    @Column(name = "id_letter2_main_street")
    private Long idLetter2MainStreet;

    @Column(name = "id_cardinal_point_main_street")
    private Long idCardinalPointMainStreet;

    @Column(name = "id_number1_second_street")
    private Long idNum1SecondStreet;

    @Column(name = "id_letter_second_street")
    private Long idLetterSecondStreet;

    @Column(name = "id_number2_second_street")
    private Long idNum2SecondStreet;

    @Column(name = "id_cardinal_point2")
    private Long idCardinalPoint2;

    @Column(name = "id_horizontal_property1")
    private Long idHorizontalProperty1;

    @Column(name = "id_number_horizontal_property1")
    private Long idNumHorizontalProperty1;

    @Column(name = "id_horizontal_property2")
    private Long idHorizontalProperty2;

    @Column(name = "id_number_horizontal_property2")
    private Long idNumHorizontalProperty2;

    @Column(name = "id_horizontal_property3")
    private Long idHorizontalProperty3;

    @Column(name = "id_number_horizontal_property3")
    private Long idNumHorizontalProperty3;

    @Column(name = "id_horizontal_property4")
    private Long idHorizontalProperty4;

    @Column(name = "id_number_horizontal_property4")
    private Long idNumHorizontalProperty4;

    @Column(name = "phone_1")
    private String phone1FamilyMember;

    @Column(name = "phone_2")
    private String phone2FamilyMember;

    @Column(name = "email")
    private String emailFamilyMember;

}
