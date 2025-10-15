package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordResponseDTO {

    @JsonProperty("nit")
    private String nit;
    
    @JsonProperty("dv")
    private String dv;
    
    @JsonProperty("registros")
    private List<CompanyRecordDTO> registros;
    
    @JsonProperty("fecha_respuesta")
    private String fecha_respuesta;
    
    @JsonProperty("hora_respuesta")
    private String hora_respuesta;
    
    @JsonProperty("error")
    private ConfecamarasErrorDTO error;
}