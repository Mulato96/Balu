package com.gal.afiliaciones.infrastructure.dto.novelty;

import com.gal.afiliaciones.infrastructure.client.generic.BaseResponseDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDisplacementNotificationRequest extends BaseResponseDTO {
    private Integer idTipoVinculacion;
    private String fechaInicioDesp; // yyyy-MM-dd
    private String fechaFinDesp;    // yyyy-MM-dd
    private Integer codigoDepartamento;
    private Integer codigoMunicipio;
    private String motivoDesp;
}


