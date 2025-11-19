package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkerCoverageDateResponseDTO {

    private boolean success;
    private String message;
    private String workerType;
    private LocalDate previousDate;
    private LocalDate newDate;
    private String filedNumber;
}