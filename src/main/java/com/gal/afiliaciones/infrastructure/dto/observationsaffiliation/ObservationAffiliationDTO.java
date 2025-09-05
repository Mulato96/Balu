package com.gal.afiliaciones.infrastructure.dto.observationsaffiliation;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObservationAffiliationDTO {

    private Long id;
    private String filedNumber;
    private String observations;
    private String date;
    private String reasonReject;
    private String nameOfficial;
    private String state;
}
