package com.gal.afiliaciones.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record InfoBasicaDTO(
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

        @JsonProperty("mainStreetTypeId")
        String idCallePrincipal,

        @JsonProperty("mainStreetNumber")
        String numeroCallePrincipal,

        @JsonProperty("mainStreetLetter1")
        String letra1CallePrincipal,

        @JsonProperty("mainStreetLetter2")
        String letra2CallePrincipal,

        @JsonProperty("mainStreetCardinal")
        String puntoCardinalCallePrincipal,

        @JsonProperty("bis")
        Boolean bis,

        @JsonProperty("secondaryNumber1")
        String numero1Secundaria,

        @JsonProperty("secondaryNumber2")
        String numero2Secundaria,

        @JsonProperty("secondaryLetter")
        String letraSecundaria,

        @JsonProperty("secondaryCardinal")
        String puntoCardinal2,

        @JsonProperty("ph1")
        String ph1,

        @JsonProperty("ph1Number")
        String numPh1,

        @JsonProperty("ph2")
        String ph2,

        @JsonProperty("ph2Number")
        String numPh2,

        @JsonProperty("ph3")
        String ph3,

        @JsonProperty("ph3Number")
        String numPh3,

        @JsonProperty("ph4")
        String ph4,

        @JsonProperty("ph4Number")
        String numPh4,

        @JsonProperty("fullAddress")
        String direccionTexto,

        @JsonProperty("jobPositionId")
        Integer idCargo,

        @JsonProperty("changeDate")
        LocalDate fechaNovedad,

        @JsonProperty("comments")
        String observaciones,

        @JsonProperty("nationalityName")
        String nacionalidadNombre,

        @JsonProperty("pensionFundName")
        String afpNombre,

        @JsonProperty("healthProviderName")
        String epsNombre,

        @JsonProperty("cityName")
        String ciudadNombre,

        @JsonProperty("departmentName")
        String departamentoNombre
) {}
