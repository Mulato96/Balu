package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "tmp_excel_independientes",
        schema = "temp_dev",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tmp_excel_independientes_doc", columnNames = {"tipo_documento", "numero_documento"})
        },
        indexes = {
                @Index(name = "idx_tmp_excel_independientes_documento", columnList = "tipo_documento, numero_documento"),
                @Index(name = "idx_tmp_excel_independientes_estado", columnList = "estado_consulta"),
                @Index(name = "idx_tmp_excel_independientes_fuente", columnList = "fuente")
        }
)
public class ExcelIndependentTmp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos de identificación
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String documentType;

    @Column(name = "numero_documento", nullable = false, length = 50)
    private String documentNumber;

    // Datos personales
    @Column(name = "primer_apellido", length = 100)
    private String firstSurname;

    @Column(name = "segundo_apellido", length = 100)
    private String secondSurname;

    @Column(name = "primer_nombre", length = 100)
    private String firstName;

    @Column(name = "segundo_nombre", length = 100)
    private String secondName;

    @Column(name = "fecha_nacimiento")
    private LocalDate dateOfBirth;

    @Column(name = "sexo", length = 1)
    private String sex;

    @Column(name = "correo_electronico", length = 150)
    private String email;

    // Datos de contacto y ubicación
    @Column(name = "direccion_residencia", columnDefinition = "TEXT")
    private String residenceAddress;

    @Column(name = "telefono", length = 50)
    private String phone;

    @Column(name = "codigo_dane_departamento_residencia", length = 10)
    private String residenceDepartmentDaneCode;

    @Column(name = "codigo_dane_municipio_residencia", length = 10)
    private String residenceMunicipalityDaneCode;

    // Datos laborales
    @Column(name = "cargo_ocupacion", length = 100)
    private String occupationTitle;

    @Column(name = "codigo_eps", length = 20)
    private String epsCode;

    @Column(name = "codigo_afp", length = 20)
    private String afpCode;

    @Column(name = "codigo_ocupacion", length = 20)
    private String occupationCode;

    // Datos de contrato
    @Column(name = "tipo_contrato")
    private Integer contractType;

    @Column(name = "naturaleza_contrato")
    private Integer contractNature;

    @Column(name = "suministra_transporte", length = 1)
    private String providesTransport;

    @Column(name = "fecha_inicio_contrato")
    private LocalDate contractStartDate;

    @Column(name = "fecha_terminacion_contrato")
    private LocalDate contractEndDate;

    @Column(name = "valor_total_contrato", precision = 15, scale = 2)
    private BigDecimal totalContractValue;

    @Column(name = "codigo_actividad_ejecutar", length = 20)
    private String activityToExecuteCode;

    // Ubicación laboral
    @Column(name = "codigo_departamento_labora", length = 10)
    private String workDepartmentCode;

    @Column(name = "codigo_ciudad_labora", length = 10)
    private String workCityCode;

    @Column(name = "fecha_inicio_cobertura")
    private LocalDate coverageStartDate;

    // Datos del contratante
    @Column(name = "tipo_documento_contratante", length = 20)
    private String contractorDocumentType;

    @Column(name = "numero_documento_contratante", length = 50)
    private String contractorDocumentNumber;

    @Column(name = "codigo_subempresa", length = 20)
    private String subCompanyCode;

    @Column(name = "actividad_centro_trabajo", length = 100)
    private String workCenterActivity;

    // Campos específicos
    @Column(name = "ingreso_base_cotizacion", precision = 12, scale = 2)
    private BigDecimal baseContributionIncome;

    @Column(name = "es_taxista", length = 2)
    private String isTaxiDriver;

    // Datos del cónyuge/responsable
    @Column(name = "tipo_documento_conyuge", length = 20)
    private String spouseDocumentType;

    @Column(name = "numero_documento_conyuge", length = 50)
    private String spouseDocumentNumber;

    @Column(name = "primer_nombre_conyuge", length = 100)
    private String spouseFirstName;

    @Column(name = "segundo_nombre_conyuge", length = 100)
    private String spouseSecondName;

    @Column(name = "primer_apellido_conyuge", length = 100)
    private String spouseFirstSurname;

    @Column(name = "segundo_apellido_conyuge", length = 100)
    private String spouseSecondSurname;

    @Column(name = "departamento_conyuge", length = 50)
    private String spouseDepartment;

    @Column(name = "municipio_conyuge", length = 50)
    private String spouseMunicipality;

    @Column(name = "telefono_conyuge", length = 50)
    private String spousePhone;

    // Metadatos
    @Column(name = "fuente", length = 20)
    private String source;

    @Column(name = "tipo_independiente", length = 20)
    private String independentType;

    @Column(name = "fecha_carga")
    private LocalDateTime loadDate;

    @Column(name = "estado_consulta", length = 20)
    private String queryStatus;
}


