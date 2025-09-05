package com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreEmploymentExamSiteDTO {

    private Long id;
    private String municipalityName;
    private String nameSite;
    private String address;
    private Long phoneNumber;
    private String webSite;
    private String latitude;
    private String longitude;

}
