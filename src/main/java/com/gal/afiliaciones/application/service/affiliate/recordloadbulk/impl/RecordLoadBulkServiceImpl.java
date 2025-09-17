package com.gal.afiliaciones.application.service.affiliate.recordloadbulk.impl;

import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.RecordLoadBulkRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordLoadBulkSpecification;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordLoadBulkServiceImpl implements RecordLoadBulkService {

    private final RecordLoadBulkRepository recordLoadBulkRepository;
    private final DetailRecordLoadBulkService recordLoadBulkService;
    private final ExcelProcessingServiceData excelProcessingServiceData;

    @Override
    public RecordLoadBulk save(RecordLoadBulk recordLoadBulk) {
        return recordLoadBulkRepository.save(recordLoadBulk);
    }

    @Override
    public List<RecordLoadBulk> findAllByIdUser(Long idUser, Long idAffiliateEmployer) {
        Specification<RecordLoadBulk> spec = RecordLoadBulkSpecification.findByUserAndEmployer(idUser, idAffiliateEmployer);
        return recordLoadBulkRepository.findAll(spec).stream().sorted((o1, o2) -> o2.getDateLoad().compareTo(o1.getDateLoad())).toList();
    }

    @Override
    public Optional<RecordLoadBulk> findById(Long id) {

        return recordLoadBulkRepository.findById(id);
    }

    @Override
    public ExportDocumentsDTO createDocument(Long id) {

        RecordLoadBulk recordLoadBulk = recordLoadBulkRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el registro"));
        ExportDocumentsDTO exportDocumentsDTO;

        if(Boolean.FALSE.equals(recordLoadBulk.getState())){

            List<ErrorFileExcelDTO> dataDetail = recordLoadBulkService.findByIdRecordLoadBulk(recordLoadBulk.getId()).stream().map(data -> {
                ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
                BeanUtils.copyProperties(data, errorFileExcelDTO);
                return errorFileExcelDTO;
            }).toList();

            exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(dataDetail);
        }else{
            exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(List.of(Map.of("Detalle", "No encontraron errores en la carga del documento consultado")));
        }

        exportDocumentsDTO.setNombre(recordLoadBulk.getFileName());

        return exportDocumentsDTO;
    }
}
