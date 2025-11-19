package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProcessSummaryDTO {
    private int totalRegistrosProcesados;
    private int registrosExitosos;
    private List<String> registrosConError;
}
