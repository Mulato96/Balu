package com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateContributorRequest {

    @NotBlank(message = "El número de identificación del aportante es requerido")
    private String employerIdentificationNumber;

    @NotBlank(message = "El tipo de identificación del aportante es requerido")
    private String employerIdentificationType;

    @NotBlank(message = "El número de identificación del trabajador es requerido")
    private String employeeIdentificationNumber;

    @NotBlank(message = "El tipo de identificación del trabajador es requerido")
    private String employeeIdentificationType;
}