package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseConsultWorkerDTO {
    private Long id;
    private String workerDocumentType;
    private String workerDocumentNumber;
    private String transportableIndependentWorker;
    private Long causal;
    private String satWorkerAffiliateArl;
}
