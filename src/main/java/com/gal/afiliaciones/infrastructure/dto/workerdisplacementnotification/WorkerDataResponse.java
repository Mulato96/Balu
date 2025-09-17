package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de datos del trabajador y conteos de desplazamientos")
public class WorkerDataResponse {

    @Schema(description = "Información del trabajador")
    private WorkerSummaryDTO worker;

    @Schema(description = "Indica si el trabajador tiene múltiples relaciones activas con el empleador", example = "false")
    private boolean hasMultipleActiveRelationships;

    @Schema(description = "Número de relaciones activas", example = "1")
    private int activeRelationshipsCount;

    @Schema(description = "Total de desplazamientos", example = "5")
    private int totalDisplacements;

    @Schema(description = "Total de desplazamientos activos", example = "5")
    private int activeDisplacements;

    @Schema(description = "Total de desplazamientos inactivos", example = "0")
    private int inactiveDisplacements;

    @Schema(description = "Mensaje de respuesta")
    private String message;
}
