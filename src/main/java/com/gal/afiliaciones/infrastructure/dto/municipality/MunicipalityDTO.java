package com.gal.afiliaciones.infrastructure.dto.municipality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDTO {

    private Long idDepartment;
    private Long idMunicipality;
    private String municipalityName;
    private String divipolaCode;

}
