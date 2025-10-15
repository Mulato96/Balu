package com.gal.afiliaciones.infrastructure.dto.telemetry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositivaLogExportDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String targetPath;
    private String targetMethod;
    private String targetUrl;
    private String requestBody;
    private String targetQuery;
    private String responseBody;
    private Integer responseStatus;
}

