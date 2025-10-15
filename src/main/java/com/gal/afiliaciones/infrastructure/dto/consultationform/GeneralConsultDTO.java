package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralConsultDTO {

    private String nameOrBusinessName;
    private String affiliationType;
    private String idAffiliate;

}
