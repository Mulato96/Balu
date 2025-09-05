package com.gal.afiliaciones.application.service.ruaf.impl;

import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.application.service.ruaf.RmrpRuafFileService;
import com.gal.afiliaciones.application.service.ruaf.RuafService;
import com.gal.afiliaciones.config.converters.RuafFilesConverter;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesSpecification;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafFilterDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuafServiceImpl implements RuafService {

    private final RmrpRuafFileService rmrpRuafFileService;
    private final NoveltyRuafService noveltyRuafService;

    private final RuafFilesRepository ruafFilesRepository;

    private final AlfrescoService alfrescoService;

    @Override
    public void generateFiles() throws IOException {
        rmrpRuafFileService.generateRmrpFile();
        noveltyRuafService.generateFileRNRA();
    }

    @Override
    public Page<RuafDTO> findAll(Pageable pageable, RuafFilterDTO filter) {
        String sortBy = filter != null && filter.sortBy() != null && !filter.sortBy().isEmpty() ? filter.sortBy() : "id";
        String sortOrder = filter != null && filter.sortOrder() != null && !filter.sortOrder().isEmpty() ? filter.sortOrder() : Sort.Direction.ASC.name();
        Pageable pageableSorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.fromString(sortOrder), sortBy));

        return ruafFilesRepository.findAll(RuafFilesSpecification.findByFilter(filter), pageableSorted)
                .map(RuafFilesConverter.entityToDto);
    }

    @Override
    public String exportFile(Long id) {
        Optional<RuafFiles> ruafFile = ruafFilesRepository.findById(id);

        if (ruafFile.isPresent() && ruafFile.get().getIsSuccessful())
            return alfrescoService.getDocument(ruafFile.get().getIdAlfresco());

        return "";
    }

    @Override
    public String retryFileGeneration(Long id) {
        return "";
    }
}
