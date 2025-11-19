package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class  ManagementAffiliationDTO {

    private Long id;
    private String field;
    private String dateRequest;
    private String numberDocument;
    private String nameOrSocialReason;
    private String typeAffiliation;
    private String stageManagement;
    private String dateInterview;
    private String assignedTo;
    private String dateRegularization;
    private String asignadoA;
    @JsonIgnore
    private Boolean cancelled;
    private Long idAffiliate;

}
