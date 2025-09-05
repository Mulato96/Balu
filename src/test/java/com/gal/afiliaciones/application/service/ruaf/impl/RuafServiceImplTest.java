package com.gal.afiliaciones.application.service.ruaf.impl;

import java.io.IOException;
import java.util.List;

import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.application.service.ruaf.RmrpRuafFileService;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesRepository;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuafServiceImplTest {

    @Mock private RuafFilesRepository ruafFilesRepository;
    @Mock private AlfrescoService alfrescoService;
    @Mock private RmrpRuafFileService rmrpRuafFileService;
    @Mock private NoveltyRuafService noveltyRuafService;

    @InjectMocks
    private RuafServiceImpl service;

    @Test
    void testGenerateFiles_IOExceptionOnCreate() throws IOException {
        RuafServiceImpl realService = new RuafServiceImpl(rmrpRuafFileService, noveltyRuafService, ruafFilesRepository, alfrescoService);
        RuafServiceImpl spyService = spy(realService);
        doThrow(new IOException()).when(spyService).generateFiles();
        assertThrows(IOException.class, spyService::generateFiles);
    }

    @Test
    void testNotFindData() {
        Page<RuafFiles> emptyPage = Page.empty();

        when(ruafFilesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(emptyPage);

        Page<RuafDTO> result = service.findAll(PageRequest.of(0, 10), null);

        assertTrue(result.isEmpty());
    }
}
