package com.gal.afiliaciones.infrastructure.dto.independentcontractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndependentContractorDTO {

    private Long id;

    private String description;

}
