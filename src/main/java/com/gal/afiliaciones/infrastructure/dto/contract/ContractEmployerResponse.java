package com.gal.afiliaciones.infrastructure.dto.contract;

public interface ContractEmployerResponse {

    String getCompany();
    String getStartContractDate();
    String getEndContractDate();
    String getStageManagement();
    String getStatus();
    String getFiledNumber();
    String getBondingType();
    Long getIdAffiliate();
    String getAffiliationType();

}
