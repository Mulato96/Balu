package com.gal.afiliaciones.infrastructure.dto.updatedatesemployee;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VinculacionQueryDTO {
    private String tipoIdentificacion;
    private String numeroIdentificacion;
}