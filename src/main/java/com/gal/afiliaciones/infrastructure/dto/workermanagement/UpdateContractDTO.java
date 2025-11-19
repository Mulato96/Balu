package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContractDTO {

    @NotNull(message = "El ID del afiliado es requerido")
    private Long idAffiliate;

    @NotNull(message = "La fecha de inicio del contrato es requerida")
    private LocalDate contractStartDate;

    @NotNull(message = "La fecha de fin del contrato es requerida")
    private LocalDate contractEndDate;

    @NotNull(message = "La duración del contrato es requerida")
    private String contractDuration;

    @NotNull(message = "La fecha de cobertura es requerida")
    private LocalDate coverageDate;

    @NotNull(message = "El valor total del contrato es requerido")
    private BigDecimal contractTotalValue;

    @NotNull(message = "El valor mensual del contrato es requerido")
    private BigDecimal contractMonthlyValue;

    @NotNull(message = "El valor IBC del contrato es requerido")
    private BigDecimal contractIbcValue;

    @NotNull(message = "El usuario es requerido")
    private String user;

}

