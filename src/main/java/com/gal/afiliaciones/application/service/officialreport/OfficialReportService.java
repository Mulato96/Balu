package com.gal.afiliaciones.application.service.officialreport;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportResponseDTO;

import java.util.List;

public interface OfficialReportService {

    List<OfficialReportResponseDTO>  findByFilter(OfficialReportRequestDTO officialReportRequestDTO);

    ExportDocumentsDTO exportDataGrid(OfficialReportRequestDTO officialReportRequestDTO);

    List<String>  findNoveltyTypeOption();
}