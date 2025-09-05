package com.gal.afiliaciones.infrastructure.dto.economicactivity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupationDecree1563DTO {

    private Long id;
    private Integer risk;
    private Long code;
    private String occupation;

}
