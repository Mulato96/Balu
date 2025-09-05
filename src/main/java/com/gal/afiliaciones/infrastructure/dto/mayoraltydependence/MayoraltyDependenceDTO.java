package com.gal.afiliaciones.infrastructure.dto.mayoraltydependence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MayoraltyDependenceDTO {

    private String name;
    private String nit;
    private int dv;
    private Long decentralizedConsecutive;

}
