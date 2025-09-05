package com.gal.afiliaciones.infrastructure.dto.workerretirement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRequestRetirementWorkerDTO {
    Long idAffiliation;
    LocalDate dateRetirement;
    String name;
}
