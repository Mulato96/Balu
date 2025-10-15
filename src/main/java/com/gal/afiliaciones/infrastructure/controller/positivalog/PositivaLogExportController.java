package com.gal.afiliaciones.infrastructure.controller.positivalog;

import com.gal.afiliaciones.infrastructure.dao.repository.telemetry.HttpOutboundCallRepository;
import com.gal.afiliaciones.infrastructure.dto.telemetry.PositivaLogExportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/positiva-logs")
@Tag(name = "Positiva Log Export", description = "Export Positiva integration logs")
@RequiredArgsConstructor
public class PositivaLogExportController {

    private final HttpOutboundCallRepository httpOutboundCallRepository;
    
    @Value("${keycloak.target-url}")
    private String keycloakTargetUrl;
    
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export Positiva logs to XLSX", description = "Exports Positiva integration logs within a date range as Excel file")
    public ResponseEntity<byte[]> exportLogs(
            @Parameter(description = "Start date (format: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-01-01T00:00:00", required = true)
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-12-31T23:59:59", required = true)
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String targetHost = extractHost(keycloakTargetUrl);
        log.info("Exporting Positiva logs from {} to {} for host {}", startDate, endDate, targetHost);

        List<PositivaLogExportDTO> logs = httpOutboundCallRepository.findPositivaLogsByDateRange(
                targetHost, startDate, endDate);
        log.info("Found {} logs to export", logs.size());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Positiva Logs");
            
            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Created At", "Target Path", "Target Method", "Response Status",
                               "Target URL", "Request Body", "Target Query", "Response Body"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            int rowNum = 1;
            for (PositivaLogExportDTO logEntry : logs) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(logEntry.getId() != null ? logEntry.getId() : 0);
                row.createCell(1).setCellValue(logEntry.getCreatedAt() != null ? logEntry.getCreatedAt().toString() : "");
                row.createCell(2).setCellValue(logEntry.getTargetPath());
                row.createCell(3).setCellValue(logEntry.getTargetMethod());
                row.createCell(4).setCellValue(logEntry.getResponseStatus() != null ? logEntry.getResponseStatus() : 0);
                row.createCell(5).setCellValue(logEntry.getTargetUrl());
                row.createCell(6).setCellValue(logEntry.getRequestBody());
                row.createCell(7).setCellValue(logEntry.getTargetQuery());
                row.createCell(8).setCellValue(logEntry.getResponseBody());
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            byte[] excelBytes = out.toByteArray();
            
            String filename = "positiva_logs_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";
            
            log.info("Successfully generated XLSX file with {} bytes", excelBytes.length);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
                    
        } catch (Exception e) {
            log.error("Error generating XLSX export", e);
            throw new IllegalStateException("Failed to export logs", e);
        }
    }
    
    private String extractHost(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost();
        } catch (Exception e) {
            log.error("Failed to extract host from URL: {}", url, e);
            throw new IllegalStateException("Invalid target URL configuration", e);
        }
    }
}
