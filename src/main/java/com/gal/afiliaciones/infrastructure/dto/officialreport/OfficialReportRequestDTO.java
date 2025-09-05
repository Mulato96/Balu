package com.gal.afiliaciones.infrastructure.dto.officialreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficialReportRequestDTO {
    private String documentNumber;

    private String startDate;
    private String endDate;
    private String reportType;
    private String department;
    private String city;
    private String occupation;
    private String affiliationType;
    private String economicActivity;
    private String noveltyType;

    private int page;
    private int size;
    private String fileExportType;
}