package com.gal.afiliaciones.infrastructure.dto.afpeps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundArlDTO {

    private Long id;
    private String codeARL;
    private String administrator;
}
