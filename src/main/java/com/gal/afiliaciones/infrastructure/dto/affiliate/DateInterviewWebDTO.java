package com.gal.afiliaciones.infrastructure.dto.affiliate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateInterviewWebDTO {

    @JsonIgnore
    private Boolean onlyAuthorized;
    @JsonIgnore
    private Long idOfficial;
    @JsonIgnore
    private Long idRoom;
    private String name;
    private String surname;
    private LocalDate day;
    private LocalTime hourStart;
    private LocalTime hourEnd;
    private String idAffiliate;
}
