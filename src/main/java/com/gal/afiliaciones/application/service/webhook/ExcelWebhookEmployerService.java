package com.gal.afiliaciones.application.service.webhook;

import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelWebhookEmployerService {
    
    /**
     * Procesa un archivo Excel y ejecuta la afiliación de empleadores
     * @param file Archivo Excel con los datos de empleadores
     * @return Lista de respuestas del webhook
     */
    List<WebhookEmployerResponseDTO> processExcelFile(MultipartFile file);
    
    /**
     * Procesa un archivo Excel de forma asíncrona
     * @param file Archivo Excel con los datos de empleadores
     * @return Mensaje de confirmación
     */
    String processExcelFileAsync(MultipartFile file);
    
    /**
     * Lee un archivo Excel y lo convierte a lista de WebhookEmployerRequestDTO
     * @param file Archivo Excel
     * @return Lista de requests para el webhook
     */
    List<WebhookEmployerRequestDTO> readExcelFile(MultipartFile file);
    
    /**
     * Valida la estructura del archivo Excel
     * @param file Archivo Excel
     * @return true si la estructura es válida
     */
    boolean validateExcelStructure(MultipartFile file);
} 