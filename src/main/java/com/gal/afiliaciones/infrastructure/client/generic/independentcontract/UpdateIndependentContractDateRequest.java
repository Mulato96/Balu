package com.gal.afiliaciones.infrastructure.client.generic.independentcontract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIndependentContractDateRequest {
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer subempresa;
    private String idTipoDocPers;
    private String idPersona;
    private String fechaInicio;
    private String fechaFin;
    private String prorroga;
    private Integer valorContrato;
}

