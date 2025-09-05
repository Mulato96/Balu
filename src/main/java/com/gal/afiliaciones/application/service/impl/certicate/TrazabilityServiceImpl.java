package com.gal.afiliaciones.application.service.impl.certicate;

import com.gal.afiliaciones.application.service.TrazabilityCertificateService;
import com.gal.afiliaciones.domain.model.affiliate.TrazabilityCerticate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.TrazabilityCertificateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrazabilityServiceImpl implements TrazabilityCertificateService {

    private final TrazabilityCertificateRepository trazabilityRepository;

    @Override
    public void recordAction(String  certificateId, String userId, String action) {
        TrazabilityCerticate trazability = new TrazabilityCerticate();
        trazability.setCertificateId(certificateId);
        trazability.setUserId(userId);
        trazability.setActionDate(LocalDateTime.now());
        trazability.setAction(action);

        trazabilityRepository.save(trazability);
    }


}
