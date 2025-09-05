package com.gal.afiliaciones.infrastructure.dto.afpeps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundEpsDTO {

    private Long id;
    private String codeEPS;
    private String nameEPS;
}
