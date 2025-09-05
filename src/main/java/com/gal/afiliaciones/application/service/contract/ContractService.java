package com.gal.afiliaciones.application.service.contract;

import com.gal.afiliaciones.infrastructure.dto.contract.ContractEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractFilterDTO;

import java.util.List;

public interface ContractService {

    List<ContractEmployerResponseDTO> findContractsByEmployer(ContractFilterDTO filters);
    Object getStep1Pila(String filedNumber);

}
