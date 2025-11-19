package com.gal.afiliaciones.application.service.employeeupdatemassive;

import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.ProcessSummaryDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IMassiveUpdateService {
    ProcessSummaryDTO processMassiveUpdate(MultipartFile file, String type, String loggedInUserDocument);
}
