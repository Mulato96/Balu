package com.gal.afiliaciones.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "recaudo")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_recaudo", length = 30)
    private String collectionType;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private UserMain userId;

    @Column(name = "periodo_pago")
    private String paymentPeriod;

    @Column(name = "tipo_planilla", length = 255)
    private String payrollType;

    @Column(name = "fecha_pago")
    private LocalDateTime paymentDate;

    @Column(name = "codigo_arl", length = 255)
    private String arlCode;

    @Column(name = "tipo_persona", length = 255)
    private String personType;

    @Column(name = "tipo_identificacion_cotizante")
    private String typeIdentificationContributor;

    @Column(name = "numero_identificacion_cotizante")
    private String numberIdentificationContributor;

    @Column(name = "primer_nombre_cotizante")
    private String firstNameContributor;

    @Column(name = "segundo_nombre_cotizante")
    private String secondNameContributor;

    @Column(name = "primer_apellido_cotizante")
    private String firsSurnameContributor;

    @Column(name = "segundo_apellido_cotizante")
    private String secondSurnameContributor;

    @Column(name = "numero_planilla", length = 255)
    private Long payrollNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "id_estado")
    private BigDecimal statusId;

    @Column(name = "valor_cotizacion")
    private BigDecimal contributionValue;

    @Column(name = "forma_presentacion")
    private String presentationMethod;

    @Column(name = "codigo_operador", length = 120)
    private Integer operatorCode;

    @Column(name = "modalidad_planilla")
    private Short payrollMethod;

    @Column(name = "dias_mora")
    private Integer overdueDays;

    @Column(name = "clase_aportante")
    private String classOfContributor;

    @Column(name = "naturaleza_juridica")
    private Short legalNature;

    @Column(name = "tipo_cotizante")
    private Integer contributorType;

    @Column(name = "subtipo_cotizante")
    private Integer contributorSubtype;

    @Column(name = "codigo_depto_trabajo", length = 255)
    private String workDeptCode;

    @Column(name = "codigo_municipio_trabajo", length = 255)
    private String workMunicipalityCode;

    @Column(name = "dias_cotizados")
    private Integer quotedDays;

    @Column(name = "salario_base")
    private BigDecimal baseSalary;

    @Column(name = "ingreso_base_cotizacion")
    private BigDecimal baseContributionIncome;

    @Column(name = "tarifa_aporte_arl")
    private Float contributionFeeToArl;

    @Column(name = "centro_trabajo")
    private Integer workCenter;

    @Column(name = "codigo_administradora_pension", length = 255)
    private String payerAdministratorCode;

    @Column(name = "codigo_eps_eoc", length = 255)
    private String epsEocCode;

    @Column(name = "correcciones", length = 255)
    private String corrections;

    @Column(name = "tipo_salario", length = 255)
    private String salaryType;

    @Column(name = "categoria_riesgo", length = 255)
    private Short riskCategory;

    @Column(name = "fecha_ingreso")
    private LocalDate admissionDate;

    @Column(name = "fecha_inicio")
    private LocalDate startDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    @Column(name = "colombiano_exterior")
    private Boolean foreignColombian;

    @Column(name = "extranjero_no_obligado")
    private Boolean nonMandatoryForeigner;

    @Column(name = "numero_planilla_asociada")
    private Long associatedPayrollNumber;

    @Column(name = "tipo_documento_aportante", length = 255)
    private String contributorDocumentType;

    @Column(name = "numero_identificacion_aportante", length = 255)
    private String contributorDocumentNumber;

    @Column(name = "tipo_aportante")
    private String typeContributor;

    @Column(name = "digito_verificacion_aportante", length = 255)
    private Long contributorVerificationDigit;

    @Column(name = "numero_de_planillas_pagadas", precision = 38, scale = 2)
    private BigDecimal paidPayrollsNumber;

    @Column(name = "numero_radicacion_planilla", length = 255)
    private Long payrollFilingNumber;

    @Column(name = "numero_registro", length = 255)
    private Long recordNumber;

    @Column(name = "tipo_registro", length = 255)
    private String recordType;

    @Column(name = "numero_secuencial_planilla", length = 255)
    private Long sequentialPayrollNumber;

    @Column(name = "total_cotizacion_obligatoria", precision = 38, scale = 2)
    private BigDecimal totalMandatoryContribution;

    @Column(name = "numero_total_de_planillas_reportadas", precision = 38, scale = 2)
    private BigDecimal totalPaidPayrollsNumber;

    @Column(name = "telefono", length = 10, nullable = false)
    private String phoneNumber;

    @Column(name = "correo_electronico", length = 60)
    private String emailAddress;

    @Column(name = "direccion_correspondencia", length = 40, nullable = false)
    private String mailingAddress;

    @Column(name = "status")
    private String status;

    @Column(name = "numero_total_empleados", length = 99999, nullable = false)
    private Integer totalEmployeeNumber;

    @Column(name = "nombre_o_razon_social_aportante", length = 200, nullable = false)
    private String contributorNameOrBusinessName;

    @Column(name = "number_receipt")
    private Integer numberReceipt;

    @Column(name = "date_issue")
    private LocalDate dateIssue;

    @Column(name = "id_alfresco_payment_receipt")
    private String idAlfrescoPaymentReceipt;

    @Column(name = "codigo_sucursal_o_dependencia", length = 10)
    private String branchOrDepartmentCode;

    @Column(name = "activity_code", length = 4)
    private Integer activityCode;
}