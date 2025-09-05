package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkplaceDTO {

    private String codeWorkplace;
    private String codeActivityEconomic;
    private String nameActivityEconomic;
    private String numberWorkers;
    private String classRisk;
    private Long department;
    private Long municipality;
    private String zoneLocationWorkplace;
    private String responsibleWorkplace;
    private String typeDocumentIdentification;
    private String numberDocument;
    private String emailResponsibleWorkplace;
    private Long natureEmployer;
    private Long typeContributor;
    private LocalDate dateRequestAffiliation;
    private LocalDate dateAffiliated;
    private LocalDate dateStartCoverage;
    private String stateAffiliation;
    private String policy;
}
