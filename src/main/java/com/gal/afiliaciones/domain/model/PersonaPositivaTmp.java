package com.gal.afiliaciones.domain.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "personas_positiva_tmp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaPositivaTmp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // …el resto de columnas como antes, sin @Id…
    @Column(name = "anio")
    private BigDecimal year;

    @Column(name = "mes", nullable = false)
    private String month;

    @Column(name = "tipodocumentopers", nullable = false)
    private String personDocumentType;

    @Column(name = "numdocumentopers", nullable = false)
    private String personDocumentNumber;

    @Column(name = "fechacorte")
    private LocalDate cutOffDate;

    @Column(name = "razonsocialemp")
    private String corporateName;

    @Column(name = "tipodocumentoemp")
    private String employerDocumentType;

    @Column(name = "numdocumentoemp")
    private String employerDocumentNumber;

    @Column(name = "emailpersona")
    private String personEmail;

    @Column(name = "telefonopersona")
    private String personPhone;

    @Column(name = "fechavinculacion")
    private OffsetDateTime affiliationDate;

    @Column(name = "sectoreconomico")
    private String economicSector;

    @Column(name = "codactecono")
    private Integer economicActivityCode;

    @Column(name = "actividadeconomica")
    private String economicActivity;

    @Column(name = "riesgo")
    private Short risk;

    @Column(name = "tamanioemp")
    private String companySize;

    @Column(name = "iddepartamento")
    private Short departmentId;

    @Column(name = "departamento")
    private String department;

    @Column(name = "idmunicipio")
    private Short municipalityId;

    @Column(name = "municipio")
    private String municipality;

    @Column(name = "coordenadas")
    private String coordinates;

    @Column(name = "latitud")
    private Double latitude;

    @Column(name = "longitud")
    private Double longitude;

    @Column(name = "idocupacion")
    private Integer occupationId;

    @Column(name = "dependiente")
    private Short dependent;

    @Column(name = "independobligatorio")
    private Short mandatoryIndependent;

    @Column(name = "independvoluntario")
    private Short voluntaryIndependent;

    @Column(name = "tiporelacion")
    private String relationType;

    @Column(name = "estudiante")
    private String student;

    @Column(name = "genero")
    private String gender;

    @Column(name = "teletrabajo")
    private String telework;

    @Column(name = "rangoedad")
    private String ageRange;

    @Column(name = "edad")
    private Short age;

    @Column(name = "ibc")
    private BigDecimal ibc;


}
