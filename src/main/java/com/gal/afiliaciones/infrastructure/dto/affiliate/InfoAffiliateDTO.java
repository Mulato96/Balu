package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfoAffiliateDTO {

    private Long idAffiliate;
    private String affiliationStatus;
    private String affiliationType;
    private String affiliationSubType;
    private String filedNumber;
    private String stageManagement;

}
