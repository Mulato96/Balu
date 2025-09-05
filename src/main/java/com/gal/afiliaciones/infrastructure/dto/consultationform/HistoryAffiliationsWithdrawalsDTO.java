package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryAffiliationsWithdrawalsDTO {

    private String channel;
    private String updateDate;
    private String affiliationWithdrawal;
    private String employerDocumentNumber;
    private String employerName;
    private String typeOfLinkage;
    private String affiliationStatus;
    private String filedNumber;
}
