package com.gal.afiliaciones.infrastructure.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEmployerRequestDTO {
    private String idTipoDocEmpresa;
    private String idEmpresa;
    private Integer idSubEmpresa;
} 