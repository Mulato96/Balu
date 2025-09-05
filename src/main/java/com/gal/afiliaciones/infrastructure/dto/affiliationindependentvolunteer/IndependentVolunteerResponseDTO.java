package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import com.gal.afiliaciones.domain.model.FamilyMember;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndependentVolunteerResponseDTO {

    private Long id;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private DataIndependentVolunteerResponseDTO dataIndependent;
    private FamilyMember dataFamilyMember;
    private DataOccupationVolunteerResponseDTO dataOccupation;
    private Danger dataDanger;
    private DataContributionVolunteerDTO dataContribution;
    private String filedNumber;
    private String stageManagement;
    private String affiliationSubType;

}
