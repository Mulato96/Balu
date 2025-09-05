package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataEmployerDTO {

    private String identificationTypeEmployer;
    private String identificationNumberEmployer;
    private Integer dv;
    private String completeNameOrCompanyName;
    private String emailEmployer;
    private String affiliationSubtype;
    private String filedNumber;

}
