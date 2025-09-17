package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerCertificateRequestDTO {

    private String identificationDocumentTypeEmployer;
    private String identificationDocumentNumberEmployer;
    private String affiliationTypeEmployer;
    private Long idAffiliateEmployer;

}
