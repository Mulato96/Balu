package com.gal.afiliaciones.infrastructure.dto.retirementreason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetirementEmployerDTO {

    private Long idUser;
    private String typeUpdate;
    private Long reasonId;
    private LocalDate dateRetirement;
    private String base64File;
    private String fileName;
    private String typeOfAffiliate;
    private String identification;
    private String identificationType;
}
