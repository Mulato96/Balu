package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRelationShipDTO {

    private String documentNumber;

    private String employerName;

    private String typeOfLinkage;

    private String affiliationStatus;

    private String filedNumber;

    private long idAffiliate;
}
