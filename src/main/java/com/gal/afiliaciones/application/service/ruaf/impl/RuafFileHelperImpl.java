package com.gal.afiliaciones.application.service.ruaf.impl;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.ruaf.RuafFilesHelper;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesRepository;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuafFileHelperImpl implements RuafFilesHelper {

    private final RuafFilesRepository ruafFilesRepository;
    private final ArlInformationRepository arlInformationRepository;

    private final AlfrescoService alfrescoService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String INIT_FILENAME = "RUA";
    private static final String INFORMATION_SOURCE = "250";

    private static final String FILE_EXTENSION = ".txt";

    public void saveRuafFile(String fileName, String idAlfresco, boolean isSuccessful, RuafTypes reportType) {
        ruafFilesRepository.save(RuafFiles.builder()
                .fileName(fileName)
                .idAlfresco(idAlfresco)
                .isSuccessful(isSuccessful)
                .reportType(reportType)
                .build());
    }

    @Override
    public String uploadAlfrescoFile(String fileName, MultipartFile file, String idFolderAlfresco) throws IOException {
        List<MultipartFile> documentList = new ArrayList<>();
        documentList.add(file);
        ResponseUploadOrReplaceFilesDTO responseAlfresco = alfrescoService
                .uploadOrReplaceFiles(idFolderAlfresco,
                        fileName.replace(FILE_EXTENSION, ""), documentList);
        return responseAlfresco.getDocuments().get(0).getDocumentId();
    }

    @Override
    public String buildFileName(String fileType) {
        return INIT_FILENAME + INFORMATION_SOURCE + fileType
                + formatter.format(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))) + "NI000860011153CO014-23.txt";
    }

    @Override
    public String buildName(String firstName, String secondName, String lastName, String secondLastName) {
        return firstName
                .concat(secondName != null && !secondName.isEmpty() ? " " + secondName : "")
                .concat(lastName != null && !lastName.isEmpty() ? " " + lastName : "")
                .concat(secondLastName != null && !secondLastName.isEmpty() ? " " + secondLastName : "").toUpperCase();
    }

    @Override
    public ArlInformation findArlInformation() {
        return arlInformationRepository.findFirstByOrderById().orElse(null);
    }

    @Override
    public RuafFiles findById(Long id) {
        return ruafFilesRepository.findById(id).orElse(null);
    }


}
