package com.gal.afiliaciones.infrastructure.dto.noveltyruaf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoveltyRuafDTO {

    private String arlCode;
    private String identificationType;
    private String identificationNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String noveltyCode;
    private String identificationTypeContributor;
    private String identificationNumberContributor;
    private Integer dvContributor;
    private LocalDate disassociationDateWithContributor;
    private LocalDate noveltyDate;
    private Integer retirmentCausal;
    private LocalDate pensionRecognitionDate;
    private LocalDate deathDate;
    private Long idAffiliate;

}
