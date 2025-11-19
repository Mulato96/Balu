package com.gal.afiliaciones.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record UpdateInfoBasicaRequest(
        @JsonProperty("documentType")
        String tipoDocumento,

        @JsonProperty("identificationNumber")
        String numeroIdentificacion,

        @JsonProperty("firstName")
        String primerNombre,

        @JsonProperty("middleName")
        String segundoNombre,

        @JsonProperty("lastName")
        String primerApellido,

        @JsonProperty("secondLastName")
        String segundoApellido,

        @JsonProperty("dateOfBirth")
        LocalDate fechaNacimiento,

        @JsonProperty("age")
        Integer edad,

        @JsonProperty("nationalityId")
        String nacionalidad,

        @JsonProperty("sex")
        String sexo,

        @JsonProperty("pensionFundId")
        String afp,

        @JsonProperty("healthProviderId")
        String eps,

        @JsonProperty("email")
        String email,

        @JsonProperty("phoneNumber1")
        String telefono1,

        @JsonProperty("phoneNumber2")
        String telefono2,

        @JsonProperty("departmentId")
        Integer idDepartamento,

        @JsonProperty("cityId")
        Integer idCiudad,

        @JsonProperty("fullAddress")
        String direccionTexto,


        @JsonProperty("changeDate")
        LocalDate fechaNovedad,

        @JsonProperty("comments")
        String observaciones,


        @JsonProperty("codeWarning")
        int codeWarning,

        @JsonProperty("isRegistry")
        Boolean isRegistry
) {}
