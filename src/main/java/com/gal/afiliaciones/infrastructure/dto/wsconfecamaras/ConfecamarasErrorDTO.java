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
public class ConfecamarasErrorDTO {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("message")
    private String message;
}
