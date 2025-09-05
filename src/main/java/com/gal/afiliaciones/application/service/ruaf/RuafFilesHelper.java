package com.gal.afiliaciones.application.service.ruaf;

import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RuafFilesHelper {

    void saveRuafFile(String fileName, String idAlfresco, boolean isSuccessful, RuafTypes reportType);
    String uploadAlfrescoFile(String fileName, MultipartFile file, String idFolderAlfresco) throws IOException;
    String buildFileName(String fileType);
    String buildName(String firstName, String secondName, String lastName, String secondLastName);
    ArlInformation findArlInformation();
    RuafFiles findById(Long id);

}
