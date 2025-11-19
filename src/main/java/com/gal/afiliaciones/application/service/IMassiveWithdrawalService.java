package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.UploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMassiveWithdrawalService {

    String downloadTemplate();

    UploadResponseDTO uploadFile(MultipartFile file, Long employerId);

    List<HistoricoCarguesMasivos> getHistory(Long employerId);
}