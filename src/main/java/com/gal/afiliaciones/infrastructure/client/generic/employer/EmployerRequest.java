package com.gal.afiliaciones.infrastructure.client.generic.employer;

import lombok.Data;

@Data
public class EmployerRequest {
    private String idTipoDoc;
    private String idEmpresa;
    private Integer subempresa;
    private String dvEmpresa;
    private String razonSocial;
    private Integer idDepartamento;
    private Integer idMunicipio;
    private Long idActEconomica;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String faxEmpresa;
    private String emailEmpresa;
    private String indZona;
    private String transporteTrabajadores;
    private String fechaRadicacion;
    private Integer indAmbitoEmpresa;
    private Integer estado;
    private String idTipoDocRepLegal;
    private String idRepresentanteLegal;
    private String representanteLegal;
    private String fechaAfiliacionEfectiva;
    private Integer origenEmpresa;
    private Integer idArp;
    private String idTipoDocArl;
    private String nitArl;
    private String fechaNotificacion;
}
