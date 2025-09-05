package com.gal.afiliaciones.infrastructure.dto.card;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseGrillaCardsDTO {

    private Long id;
    private String employer;
    private LocalDate dateAffiliate;
    private String dateDisaffiliate;
    private String affiliationStatus;
}
