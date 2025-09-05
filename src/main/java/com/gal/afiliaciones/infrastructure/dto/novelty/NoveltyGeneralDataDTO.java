package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoveltyGeneralDataDTO {

    private Long id;
    private String channel;
    private LocalDate registryDate;
    private String contributorIdentificationType;
    private String contributorIdentification;
    private String nameOrCompanyName;
    private String contributantIdentificationType;
    private String contributantIdentification;
    private String contributantName;
    private String noveltyType;
    private String status;
    private String causal;

}
