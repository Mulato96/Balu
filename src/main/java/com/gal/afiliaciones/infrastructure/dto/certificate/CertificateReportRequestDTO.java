package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateReportRequestDTO {
    private String reportName;
    private String idReport;
    private Map<String, Object> parameters;
}

