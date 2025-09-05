package com.gal.afiliaciones.infrastructure.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ByteArrayToMultipartFile implements MultipartFile {

    private final byte[] bytes;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayToMultipartFile(byte[] bytes, String fileName, String originalFileName, String contentType) {
        this.bytes = bytes;
        this.name = fileName;
        this.originalFilename = originalFileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public boolean isEmpty() { return bytes == null || bytes.length == 0; }

    @Override
    public long getSize() { return bytes.length; }

    @Override
    public byte[] getBytes() { return bytes; }

    @Override
    public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }

    @Override
    public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), bytes); }
}
