package com.gal.afiliaciones.infrastructure.client.generic.workcenter;

import lombok.Data;

@Data
public class WorkCenterRequest {

    private String tipoDocEmp;
    private String numeDocEmp;
    private Integer subempresa;
    private Integer idSucursal;
    private Integer idSede;
    private Long idActEconomica;
    private Integer principal;
    private Integer indTipoCentro;
    private Integer numeroTrab;

}
