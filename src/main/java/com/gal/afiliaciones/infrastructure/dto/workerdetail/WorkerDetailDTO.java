package com.gal.afiliaciones.infrastructure.dto.workerdetail;


import com.gal.afiliaciones.infrastructure.dto.affiliationdetail.AffiliationDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.PolicyDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerDetailDTO {
    private AffiliationDetailDTO contract;
    private PolicyDTO policy;
    private List<DocumentsDTO> documents;
}
