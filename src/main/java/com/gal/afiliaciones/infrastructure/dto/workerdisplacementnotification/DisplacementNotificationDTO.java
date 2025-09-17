package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for displacement notification display and operations")
public class DisplacementNotificationDTO {

    @Schema(description = "Displacement notification ID", example = "1")
    private Long id;

    @Schema(description = "Filed number (radicado)", example = "SND-2024-000001")
    private String filedNumber;

    // ===== WORKER INFORMATION =====
    
    @Schema(description = "Worker document type", example = "CC")
    private String workerDocumentType;

    @Schema(description = "Worker document number", example = "1234567890")
    private String workerDocumentNumber;

    @Schema(description = "Worker full name", example = "Juan Carlos Pérez González")
    private String workerFullName;

    @Schema(description = "Worker affiliation type", example = "Trabajador Dependiente")
    private String workerAffiliationType;

    @Schema(description = "Worker job position (cargo)", example = "Analista")
    private String workerJobPosition;

    // ===== EMPLOYER INFORMATION =====
    
    @Schema(description = "Employer document type", example = "NI")
    private String employerDocumentType;

    @Schema(description = "Employer document number (NIT)", example = "860011153")
    private String employerDocumentNumber;

    @Schema(description = "Contractor company name (razón social)", example = "EMPRESA EJEMPLO S.A.S.")
    private String contractorCompanyName;

    // ===== DISPLACEMENT INFORMATION =====
    
    @NotNull(message = "Fecha de inicio de desplazamiento es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement start date", example = "2024-07-01")
    private LocalDate displacementStartDate;

    @NotNull(message = "Fecha de fin de desplazamiento es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Displacement end date", example = "2024-07-05")
    private LocalDate displacementEndDate;

    @Schema(description = "Displacement department code", example = "11")
    private String displacementDepartmentCode;

    @Schema(description = "Displacement department name", example = "Bogotá D.C.")
    private String displacementDepartmentName;

    @Schema(description = "Displacement department id", example = "11")
    private Long displacementDepartmentId;

    @Schema(description = "Displacement municipality code", example = "001")
    private String displacementMunicipalityCode;

    @Schema(description = "Displacement municipality name", example = "Bogotá D.C.")
    private String displacementMunicipalityName;

    @Schema(description = "Displacement municipality id", example = "11001")
    private Long displacementMunicipalityId;

    @NotBlank(message = "Motivo de desplazamiento es requerido")
    @Schema(description = "Displacement reason", example = "DESPLAZAMIENTO POR PROYECTO")
    private String displacementReason;

    @Schema(description = "Displacement status", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    private String status;

    @Schema(description = "Lifecycle status (visibility)", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    private String lifecycleStatus;

    // ===== BUSINESS LOGIC FLAGS =====
    
    @Schema(description = "Indicates if displacement can be edited", example = "true")
    private Boolean canBeEdited;

    @Schema(description = "Indicates if displacement can be inactivated", example = "true")
    private Boolean canBeInactivated;

    // ===== AUDIT INFORMATION =====
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date", example = "2024-01-15 10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "User who created the record", example = "1")
    private Long createdByUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update date", example = "2024-01-15 14:45:00")
    private LocalDateTime updatedDate;

    @Schema(description = "User who last updated the record", example = "1")
    private Long updatedByUserId;

    // ===== HELPER METHODS =====

    /**
     * Get full worker name for display
     */
    public String getWorkerDisplayName() {
        return String.format("%s - %s (%s)", 
                workerDocumentType, workerDocumentNumber, workerFullName);
    }

    /**
     * Get full employer name for display
     */
    public String getEmployerDisplayName() {
        return String.format("%s - %s (%s)", 
                employerDocumentType, employerDocumentNumber, contractorCompanyName);
    }

    /**
     * Get displacement location for display
     */
    public String getDisplacementLocation() {
        return String.format("%s - %s", 
                displacementDepartmentName, displacementMunicipalityName);
    }

    /**
     * Get date range for display
     */
    public String getDateRange() {
        return String.format("%s al %s", 
                displacementStartDate, displacementEndDate);
    }
}
