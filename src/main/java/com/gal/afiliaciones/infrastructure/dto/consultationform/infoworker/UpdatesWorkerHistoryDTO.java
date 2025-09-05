package com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatesWorkerHistoryDTO {

    private String channel;
    private String filingDate;
    private String noveltyType;
    private String noveltyStatus;
    private String retirementNovelty;
    private int quotedDays;
    private String recordNumber;
    private String retirementDate;
    private String affiliationType;
    private String observation;
    private Long user;
}
