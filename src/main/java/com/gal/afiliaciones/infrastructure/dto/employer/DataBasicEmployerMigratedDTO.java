package com.gal.afiliaciones.infrastructure.dto.employer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataBasicEmployerMigratedDTO {

    private String documentTypeEmployer;
    private String documentNumberEmployer;
    private Integer digitVerificationEmployer;
    private String businessNameEmployer;

}
