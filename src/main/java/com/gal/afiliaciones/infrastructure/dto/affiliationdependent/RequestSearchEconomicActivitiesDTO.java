package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSearchEconomicActivitiesDTO {

    private String documentType;
    private String documentNumber;
    private String affiliationSubtype;
    private Long idHeadquarter;

}
