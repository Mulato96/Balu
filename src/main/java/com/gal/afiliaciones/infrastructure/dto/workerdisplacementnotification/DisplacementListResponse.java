package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for displacement listing with relationship status")
public class DisplacementListResponse {

    @Schema(description = "Worker header info repeated across displacements")
    private WorkerSummaryDTO worker;

    @Schema(description = "Indicates if worker has multiple active relationships with employer", example = "true")
    private boolean hasMultipleActiveRelationships;

    @Schema(description = "Number of active relationships found", example = "2")
    private int activeRelationshipsCount;

    @Schema(description = "List of displacement notifications for the worker")
    private List<DisplacementNotificationDTO> displacements;

    @Schema(description = "Total number of displacements found", example = "5")
    private int totalDisplacements;

    @Schema(description = "Number of active displacements", example = "2")
    private int activeDisplacements;

    @Schema(description = "Number of inactive displacements", example = "3")
    private int inactiveDisplacements;

    @Schema(description = "Response message", example = "Desplazamientos obtenidos exitosamente")
    private String message;

    // Pagination metadata
    @Schema(description = "Current page number", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "10")
    private Integer size;

    @Schema(description = "Total pages", example = "3")
    private Integer totalPages;

    @Schema(description = "Sort by field", example = "displacementStartDate")
    private String sortBy;

    @Schema(description = "Sort order", example = "DESC")
    private String sortOrder;
}
