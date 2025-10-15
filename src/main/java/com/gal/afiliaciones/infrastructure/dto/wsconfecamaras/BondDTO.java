package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BondDTO {

    private Long id;
    
    @JsonProperty("clase_identificacion")
    private String clase_identificacion;
    
    @JsonProperty("numero_identificacion")
    private String numero_identificacion;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("tipo_vinculo")
    private String tipo_vinculo;
    
    @JsonProperty("companyRecord")
    private CompanyRecordDTO companyRecord;
}
