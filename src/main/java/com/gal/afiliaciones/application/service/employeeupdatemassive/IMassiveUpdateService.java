package com.gal.afiliaciones.application.service.employeeupdatemassive;

import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.MassiveUpdateResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IMassiveUpdateService {
    MassiveUpdateResponseDTO processMassiveUpdate(MultipartFile file, String type, String loggedInUserDocument);
}
