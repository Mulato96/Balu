package com.gal.afiliaciones.infrastructure.dto.card;

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
public class GeneratePdfCard {

    private String nameUser;
    private String nit;
    private String nameARL;
    private String identification;
    private String companyName;
    private String membershipDate;
    private String contractType;
    private String phoneARL;
    private String emailARL;
    private String addressARL;
    private String webpageARL;
}
