package com.gal.afiliaciones.infrastructure.dto.employerreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployerReportResponseDTO {

    private String identification;
    private String fullName;
    private String occupation;
    private String affiliationType;
    private String affiliationDate;
    private String affiliationStatus;
    private String noveltyType;
}