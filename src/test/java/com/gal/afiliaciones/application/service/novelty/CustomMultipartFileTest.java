package com.gal.afiliaciones.application.service.novelty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class CustomMultipartFileTest {

    private File tempFile;
    private File tempOutputFile;
    private final String testContent = "Sample file content for testing.";
    private final String contentType = "text/plain";

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file with known content
        tempFile = File.createTempFile("testFile", ".txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(testContent.getBytes());
        }
        // Create a temporary file for transferTo test
        tempOutputFile = File.createTempFile("outputFile", ".txt");
        // Delete its content so we can test transferTo (optional)
        Files.deleteIfExists(tempOutputFile.toPath());
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        if (tempOutputFile != null && tempOutputFile.exists()) {
            tempOutputFile.delete();
        }
    }

    @Test
    void testGetNameAndOriginalFilename() throws IOException {
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        assertEquals(tempFile.getName(), multipartFile.getName());
        assertEquals(tempFile.getName(), multipartFile.getOriginalFilename());
    }

    @Test
    void testGetContentType() throws IOException {
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        assertEquals(contentType, multipartFile.getContentType());
    }

    @Test
    void testIsEmpty() throws IOException {
        // Non-empty file should not be empty.
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        assertFalse(multipartFile.isEmpty());

        // Create an actual empty file to test empty condition.
        File emptyFile = File.createTempFile("empty", ".txt");
        try {
            MultipartFile emptyMultipartFile = new CustomMultipartFile(emptyFile, "text/plain");
            assertTrue(emptyMultipartFile.isEmpty());
        } finally {
            emptyFile.delete();
        }
    }

    @Test
    void testGetSizeAndGetBytes() throws IOException {
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        byte[] bytes = multipartFile.getBytes();
        assertEquals(testContent.getBytes().length, multipartFile.getSize());
        assertArrayEquals(testContent.getBytes(), bytes);
    }

    @Test
    void testGetInputStream() throws IOException {
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        try (InputStream is = multipartFile.getInputStream()) {
            byte[] streamBytes = is.readAllBytes();
            assertArrayEquals(testContent.getBytes(), streamBytes);
        }
    }

    @Test
    void testTransferTo() throws IOException {
        MultipartFile multipartFile = new CustomMultipartFile(tempFile, contentType);
        // transfer the file content to a new destination file
        File destFile = File.createTempFile("destFile", ".txt");
        try {
            multipartFile.transferTo(destFile);
            byte[] destBytes = Files.readAllBytes(destFile.toPath());
            assertArrayEquals(testContent.getBytes(), destBytes);
        } finally {
            destFile.delete();
        }
    }
}