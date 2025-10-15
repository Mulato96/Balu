package com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "affiliate_mercantile")
@Builder
public class AffiliateMercantile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "type_document_identification")
    private String typeDocumentIdentification;
    @Column(name = "number_identification")
    private String numberIdentification;
    @Column(name = "digit_verification_dv")
    private Integer digitVerificationDV;
    @Column(name = "business_name")
    private String businessName;
    @Column(name = "type_person")
    private String typePerson;
    @Column(name = "number_workers")
    private Long numberWorkers;
    @Column(name = "zone_location_employer")
    private String zoneLocationEmployer;
    @Column(name = "department")
    private Long department;
    @Column(name = "city_municipality")
    private Long cityMunicipality;
    @Column(name = "phone_one")
    private String phoneOne;
    @Column(name = "phone_two")
    private String phoneTwo;
    @Column(name = "email")
    private String email;
    @Column(name = "number_document_person_responsible")
    private String numberDocumentPersonResponsible;
    @Column(name = "type_document_person_responsible")
    private String typeDocumentPersonResponsible;
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
    @Column(name = "affiliation_status")
    private String affiliationStatus;
    @Column(name = "id_type_employer")
    private Long idTypeEmployer;
    @Column(name = "id_sub_type_employer")
    private Long idSubTypeEmployer;
    @Column(name = "arl")
    private String arl;
    @Column(name = "date_interview")
    private LocalDateTime dateInterview;
    @Column(name = "type_affiliation")
    private String typeAffiliation;
    @Column(name = "sub_type_affiliation")
    private String subTypeAffiliation;
    @Column(name = "legal_status")
    private String legalStatus;
    @Column(name = "date_regularization")
    private LocalDateTime dateRegularization;

    @Column(name = "department_contact_company")
    private Long departmentContactCompany;
    @Column(name = "city_municipality_contact_company")
    private Long cityMunicipalityContactCompany;

    @Column(name = "address_contact_company")
    private String addressContactCompany;
    @Column(name = "id_department_contact_company")
    private Long idDepartmentContactCompany;
    @Column(name = "id_city_contact_company")
    private Long idCityContactCompany;
    @Column(name = "id_main_street_contact_company")
    private Long idMainStreetContactCompany;
    @Column(name = "id_number_main_street_contact_company")
    private Long idNumberMainStreetContactCompany;
    @Column(name = "id_letter_1_main_street_contact_company")
    private Long idLetter1MainStreetContactCompany;
    @Column(name = "is_bis_contact_company")
    private Boolean isBisContactCompany;
    @Column(name = "id_letter_2_main_street_contact_company")
    private Long idLetter2MainStreetContactCompany;
    @Column(name = "id_cardinal_point_main_street_contact_company")
    private Long idCardinalPointMainStreetContactCompany;
    @Column(name = "id_num_1_second_street_contact_company")
    private Long idNum1SecondStreetContactCompany;
    @Column(name = "id_letter_second_street_contact_company")
    private Long idLetterSecondStreetContactCompany;
    @Column(name = "id_num_2_second_street_contact_company")
    private Long idNum2SecondStreetContactCompany;
    @Column(name = "id_cardinal_point_2_contact_company")
    private Long idCardinalPoint2ContactCompany;
    @Column(name = "id_horizontal_property_1_contact_company")
    private Long idHorizontalProperty1ContactCompany;
    @Column(name = "id_num_horizontal_property_1_contact_company")
    private Long idNumHorizontalProperty1ContactCompany;
    @Column(name = "id_horizontal_property_2_contact_company")
    private Long idHorizontalProperty2ContactCompany;
    @Column(name = "id_num_horizontal_property_2_contact_company")
    private Long idNumHorizontalProperty2ContactCompany;
    @Column(name = "id_horizontal_property_3_contact_company")
    private Long idHorizontalProperty3ContactCompany;
    @Column(name = "id_num_horizontal_property_3_contact_company")
    private Long idNumHorizontalProperty3ContactCompany;
    @Column(name = "id_horizontal_property_4_contact_company")
    private Long idHorizontalProperty4ContactCompany;
    @Column(name = "id_num_horizontal_property_4_contact_company")
    private Long idNumHorizontalProperty4ContactCompany;

    @Column(name = "address_is_equals_Contact_Company")
    private Boolean addressIsEqualsContactCompany;
    @Column(name = "phone_one_contact_company")
    private String phoneOneContactCompany;
    @Column(name = "phone_two_contact_company")
    private String phoneTwoContactCompany;
    @Column(name = "email_contact_company")
    private String emailContactCompany;

    @Column(name = "id_user_pre_register")
    private Long idUserPreRegister;
    @Column(name = "afp")
    private Long afp;
    @Column(name = "eps")
    private Long eps;

    @Column(name = "filed_number")
    private String filedNumber;
    @Column(name = "date_request")
    private String dateRequest;
    @Column(name = "date_create_affiliate")
    private LocalDate dateCreateAffiliate = (LocalDate.now());
    @Column(name = "stage_management")
    private String stageManagement;
    @Column(name = "id_folder_alfresco")
    private String idFolderAlfresco;

    @Column(name = "affiliation_cancelled")
    private Boolean affiliationCancelled =  false;
    @Column(name = "status_document")
    private Boolean statusDocument =  false;

    @OneToMany(mappedBy = "affiliateMercantile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffiliateActivityEconomic> economicActivity;

    @Column(name = "id_main_headquarter")
    private Long idMainHeadquarter;

    @Column(name = "id_procedure_type")
    private Long idProcedureType;

    //numeros de telefono del representante legal

    @Column(name = "phone_one_legal_representative")
    private String phoneOneLegalRepresentative;
    @Column(name = "phone_two_legal_representative")
    private String phoneTwoLegalRepresentative;

    //direccion del representante legal

    @Column(name = "address_legal_representative")
    private String addressLegalRepresentative;
    @Column(name = "id_department_legal_representative")
    private Long idDepartmentLegalRepresentative;
    @Column(name = "id_city_legal_representative")
    private Long idCityLegalRepresentative;
    @Column(name = "id_main_street_legal_representative")
    private Long idMainStreetLegalRepresentative;
    @Column(name = "id_number_main_street_legal_representative")
    private Long idNumberMainStreetLegalRepresentative;
    @Column(name = "id_letter_1_main_street_legal_representative")
    private Long idLetter1MainStreetLegalRepresentative;
    @Column(name = "is_bis_legal_representative")
    private Boolean isBisLegalRepresentative;
    @Column(name = "id_letter_2_main_street_legal_representative")
    private Long idLetter2MainStreetLegalRepresentative;
    @Column(name = "id_cardinal_point_main_street_legal_representative")
    private Long idCardinalPointMainStreetLegalRepresentative;
    @Column(name = "id_num_1_second_street_legal_representative")
    private Long idNum1SecondStreetLegalRepresentative;
    @Column(name = "id_letter_second_street_legal_representative")
    private Long idLetterSecondStreetLegalRepresentative;
    @Column(name = "id_num_2_second_street_legal_representative")
    private Long idNum2SecondStreetLegalRepresentative;
    @Column(name = "id_cardinal_point_2_legal_representative")
    private Long idCardinalPoint2LegalRepresentative;
    @Column(name = "id_horizontal_property_1_legal_representative")
    private Long idHorizontalProperty1LegalRepresentative;
    @Column(name = "id_num_horizontal_property_1_legal_representative")
    private Long idNumHorizontalProperty1LegalRepresentative;
    @Column(name = "id_horizontal_property_2_legal_representative")
    private Long idHorizontalProperty2LegalRepresentative;
    @Column(name = "id_num_horizontal_property_2_legal_representative")
    private Long idNumHorizontalProperty2LegalRepresentative;
    @Column(name = "id_horizontal_property_3_legal_representative")
    private Long idHorizontalProperty3LegalRepresentative;
    @Column(name = "id_num_horizontal_property_3_legal_representative")
    private Long idNumHorizontalProperty3LegalRepresentative;
    @Column(name = "id_horizontal_property_4_legal_representative")
    private Long idHorizontalProperty4LegalRepresentative;
    @Column(name = "id_num_horizontal_property_4_legal_representative")
    private Long idNumHorizontalProperty4LegalRepresentative;

    @Column(name = "is_vip")
    private Boolean isVip;
    @Column(name = "code_contributor_type")
    private String codeContributorType;

    @Column(name = "id_employer_size")
    private Long idEmployerSize;
    @Column(name = "real_number_workers")
    private Long realNumberWorkers;

    @Column(name = "decentralized_consecutive")
    private Long decentralizedConsecutive;


    // Exposición del id de afiliado sin afectar inserciones/actualizaciones
    @Column(name = "id_affiliate", nullable = false)
    private Long idAffiliate;

}
