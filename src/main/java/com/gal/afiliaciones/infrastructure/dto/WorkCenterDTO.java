package com.gal.afiliaciones.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCenterDTO {

    private Long id;
    private String code;
    private String economicActivityCode;
    private int totalWorkers;
    private String riskClass;
    private String workCenterDepartment;
    private String workCenterCity;
    private String workCenterZone;
    private Long workCenterManagerId;

}
