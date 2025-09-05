package com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "affiliation_detail")
public class Affiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_hiring_domestic_service")
    private Boolean isHiringDomesticService;

    @Column(name = "num_domestic_service")
    private int numDomesticService;

    @Column(name = "is_hiring_nurse")
    private Boolean isHiringNurse;

    @Column(name = "num_nurse")
    private int numNurse;

    @Column(name = "is_hiring_butler")
    private Boolean isHiringButler;

    @Column(name = "num_butler")
    private int numButler;

    @Column(name = "is_hiring_driver")
    private Boolean isHiringDriver;

    @Column(name = "num_driver")
    private int numDriver;

    @Column(name = "is_rural_zone_employer")
    private Boolean isRuralZoneEmployer;

    @Column(name = "department_employer")
    private Long departmentEmployer;

    @Column(name = "municipality_employer")
    private Long municipalityEmployer;

    @Column(name = "address_employer")
    private String addressEmployer;

    @Column(name = "id_main_street_employer")
    private Long idMainStreetEmployer;

    @Column(name = "id_number_main_street_employer")
    private Long idNumberMainStreetEmployer;

    @Column(name = "id_letter1_main_street_employer")
    private Long idLetter1MainStreetEmployer;

    @Column(name = "bis_employer")
    private Boolean isBisEmployer;

    @Column(name = "id_letter2_main_street_employer")
    private Long idLetter2MainStreetEmployer;

    @Column(name = "id_cardinal_point_main_street_employer")
    private Long idCardinalPointMainStreetEmployer;

    @Column(name = "id_number1_second_street_employer")
    private Long idNum1SecondStreetEmployer;

    @Column(name = "id_letter_second_street_employer")
    private Long idLetterSecondStreetEmployer;

    @Column(name = "id_number2_second_street_employer")
    private Long idNum2SecondStreetEmployer;

    @Column(name = "id_cardinal_point2_employer")
    private Long idCardinalPoint2Employer;

    @Column(name = "id_horizontal_property1_employer")
    private Long idHorizontalProperty1Employer;

    @Column(name = "id_number_horizontal_property1_employer")
    private Long idNumHorizontalProperty1Employer;

    @Column(name = "id_horizontal_property2_employer")
    private Long idHorizontalProperty2Employer;

    @Column(name = "id_number_horizontal_property2_employer")
    private Long idNumHorizontalProperty2Employer;

    @Column(name = "id_horizontal_property3_employer")
    private Long idHorizontalProperty3Employer;

    @Column(name = "id_number_horizontal_property3_employer")
    private Long idNumHorizontalProperty3Employer;

    @Column(name = "id_horizontal_property4_employer")
    private Long idHorizontalProperty4Employer;

    @Column(name = "id_number_horizontal_property4_employer")
    private Long idNumHorizontalProperty4Employer;

    @Column(name = "phone_1")
    private String phone1;

    @Column(name = "phone_2")
    private String phone2;

    @Column(name = "current_arl")
    private String currentARL;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "is_foreign_pension")
    private Boolean isForeignPension;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "email")
    private String email;

    @Column(name = "identification_document_type")
    private String identificationDocumentType;

    @Column(name = "identification_document_number")
    private String identificationDocumentNumber;

    @Column(name = "person_type")
    private String personType;

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
    private String age;

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

    @Column(name = "department")
    private Long department;

    @Column(name = "city_municipality")
    private Long cityMunicipality;

    @Column(name = "is_rural_zone")
    private Boolean isRuralZone;

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

    @Column(name = "secondary_phone_1")
    private String secondaryPhone1;

    @Column(name = "secondary_phone_2")
    private String secondaryPhone2;

    @Column(name = "secondary_email")
    private String secondaryEmail;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "date_request")
    private String dateRequest;

    @Column(name = "type_affiliation")
    private String typeAffiliation;

    @Column(name = "stage_management")
    private String stageManagement;

    @Column(name = "id_folder_alfresco")
    private String idFolderAlfresco;

    @Column(name = "id_procedure_type")
    private Long idProcedureType;

    @Column(name = "name_legal_nature_employer")
    private String nameLegalNatureEmployer;

    @Column(name = "code_legal_nature_employer")
    private Long codeLegalNatureEmployer;

    @Column(name = "code_contributor_type")
    private String codeContributorType;

    @Column(name = "id_main_headquarter")
    private Long idMainHeadquarter;

    @Column(name = "code_main_economic_activity")
    private String codeMainEconomicActivity;

    @Column(name = "num_headquarters")
    private int numHeadquarters;

    @Column(name = "num_work_centers")
    private int numWorkCenters;

    @Column(name = "initial_number_workers")
    private int initialNumberWorkers;

    @Column(name = "total_payroll_value")
    private BigDecimal totalPayrollValue;

    @OneToMany(mappedBy = "affiliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffiliateActivityEconomic> economicActivity;

    @Column(name = "id_family_member")
    private Long idFamilyMember;

    @Column(name = "risk")
    private String risk;

    @Column(name = "price_risk")
    private BigDecimal price;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "contract_duration")
    private String contractDuration;

    @Column(name = "contract_total_value")
    private BigDecimal contractTotalValue;

    @Column(name = "contract_monthly_value")
    private BigDecimal contractMonthlyValue;

    @Column(name = "contract_ibc_value")
    private BigDecimal contractIbcValue;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;
    @Column(name = "identification_document_type_contractor")
    private String identificationDocumentTypeContractor;
    @Column(name = "identification_document_number_contractor")
    private String identificationDocumentNumberContractor;
    @Column(name = "dv")
    private Integer dv;
    @Column(name = "identification_document_type_legal_representative")
    private String identificationDocumentTypeLegalRepresentative;
    @Column(name = "identification_document_number_contractor_legal_representative")
    private String identificationDocumentNumberContractorLegalRepresentative;
    @Column(name = "legal_rep_first_name")
    private String legalRepFirstName;
    @Column(name = "legal_rep_second_name")
    private String legalRepSecondName;
    @Column(name = "legal_rep_surname")
    private String legalRepSurname;
    @Column(name = "legal_rep_second_surname")
    private String legalRepSecondSurname;
    @Column(name = "first_name_contractor")
    private String firstNameContractor;
    @Column(name = "second_name_contractor")
    private String secondNameContractor;
    @Column(name = "surname_contractor")
    private String surnameContractor;
    @Column(name = "second_surname_contractor")
    private String secondSurnameContractor;
    @Column(name = "email_contractor")
    private String emailContractor;
    @Column(name = "first_name_independent_worker")
    private String firstNameIndependentWorker;
    @Column(name = "second_name_independent_worker")
    private String secondNameIndependentWorker;
    @Column(name = "surname_independent_worker")
    private String surnameIndependentWorker;
    @Column(name = "second_surname_independent_worker")
    private String secondSurnameIndependentWorker;
    @Column(name = "date_birth_independent_worker")
    private LocalDate dateOfBirthIndependentWorker;
    @Column(name = "nationality_independent_worker")
    private Long nationalityIndependentWorker;
    @Column(name = "address_independent_worker")
    private String addressIndependentWorker;
    @Column(name = "id_department_independent_worker")
    private Long idDepartmentIndependentWorker;
    @Column(name = "id_city_independent_worker")
    private Long idCityIndependentWorker;
    @Column(name = "id_main_street_independent_worker")
    private Long idMainStreetIndependentWorker;
    @Column(name = "id_number_main_street_independent_worker")
    private Long idNumberMainStreetIndependentWorker;
    @Column(name = "id_letter1_main_street_independent_worker")
    private Long idLetter1MainStreetIndependentWorker;
    @Column(name = "bis_independent_worker")
    private Boolean isBisIndependentWorker;
    @Column(name = "id_letter2_main_street_independent_worker")
    private Long idLetter2MainStreetIndependentWorker;
    @Column(name = "id_cardinal_point_main_street_independent_worker")
    private Long idCardinalPointMainStreetIndependentWorker;
    @Column(name = "id_number1_second_street_independent_worker")
    private Long idNum1SecondStreetIndependentWorker;
    @Column(name = "id_letter_second_street_independent_worker")
    private Long idLetterSecondStreetIndependentWorker;
    @Column(name = "id_number2_second_street_independent_worker")
    private Long idNum2SecondStreetIndependentWorker;
    @Column(name = "id_cardinal_point2_independent_worker")
    private Long idCardinalPoint2IndependentWorker;
    @Column(name = "id_horizontal_property1_independent_worker")
    private Long idHorizontalProperty1IndependentWorker;
    @Column(name = "id_number_horizontal_property1_independent_worker")
    private Long idNumHorizontalProperty1IndependentWorker;
    @Column(name = "id_horizontal_property2_independent_worker")
    private Long idHorizontalProperty2IndependentWorker;
    @Column(name = "id_number_horizontal_property2_independent_worker")
    private Long idNumHorizontalProperty2IndependentWorker;
    @Column(name = "id_horizontal_property3_independent_worker")
    private Long idHorizontalProperty3IndependentWorker;
    @Column(name = "id_number_horizontal_property3_independent_worker")
    private Long idNumHorizontalProperty3IndependentWorker;
    @Column(name = "id_horizontal_property4_independent_worker")
    private Long idHorizontalProperty4IndependentWorker;
    @Column(name = "id_number_horizontal_property4_independent_worker")
    private Long idNumHorizontalProperty4IndependentWorker;
    @Column(name = "address_work_data_center")
    private String addressWorkDataCenter;
    @Column(name = "id_department_work_data_center")
    private Long idDepartmentWorkDataCenter;
    @Column(name = "id_city_work_data_center")
    private Long idCityWorkDataCenter;
    @Column(name = "id_main_street_work_data_center")
    private Long idMainStreetWorkDataCenter;
    @Column(name = "id_number_main_street_work_data_center")
    private Long idNumberMainStreetWorkDataCenter;
    @Column(name = "id_letter1_main_street_work_data_center")
    private Long idLetter1MainStreetWorkDataCenter;
    @Column(name = "bis_work_data_center")
    private Boolean isBisWorkDataCenter;
    @Column(name = "id_letter2_main_street_work_data_center")
    private Long idLetter2MainStreetWorkDataCenter;
    @Column(name = "id_cardinal_point_main_street_work_data_center")
    private Long idCardinalPointMainStreetWorkDataCenter;
    @Column(name = "id_number1_second_street_work_data_center")
    private Long idNum1SecondStreetWorkDataCenter;
    @Column(name = "id_letter_second_street_work_data_center")
    private Long idLetterSecondStreetWorkDataCenter;
    @Column(name = "id_number2_second_street_work_data_center")
    private Long idNum2SecondStreetWorkDataCenter;
    @Column(name = "id_cardinal_point2_work_data_center")
    private Long idCardinalPoint2WorkDataCenter;
    @Column(name = "id_horizontal_property1_work_data_center")
    private Long idHorizontalProperty1WorkDataCenter;
    @Column(name = "id_number_horizontal_property1_work_data_center")
    private Long idNumHorizontalProperty1WorkDataCenter;
    @Column(name = "id_horizontal_property2_work_data_center")
    private Long idHorizontalProperty2WorkDataCenter;
    @Column(name = "id_number_horizontal_property2_work_data_center")
    private Long idNumHorizontalProperty2WorkDataCenter;
    @Column(name = "id_horizontal_property3_work_data_center")
    private Long idHorizontalProperty3WorkDataCenter;
    @Column(name = "id_number_horizontal_property3_work_data_center")
    private Long idNumHorizontalProperty3WorkDataCenter;
    @Column(name = "id_horizontal_property4_work_data_center")
    private Long idHorizontalProperty4WorkDataCenter;
    @Column(name = "id_number_horizontal_property4_work_data_center")
    private Long idNumHorizontalProperty4WorkDataCenter;
    @Column(name = "phone1_work_data_center")
    private String phone1WorkDataCenter;
    @Column(name = "phone2_work_data_center")
    private String phone2WorkDataCenter;
    @Column(name = "address_contract_data_step_2")
    private String addressContractDataStep2;
    @Column(name = "id_department_contract_data_step_2")
    private Long idDepartmentContractDataStep2;
    @Column(name = "id_city_contract_data_step_2")
    private Long idCityContractDataStep2;
    @Column(name = "id_main_street_contract_data_step_2")
    private Long idMainStreetContractDataStep2;
    @Column(name = "id_number_main_street_contract_data_step_2")
    private Long idNumberMainStreetContractDataStep2;
    @Column(name = "id_letter1_main_street_contract_data_step_2")
    private Long idLetter1MainStreetContractDataStep2;
    @Column(name = "bis_contract_data_step_2")
    private Boolean isBisContractDataStep2;
    @Column(name = "id_letter2_main_street_contract_data_step_2")
    private Long idLetter2MainStreetContractDataStep2;
    @Column(name = "id_cardinal_point_main_street_contract_data_step_2")
    private Long idCardinalPointMainStreetContractDataStep2;
    @Column(name = "id_number1_second_street_contract_data_step_2")
    private Long idNum1SecondStreetContractDataStep2;
    @Column(name = "id_letter_second_street_contract_data_step_2")
    private Long idLetterSecondStreetContractDataStep2;
    @Column(name = "id_number2_second_street_contract_data_step_2")
    private Long idNum2SecondStreetContractDataStep2;
    @Column(name = "id_cardinal_point2_contract_data_step_2")
    private Long idCardinalPoint2ContractDataStep2;
    @Column(name = "id_horizontal_property1_contract_data_step_2")
    private Long idHorizontalProperty1ContractDataStep2;
    @Column(name = "id_number_horizontal_property1_contract_data_step_2")
    private Long idNumHorizontalProperty1ContractDataStep2;
    @Column(name = "id_horizontal_property2_contract_data_step_2")
    private Long idHorizontalProperty2ContractDataStep2;
    @Column(name = "id_number_horizontal_property2_contract_data_step_2")
    private Long idNumHorizontalProperty2ContractDataStep2;
    @Column(name = "id_horizontal_property3_contract_data_step_2")
    private Long idHorizontalProperty3ContractDataStep2;
    @Column(name = "id_number_horizontal_property3_contract_data_step_2")
    private Long idNumHorizontalProperty3ContractDataStep2;
    @Column(name = "id_horizontal_property4_contract_data_step_2")
    private Long idHorizontalProperty4ContractDataStep2;
    @Column(name = "id_number_horizontal_property4_contract_data_step_2")
    private Long idNumHorizontalProperty4ContractDataStep2;
    @Column(name = "contract_quality")
    private String contractQuality;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "duration")
    private String duration;
    @Column(name = "journey_established")
    private String journeyEstablished;

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
    @Column(name = "transport_supply")
    private Boolean transportSupply;
    @Column(name = "is_same_employer_address")
    private Boolean isSameEmployerAddress;
    @Column(name = "date_regularization")
    private LocalDateTime dateRegularization;

    @Column(name = "is723")
    private Boolean is723;
    @Column(name = "special_nit")
    private String specialTaxIdentificationNumber;

    @Column(name = "is_vip")
    private Boolean isVip;
    @Column(name = "code_contributant_type")
    private Long codeContributantType;
    @Column(name = "code_contributant_subtype")
    private String codeContributantSubtype;

    @Column(name = "id_employer_size")
    private Long idEmployerSize;
    @Column(name = "real_number_workers")
    private Long realNumberWorkers;

    @Column(name = "ibc_percentage")
    private BigDecimal ibcPercentage;

}
