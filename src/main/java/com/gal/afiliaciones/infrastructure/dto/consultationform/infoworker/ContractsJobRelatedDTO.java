package com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractsJobRelatedDTO {

    private String contractNumber;
    private String validityFrom;
    private String validityTo;
    private boolean contractStatus;

}
