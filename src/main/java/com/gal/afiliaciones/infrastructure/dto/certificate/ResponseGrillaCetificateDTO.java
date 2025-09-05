package com.gal.afiliaciones.infrastructure.dto.certificate;

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
public class ResponseGrillaCetificateDTO {

    private String id;
    private String employer;
    private LocalDate dateAffiliate;
    private String dateDisaffiliate;
    private String affiliationStatus;
}
