package com.gal.afiliaciones.infrastructure.dto.certificate;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateReportRequestDTO {
    private String reportName;
    private String idReport;
    private Map<String, Object> parameters;
}

