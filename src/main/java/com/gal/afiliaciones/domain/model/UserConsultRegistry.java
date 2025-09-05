package com.gal.afiliaciones.domain.model;

import com.gal.afiliaciones.infrastructure.enums.DocumentTypeEnumExclNI;
import com.gal.afiliaciones.infrastructure.enums.SexEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Data
@Table(name = "tmp_consult_user_registry")
public class UserConsultRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentTypeEnumExclNI documentType;
    @Column(name = "document_number")
    private String documentNumber;
    @Column(name = "fist_name")
    private String fistName;
    @Column(name = "second_name")
    private String secondName;
    @Column(name = "fist_last_name")
    private String fistLastName;
    @Column(name = "second_last_name")
    private String secondLastName;
    @Column(name = "date_birth")
    private LocalDate dateBirth;
    @Column(name = "age")
    private int age;
    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private SexEnum sex;
}
