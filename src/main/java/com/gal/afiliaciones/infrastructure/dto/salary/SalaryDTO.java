package com.gal.afiliaciones.infrastructure.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDTO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long value;
    private String denomination;
    private Integer numUpdateSmlmv;

}
