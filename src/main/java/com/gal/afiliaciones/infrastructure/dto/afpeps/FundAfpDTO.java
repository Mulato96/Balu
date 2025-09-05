package com.gal.afiliaciones.infrastructure.dto.afpeps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundAfpDTO {

    private Integer idAfp;
    private String nameAfp;
    private Long codeAfp;
}
