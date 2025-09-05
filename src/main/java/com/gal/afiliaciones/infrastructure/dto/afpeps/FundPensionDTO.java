package com.gal.afiliaciones.infrastructure.dto.afpeps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundPensionDTO {

    private Integer idAfp;
    private String nameAfp;
    private Long codeAfp;
}
