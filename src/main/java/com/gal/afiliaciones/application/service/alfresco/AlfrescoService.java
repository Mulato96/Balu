package com.gal.afiliaciones.application.service.alfresco;

import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AlfrescoService {

    AlfrescoUploadResponse uploadFileAlfresco(AlfrescoUploadRequest request) throws IOException;
    AlfrescoUploadResponse createFolder(String idParentNode, String nameFolder);
    Optional<ConsultFiles> getIdDocumentsFolder(String idFolder);
    String getDocument(String idDocument);
    ResponseUploadOrReplaceFilesDTO uploadOrReplaceFiles(String idNode, String nameFolder, List<MultipartFile> files) throws IOException;
    ResponseUploadOrReplaceFilesDTO uploadAffiliationDocuments(String idNode, String nameFolder, List<MultipartFile> files) throws IOException;
}
