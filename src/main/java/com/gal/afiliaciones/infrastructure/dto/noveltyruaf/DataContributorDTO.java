package com.gal.afiliaciones.infrastructure.dto.noveltyruaf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataContributorDTO {

    private String identificationType;
    private String identificationNumber;
    private Integer dv;

}
