package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErgonomicDanger {

    private Boolean isStatic;
    private Boolean isDynamic;
    private Boolean isInappropriateWorkPlans;
    private Boolean isInappropriateWorkspaces;

}
