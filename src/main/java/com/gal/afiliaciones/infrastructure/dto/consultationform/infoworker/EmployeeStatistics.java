package com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EmployeeStatistics {

    long activeDependentEmployees;
    long activeIndependentEmployees;
    Integer totalActiveEmployees;
    Integer totalInactiveEmployees;
    long totalEmployees;

}
