package com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryAffiliationsWithdrawalsHistoryDTO {

    private String channel;
    private String FilingDate;
    private Long eps;
    private String ocupation;
    private String address;
}
