package com.gal.afiliaciones.application.service.ruaf.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class RuafFilesHelperImplTest {

    @InjectMocks private RuafFileHelperImpl service;

    @Test
    void testBuildFileNamePattern() {
        String ruafFileType = "RMRP";
        String fileName = service.buildFileName(ruafFileType);
        assertNotNull(fileName);
        assertTrue(fileName.matches("RUA250RMRP\\d{8}NI000860011153CO014-23\\.txt"));
    }

}
