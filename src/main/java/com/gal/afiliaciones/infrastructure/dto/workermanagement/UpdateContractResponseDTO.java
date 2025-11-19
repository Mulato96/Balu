package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContractResponseDTO {

    private Boolean success;
    private String message;
    private String filedNumber;

}

