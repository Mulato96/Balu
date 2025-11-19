package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEconomicActivityRequest {
    
    @NotBlank(message = "El tipo de documento de la persona es requerido")
    private String documentType;
    
    @NotBlank(message = "El número de documento de la persona es requerido")
    private String documentNumber;
    
    @NotBlank(message = "El tipo de documento de la empresa es requerido")
    private String documentTypeCompany;
    
    @NotBlank(message = "El NIT de la empresa es requerido")
    private String nitCompany;
    
    @NotBlank(message = "El código de actividad económica es requerido")
    private String economicActivityCode;
}

