package com.gal.afiliaciones.infrastructure.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Base64ToMultipartFile implements MultipartFile {

    private final byte[] fileContent;
    private final String fileName;

    public Base64ToMultipartFile(String base64, String fileName) {
        this.fileContent = Base64.getDecoder().decode(cleanBase64String(base64));
        this.fileName = fileName;
    }
    
    /**
     * Limpia y valida la cadena Base64 removiendo prefijos y caracteres inválidos
     */
    private String cleanBase64String(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 string cannot be null or empty");
        }
        
        String cleaned = base64.trim();
        
        // Remover prefijo data URL si existe (ej: data:application/pdf;base64,)
        if (cleaned.contains(",")) {
            int commaIndex = cleaned.indexOf(",");
            cleaned = cleaned.substring(commaIndex + 1);
        }
        
        // Remover espacios en blanco y saltos de línea
        cleaned = cleaned.replaceAll("\\s+", "");
        
        // Verificar que la cadena no esté vacía después de la limpieza
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Base64 string is empty after cleaning");
        }
        
        // Validar que solo contenga caracteres válidos de Base64
        if (!cleaned.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            throw new IllegalArgumentException("Invalid Base64 string format");
        }
        
        // Validar que la longitud sea múltiplo de 4 (requerimiento de Base64)
        if (cleaned.length() % 4 != 0) {
            throw new IllegalArgumentException("Invalid Base64 string length - must be multiple of 4");
        }
        
        return cleaned;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }
}