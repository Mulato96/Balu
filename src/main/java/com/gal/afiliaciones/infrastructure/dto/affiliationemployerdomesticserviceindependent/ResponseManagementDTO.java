package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record ResponseManagementDTO(Page<ManagementAffiliationDTO> data, Long totalInterviewing, Long totalSignature, Long totalDocumentalRevision, Long totalRegularization, Long totalScheduling) {
}
