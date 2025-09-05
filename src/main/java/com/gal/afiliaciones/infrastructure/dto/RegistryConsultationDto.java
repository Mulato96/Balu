package com.gal.afiliaciones.infrastructure.dto;

import com.gal.afiliaciones.infrastructure.enums.DocumentTypeEnumExclNI;
import com.gal.afiliaciones.infrastructure.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistryConsultationDto {
    private DocumentTypeEnumExclNI documentType;
    private String documentNumber;
    private String fistName;
    private String secondName;
    private String fistLastName;
    private String secondLastName;
    private LocalDate dateBirth;
    private int age;
    private SexEnum sex;
}
