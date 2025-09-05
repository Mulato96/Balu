package com.gal.afiliaciones.application.service.contractextension;

import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionRequest;

public interface ContractExtensionService {
    ContractExtensionInfoDTO getInfoContract(String filedNumber);

    String saveExtensionContract(ContractExtensionRequest request);
}
