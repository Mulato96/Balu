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
        name = "tmp_excel_dependientes",
        schema = "temp_dev",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tmp_excel_dependientes_doc", columnNames = {"tipo_documento", "numero_documento"})
        },
        indexes = {
                @Index(name = "idx_tmp_excel_dependientes_documento", columnList = "tipo_documento, numero_documento"),
                @Index(name = "idx_tmp_excel_dependientes_estado", columnList = "estado_consulta"),
                @Index(name = "idx_tmp_excel_dependientes_fuente", columnList = "fuente")
        }
)
public class ExcelDependentTmp {

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

    // Datos de contacto
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String address;

    @Column(name = "telefono", length = 50)
    private String phone;

    // Datos de ubicación
    @Column(name = "codigo_dane_departamento_residencia", length = 10)
    private String residenceDepartmentDaneCode;

    @Column(name = "codigo_dane_municipio_residencia", length = 10)
    private String residenceMunicipalityDaneCode;

    // Datos laborales/cobertura
    @Column(name = "codigo_eps", length = 20)
    private String epsCode;

    @Column(name = "codigo_afp", length = 20)
    private String afpCode;

    @Column(name = "fecha_inicio_cobertura")
    private LocalDate coverageStartDate;

    @Column(name = "codigo_ocupacion", length = 20)
    private String occupationCode;

    @Column(name = "salario_ibc", precision = 12, scale = 2)
    private BigDecimal salaryIbc;

    @Column(name = "codigo_actividad_economica", length = 20)
    private String economicActivityCode;

    // Datos del empleador/trabajo
    @Column(name = "codigo_departamento_labora", length = 10)
    private String workDepartmentCode;

    @Column(name = "codigo_ciudad_labora", length = 10)
    private String workCityCode;

    @Column(name = "tipo_documento_empleador", length = 20)
    private String employerDocumentType;

    @Column(name = "numero_documento_empleador", length = 50)
    private String employerDocumentNumber;

    @Column(name = "codigo_sub_empresa", length = 20)
    private String subCompanyCode;

    @Column(name = "modo_trabajo")
    private Integer workMode;

    // Metadatos
    @Column(name = "fuente", length = 20)
    private String source;

    @Column(name = "fecha_carga")
    private LocalDateTime loadDate;

    @Column(name = "estado_consulta", length = 20)
    private String queryStatus;
}


