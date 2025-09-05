package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveGeneralNoveltyRequest {
    private Long idAffiliation;
    private String filedNumber;
    private String noveltyType;
    private String status;
    private String observation;
}