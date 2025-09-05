package com.gal.afiliaciones.infrastructure.dto.sat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerTransferResponseDTO {

    private String result;
    private String message;
    private String code;

}
