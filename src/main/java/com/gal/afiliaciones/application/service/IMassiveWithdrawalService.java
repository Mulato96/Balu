package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMassiveWithdrawalService {

    Resource downloadTemplate();

    void uploadFile(MultipartFile file, Long employerId);

    List<HistoricoCarguesMasivos> getHistory(Long employerId);
}