package com.gal.afiliaciones.infrastructure.client.generic.workcenter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWorkCenterRequest {
    private String tipoDocEmp;
    private String numeDocEmp;
    private Integer subempresa;
    private Integer idSucursal;
    private Integer idSede;
    private Integer idCentro;
    private Long idActEconomica;
    private Integer principal;
    private Integer indTipoCentro;
    private Integer numeroTrab;
}

