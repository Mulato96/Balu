package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class UserMain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "primer_nombre")
    private String firstName;
    @Column(name = "segundo_nombre")
    private String secondName;
    @Column(name = "primer_apellido")
    private String surname;
    @Column(name = "segundo_apellido")
    private String secondSurname;
    @Column(name = "razon_social")
    private String companyName;
    @Column(name = "tipo")
    private Long userType;
    @Column(name = "estado")
    private Long status;
    @Column(name = "email")
    private String email;
    @Column(name = "telefono")
    private String phoneNumber;
    @Column(name = "telefono_2")
    private String phoneNumber2;
    @Column(name = "pin")
    private String pin;
    @Column(name = "fecha_registro")
    private Timestamp createDate;
    @Column(name = "tipo_documento")
    private String identificationType;
    @Column(name = "numero_identificacion")
    private String identification;
    @Column(name = "dv")
    private Integer verificationDigit;
    @Column(name = "accept_notification")
    private Boolean acceptNotification;
    @Column(name = "perfil")
    private String profile;
    @Column(name = "date_birth")
    private LocalDate dateBirth;
    @Column(name = "age")
    private Integer age;
    @Column(name = "sex")
    private String sex;
    @Column(name = "other_sex")
    private String otherSex;
    @Column(name = "nationality")
    private Long nationality;
    @Column(name = "address")
    private String address;
    @Column(name = "status_pre_register")
    private Boolean statusPreRegister;
    @Column(name = "status_active")
    private Boolean statusActive;
    @Column(name = "status_start_affiliate")
    private Boolean statusStartAfiiliate;
    @Column(name = "last_affiliation_attempt")
    private LocalDateTime lastAffiliationAttempt;
    @Column(name = "status_inactive_since")
    private LocalDateTime statusInactiveSince;
    @Column(name = "login_attempts")
    private Integer loginAttempts;
    @Column(name = "lockout_time")
    private LocalDateTime lockoutTime;
    @Column(name = "valid_attempts")
    private Integer validAttempts;
    @Column(name = "valid_time")
    private LocalDateTime validOutTime;
    @Column(name = "generate_attempts")
    private Integer generateAttempts;
    @Column(name = "generate_time")
    private LocalDateTime generateOutTime;
    @Column(name = "id_department")
    private Long idDepartment;
    @Column(name = "id_city")
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
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    @Column(name = "eps")
    private Long healthPromotingEntity;
    @Column(name = "afp")
    private Long pensionFundAdministrator;
    @Column(name = "last_password_update")
    private LocalDateTime lastPasswordUpdate;
    @Column(name = "password_expired")
    private Boolean isPasswordExpired;
    @Column(name = "Inactive_By_Pending_Affiliation")
    private Boolean inactiveByPendingAffiliation;

    @Builder.Default
    @Column(name = "is_temporal_password")
    private Boolean isTemporalPassword = false;

    @Builder.Default
    @Column(name = "created_at_temporal_password")
    private LocalDate createdAtTemporalPassword = LocalDate.now();

    @OneToOne
    @JoinColumn(name = "id_operador_informacion", nullable = true)
    private Operator InfoOperator;

    @OneToOne
    @JoinColumn(name = "id_operador_financiero", nullable = true)
    private Operator financialOperator;

    @Column(name = "is_in_arrears_status")
    private Boolean isInArrearsStatus;

    @Column(name = "nombre_usuario")
    private String userName;

    @Column(name = "is_import")
    private Boolean isImport;

    @Column(name = "assigned_password")
    private Boolean assignedPassword;

    @Column(name = "employer_update_time")
    private LocalDateTime employerUpdateTime;

    @Column(name = "code_otp")
    private String codeOtp;

    @Column(name = "id_cargo")
    private Integer position;

    @Column(name = "id_oficina")
    private Integer office;

    @Column(name = "\"Area\"")
    private Long area;

    @Column(name = "nivel_autorizacion")
    private String levelAuthorization;

    @ManyToMany
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Role> roles;
    
}
