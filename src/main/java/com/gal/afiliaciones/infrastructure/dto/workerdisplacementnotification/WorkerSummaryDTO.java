package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen del trabajador para cabecera de respuesta")
public class WorkerSummaryDTO {

    @Schema(description = "Tipo de documento del trabajador", example = "CC")
    private String documentType;

    @Schema(description = "Número de documento del trabajador", example = "80067374")
    private String documentNumber;

    @Schema(description = "Nombre completo del trabajador", example = "DAGO LEONARDO FONSECA GARCIA")
    private String fullName;

    @Schema(description = "Tipo de vinculación/afiliación", example = "Trabajador Independiente")
    private String affiliationType;

    @Schema(description = "Nombre para mostrar", example = "CC - 80067374 (DAGO LEONARDO FONSECA GARCIA)")
    private String displayName;

    // Extended fields to fill the form header
    private String firstName;
    private String secondName;
    private String firstSurname;
    private String secondSurname;
    private String birthDate; // yyyy-MM-dd
    private Integer age;
    private String gender;
    private String nationality;
    private String completeAddress;
    private String eps;
    private String afp;
    private String phone1;
    private String phone2;
    private String email;
}

