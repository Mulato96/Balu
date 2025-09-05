package com.gal.afiliaciones.infrastructure.dto.consecutive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsecutiveRequestDTO {

    private String prefix;
    private Long processId;

}
