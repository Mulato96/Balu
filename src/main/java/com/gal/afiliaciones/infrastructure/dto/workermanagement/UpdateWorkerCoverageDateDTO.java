package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkerCoverageDateDTO {

    @NotNull(message = "El ID del afiliado es requerido")
    private Long idAffiliate;

    @NotNull(message = "La nueva fecha de cobertura es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newCoverageDate;

    private String user;

}