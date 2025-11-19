package com.gal.afiliaciones.application.service.affiliate.recordloadbulk.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.RecordLoadBulkRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordLoadBulkSpecification;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    @Transactional
    public void updateStatus(Long id, String status) {
        log.info("Iniciando cambio de estado para RecordLoadBulk ID: {} - Nuevo estado: {}", id, status);
        int updatedRows = recordLoadBulkRepository.updateStatusById(id, status);
        if (updatedRows > 0) {
            log.info("Cambio de estado exitoso para RecordLoadBulk ID: {} - Estado actualizado a: {}", id, status);
        } else {
            log.warn("No se pudo actualizar el estado para RecordLoadBulk ID: {} - Registro no encontrado", id);
        }
    }
 
    @Override
    public ExportDocumentsDTO createDocument(Long id) {

        RecordLoadBulk recordLoadBulk = recordLoadBulkRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el registro"));
        ExportDocumentsDTO exportDocumentsDTO = new ExportDocumentsDTO();

        if(Boolean.FALSE.equals(recordLoadBulk.getState())){

            ObjectMapper mapper = new ObjectMapper();
            List<DetailRecordLoadBulk> dataDetailRecord = recordLoadBulkService.findByIdRecordLoadBulk(recordLoadBulk.getId());
            List<DetailRecordLoadBulk> recodOld = dataDetailRecord.stream()
                    .filter(data -> data.getColumn() != null)
                    .toList();

            if(!recodOld.isEmpty()){
                List<ErrorFileExcelDTO> dataDetail = recodOld.stream().map(data -> {
                    ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
                    BeanUtils.copyProperties(data, errorFileExcelDTO);
                    return errorFileExcelDTO;
                }).toList();

                if(!dataDetail.isEmpty())
                    exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(dataDetail);
            }else{
                List<DataExcelDependentDTO> dataDetail = dataDetailRecord
                        .stream()
                        .map(data -> {
                            try {
                                ErrorFileExcelDTO errorFileExcelDTO = new ErrorFileExcelDTO();
                                BeanUtils.copyProperties(data, errorFileExcelDTO);
                                return mapper.readValue(data.getError(), DataExcelDependentDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }).toList();

                if(!dataDetail.isEmpty())
                    exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(dataDetail);
            }

        }else{
            exportDocumentsDTO = excelProcessingServiceData.createDocumentExcelErrors(List.of(Map.of("Detalle", "No encontraron errores en la carga del documento consultado")));
        }

        exportDocumentsDTO.setNombre(recordLoadBulk.getFileName());

        return exportDocumentsDTO;
    }
}
