package com.gal.afiliaciones.infrastructure.client.generic.headquarters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHeadquartersRequest {
    private String tipoDocEmp;
    private String numeDocEmp;
    private Integer subempresa;      // Decentralized consecutive (defaults to 0)
    private Integer idDepartamento;
    private Integer idMunicipio;
    private Integer idActEconomica;
    private Integer principal;
    private String fechaRadicacion;
    private String nombre;
    private String direccion;
    private String zona;
    private String telefono;
    private String email;
    private String tipoDocResp;
    private String numeDocResp;
    private Integer sedeMision;
    private String tipoDocEmpMision;
    private String numeDocEmpMision;
}

