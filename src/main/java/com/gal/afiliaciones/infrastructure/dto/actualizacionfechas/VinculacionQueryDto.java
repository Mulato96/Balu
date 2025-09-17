package com.gal.afiliaciones.infrastructure.dto.actualizacionfechas;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VinculacionQueryDto {
    private String tipoIdentificacion;
    private String numeroIdentificacion;
}
