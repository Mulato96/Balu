package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMassiveWithdrawalService {

    Resource downloadTemplate();

    com.gal.afiliaciones.infrastructure.controller.massive_withdrawal.dto.UploadResponseDTO uploadFile(MultipartFile file, Long employerId);

    List<HistoricoCarguesMasivos> getHistory(Long employerId);
}