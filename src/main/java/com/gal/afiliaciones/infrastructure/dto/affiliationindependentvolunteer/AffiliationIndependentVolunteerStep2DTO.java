package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationIndependentVolunteerStep2DTO {

    private Long idAffiliation;
    private PhysicalDanger physicalDanger;
    private ChemistDanger chemistDanger;
    private BiologicsDanger biologicsDanger;
    private ErgonomicDanger ergonomicDanger;
    private SecurityDanger securityDanger;
    private NaturalPhenomenaDanger naturalPhenomenaDanger;
    private PsychosocialDanger psychosocialDanger;

}
