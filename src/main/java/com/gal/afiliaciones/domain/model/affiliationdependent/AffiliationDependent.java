package com.gal.afiliaciones.domain.model.affiliationdependent;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "affiliation_dependent")
@Builder
public class AffiliationDependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_bonding_type")
    private Long idBondingType;
    @Column(name = "coverage_date")
    private LocalDate coverageDate;
    @Column(name = "identification_type")
    private String identificationDocumentType;
    @Column(name = "identification_number")
    private String identificationDocumentNumber;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "second_name")
    private String secondName;
    @Column(name = "surname")
    private String surname;
    @Column(name = "second_surname")
    private String secondSurname;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "age")
    private Integer age;
    @Column(name = "gender")
    private String gender;
    @Column(name = "other_gender")
    private String otherGender;
    @Column(name = "nationality")
    private Long nationality;
    @Column(name = "health_promoting_entity")
    private Long healthPromotingEntity;
    @Column(name = "pension_fund_administrator")
    private Long pensionFundAdministrator;
    @Column(name = "occupational_risk_manager")
    private String occupationalRiskManager;
    @Column(name = "id_department")
    private Long idDepartment;
    @Column(name = "id_city_municipality")
    private Long idCity;
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
    @Column(name = "address")
    private String address;
    @Column(name = "phone1")
    private String phone1;
    @Column(name = "phone2")
    private String phone2;
    @Column(name = "email")
    private String email;
    @Column(name = "id_work_modality")
    private Long idWorkModality;
    @Column(name = "salary")
    private BigDecimal salary;
    @Column(name = "id_occupation")
    private Long idOccupation;
    @Column(name = "id_headquarter")
    private Long idHeadquarter;
    @Column(name = "id_department_work_center")
    private Long idDepartmentWorkCenter;
    @Column(name = "id_city_work_center")
    private Long idCityWorkCenter;
    @Column(name = "address_work_center")
    private String addressWorkCenter;
    @Column(name = "economic_activity_code")
    private String economicActivityCode;
    @Column(name = "filed_number")
    private String filedNumber;
    @Column(name = "risk")
    private Integer risk;
    @Column(name = "price_risk")
    private BigDecimal priceRisk;
    @Column(name = "contract_quality")
    private String contractQuality;
    @Column(name = "contract_type")
    private String contractType;
    @Column(name = "transport_supply")
    private Boolean transportSupply;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "duration")
    private String duration;
    @Column(name = "journey_established")
    private String journeyEstablished;
    @Column(name = "contract_total_value")
    private BigDecimal contractTotalValue;
    @Column(name = "contract_ibc_value")
    private BigDecimal contractIbcValue;
    @Column(name = "identification_document_type_signatory")
    private String identificationDocumentTypeSignatory;
    @Column(name = "identification_document_number_signatory")
    private String identificationDocumentNumberSignatory;
    @Column(name = "first_name_signatory")
    private String firstNameSignatory;
    @Column(name = "second_name_signatory")
    private String secondNameSignatory;
    @Column(name = "surname_signatory")
    private String surnameSignatory;
    @Column(name = "second_surname_signatory")
    private String secondSurnameSignatory;
    @Column(name = "occupation_signatory")
    private String occupationSignatory;
    @Column(name = "code_activity_employer")
    private String codeActivityEmployer;
    @Column(name = "code_activity_contract")
    private String codeActivityContract;
    @Column(name = "bulk_upload_affiliation")
    private Boolean bulkUploadAffiliation;

    @Column(name = "code_contributant_type")
    private Long codeContributantType;
    @Column(name = "code_contributant_subtype")
    private String codeContributantSubtype;

    @Column(name = "pending_complete_form_pila")
    private Boolean pendingCompleteFormPila;

    @Column(name = "ibc_percentage")
    private BigDecimal ibcPercentage;

    @Column(name = "id_affiliate_employer")
    private Long idAffiliateEmployer;

    @Column(name = "id_work_center")
    private Long idWorkCenter;

}
