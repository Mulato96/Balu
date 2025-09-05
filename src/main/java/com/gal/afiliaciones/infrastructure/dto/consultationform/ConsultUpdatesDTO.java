package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultUpdatesDTO {
    private String channel;
    private String updateDate;
    private String affiliationWithdrawal;
    private String employerDocumentNumber;
    private String employerName;
    private String typeOfLinkage;
    private String affiliationStatus;
    private String filedNumber;
}
