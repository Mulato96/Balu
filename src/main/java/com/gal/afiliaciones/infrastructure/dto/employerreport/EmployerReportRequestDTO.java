package com.gal.afiliaciones.infrastructure.dto.employerreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerReportRequestDTO {
    private String documentNumber;

    private String startDate;
    private String endDate;
    private String reportType;
    private String affiliationType ;

    private int page;
    private int size;
    private String fileExportType;
}