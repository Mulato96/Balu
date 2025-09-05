package com.gal.afiliaciones.infrastructure.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractEmployerResponseDTO {

    private String company;
    private String startContractDate;
    private String endContractDate;
    private String stageManagement;
    private String status;
    private String filedNumber;
    private String bondingType;
    private Long idAffiliate;
    private String affiliationType;

}
