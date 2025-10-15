package com.gal.afiliaciones.infrastructure.dto.employer;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerEmployeeListResponseDTO {

    private List<EmployerEmployeeDTO> employees;
    private Integer totalCount;
    private String message;
    private Boolean success;

    public EmployerEmployeeListResponseDTO(List<EmployerEmployeeDTO> employees) {
        this.employees = employees;
        this.totalCount = employees != null ? employees.size() : 0;
        this.success = true;
        this.message = "Consulta exitosa";
    }
}
