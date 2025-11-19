package com.gal.afiliaciones.infrastructure.client.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseResponseDTO {

    @JsonProperty("tipoDoc")
    private String tipoDoc;

    @JsonProperty("idPersona")
    private String idPersona;

    @JsonProperty("fechaNacimiento")
    private String fechaNacimiento;

    @JsonProperty("idDepartamento")
    private Integer idDepartamento;

    @JsonProperty("idMunicipio")
    private Integer idMunicipio;

    @JsonProperty("nombre1")
    private String nombre1;

    @JsonProperty("nombre2")
    private String nombre2;

    @JsonProperty("apellido1")
    private String apellido1;

    @JsonProperty("apellido2")
    private String apellido2;

    @JsonProperty("emailPersona")
    private String emailPersona;

    @JsonProperty("idOcupacion")
    private Integer idOcupacion;

    @JsonProperty("telefonoPersona")
    private String telefonoPersona;

    @JsonProperty("sexo")
    private String sexo;

    @JsonProperty("fechaInicioVinculacion")
    private String fechaInicioVinculacion;

    @JsonProperty("fechaFinVinculacion")
    private String fechaFinVinculacion;

    @JsonProperty("eps")
    private String eps;

    @JsonProperty("nombreEps")
    private String nombreEps;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("idEmpresa")
    private String idEmpresa;

    @JsonProperty("razonSocial")
    private String razonSocial;

    @JsonProperty("direccionEmpresa")
    private String direccionEmpresa;

    @JsonProperty("telefonoEmpresa")
    private String telefonoEmpresa;

    @JsonProperty("emailEmpresa")
    private String emailEmpresa;

    @JsonProperty("indZona")
    private String indZona;

    @JsonProperty("idActEconomica")
    private Long idActEconomica;

    @JsonProperty("fechaAfiliacionEfectiva")
    private String fechaAfiliacionEfectiva;

    @JsonProperty("estadoEmpresa")
    private String estadoEmpresa;

    @JsonProperty("idSucursal")
    private Integer idSucursal;

    @JsonProperty("idTipoDoc")
    private String idTipoDoc;

    @JsonProperty("idTipoDocEmp")
    private String idTipoDocEmp;
}
