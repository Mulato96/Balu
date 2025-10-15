package com.gal.afiliaciones.domain.model.retirement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetirementResponseDTO {
    private String message;
    private LocalDate retirementDate;
}