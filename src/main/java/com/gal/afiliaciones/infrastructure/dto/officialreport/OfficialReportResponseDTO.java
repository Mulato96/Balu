package com.gal.afiliaciones.infrastructure.dto.officialreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficialReportResponseDTO {

    private String identification;
    private String fullName;
    private String occupation;
    private String age;
    private String affiliationType;
    private String noveltyType;
    private String affiliationStatus;
    private String affiliationDate;
    private String department;
    private String city;
    private String economicActivityCode;
    private String descriptionEconomicActivity;
    private String coverageStartDate;
    private String noveltyDate;
    private String personType;
}