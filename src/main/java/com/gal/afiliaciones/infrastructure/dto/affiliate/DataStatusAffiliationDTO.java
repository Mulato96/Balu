package com.gal.afiliaciones.infrastructure.dto.affiliate;

import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStatusAffiliationDTO {

    private String affiliationStatus;
    private String stageManagement;
    private String filedNumber;
    private String dateRequest;
    private LocalDateTime dateAffiliateSuspend;
    private String typeAffiliation;
    private String affiliationSubType;
    private LocalDateTime dateInterview;
    private DataDailyDTO dataDailyDTO;
    private String nameOfficial;
    private LocalDateTime dateRegularization;
    private Long idAffiliate;

}
