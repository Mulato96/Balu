package com.gal.afiliaciones.application.service.notification;

import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;

import java.util.List;

public interface RegistryConnectInterviewWebService {

    void save(RegistryConnectInterviewWeb registryConnectInterviewWeb);
    RegistryConnectInterviewWeb findById(Long id);
    List<RegistryConnectInterviewWeb> findByFiledNumber(String filedNumber);
    List<RegistryConnectInterviewWeb> findAll();
    void deleteByFiledNumber(String filedNumber);

}
