package com.gal.afiliaciones.application.service.employerreport;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportResponseDTO;

import java.util.List;

public interface EmployerReportService {

    List<EmployerReportResponseDTO>  findByFilter(EmployerReportRequestDTO employerReportRequestDTO);

    ExportDocumentsDTO exportDataGrid(EmployerReportRequestDTO employerReportRequestDTO);
}