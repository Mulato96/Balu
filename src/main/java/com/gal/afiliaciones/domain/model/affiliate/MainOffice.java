package com.gal.afiliaciones.domain.model.affiliate;

import com.gal.afiliaciones.domain.model.UserMain;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "main_office")
public class MainOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String mainOfficeName;
    @Column(name = "zone")
    private String mainOfficeZone;
    @Column(name = "phone")
    private String mainOfficePhoneNumber;
    @Column(name = "email")
    private String mainOfficeEmail;

    @Column(name = "main")
    private Boolean main;
    @Column(name = "phone_two")
    private String mainPhoneNumberTwo;

    @Column(name = "id_affiliate")
    private Long idAffiliate;

    @Column(name = "id_sede_positiva")
    private Long idSedePositiva;

    @Column(name = "type_document_responsible_headquarters")
    private String typeDocumentResponsibleHeadquarters;
    @Column(name = "number_document_responsible_headquarters")
    private String numberDocumentResponsibleHeadquarters;
    @Column(name = "firs_name_responsible_headquarters")
    private String firstNameResponsibleHeadquarters;
    @Column(name = "second_name_responsible_headquarters")
    private String secondNameResponsibleHeadquarters;
    @Column(name = "surname_responsible_headquarters")
    private String surnameResponsibleHeadquarters;
    @Column(name = "second_surname_responsible_headquarters")
    private String secondSurnameResponsibleHeadquarters;
    @Column(name = "phone_one_responsible_headquarters")
    private String phoneOneResponsibleHeadquarters;
    @Column(name = "phone_two_responsible_headquarters")
    private String phoneTwoResponsibleHeadquarters;
    @Column(name = "email_responsible_headquarters")
    private String emailResponsibleHeadquarters;
    //direccion
    @Column(name = "address")
    private String address;
    @Column(name = "id_department")
    private Long idDepartment;
    @Column(name = "id_city")
    private Long idCity;
    @Column(name = "id_main_street")
    private Long idMainStreet;
    @Column(name = "id_number_main_street")
    private Long idNumberMainStreet;
    @Column(name = "id_letter_1_main_street")
    private Long idLetter1MainStreet;
    @Column(name = "is_bis")
    private Boolean isBis;
    @Column(name = "id_letter_2_main_street")
    private Long idLetter2MainStreet;
    @Column(name = "id_cardinal_point_main_street")
    private Long idCardinalPointMainStreet;
    @Column(name = "id_num_1_second_street")
    private Long idNum1SecondStreet;
    @Column(name = "id_letter_second_street")
    private Long idLetterSecondStreet;
    @Column(name = "id_num_2_second_street")
    private Long idNum2SecondStreet;
    @Column(name = "id_cardinal_point_2")
    private Long idCardinalPoint2;
    @Column(name = "id_horizontal_property_1")
    private Long idHorizontalProperty1;
    @Column(name = "id_num_horizontal_property_1")
    private Long idNumHorizontalProperty1;
    @Column(name = "id_horizontal_property_2")
    private Long idHorizontalProperty2;
    @Column(name = "id_num_horizontal_property_2")
    private Long idNumHorizontalProperty2;
    @Column(name = "id_horizontal_property_3")
    private Long idHorizontalProperty3;
    @Column(name = "id_num_horizontal_property_3")
    private Long idNumHorizontalProperty3;
    @Column(name = "id_horizontal_property_4")
    private Long idHorizontalProperty4;
    @Column(name = "id_num_horizontal_property_4")
    private Long idNumHorizontalProperty4;

    @ManyToOne
    @JoinColumn(name = "office_manager", referencedColumnName = "id")
    private UserMain officeManager;

}