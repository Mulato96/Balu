package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.infrastructure.dto.ValueContractRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueUserContractDTO;

public interface IValueContratService {

    public ValueUserContractDTO getUserContractInfo(String typeDocument,String numberidentificacion);
    public ValueUserContractDTO saveContract(ValueContractRequestDTO dto);
}
