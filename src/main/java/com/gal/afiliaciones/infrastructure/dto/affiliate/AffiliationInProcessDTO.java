package com.gal.afiliaciones.infrastructure.dto.affiliate;

import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffiliationInProcessDTO {

    private Long idAffiliate;
    private String filedNumber;
    private String requestDate;
    private String name;
    private String typeAffiliation;
    private String affiliationSubType;
    private String stageManagement;
    private String interviewDate;
    private String affiliationStatus;
    private DataDailyDTO dataDailyDTO;
    private String documentTypeEmployer;
    private String documentNumberEmployer;

}
