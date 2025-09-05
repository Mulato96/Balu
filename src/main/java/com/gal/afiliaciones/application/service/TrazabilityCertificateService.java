package com.gal.afiliaciones.application.service;


import org.springframework.stereotype.Service;



@Service
public interface TrazabilityCertificateService {

    void recordAction(String certificateId, String userId, String action);
}