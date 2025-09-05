package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.employer.RecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.RecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordMassiveUpdateWorkerSpecification;
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
public class RecordMassiveUpdateWorkerServiceImpl implements RecordMassiveUpdateWorkerService {

    private final RecordMassiveUpdateWorkerRepository repository;
    private final DetailRecordMassiveUpdateWorkerService service;
    private final ExcelProcessingServiceData excelProcessingServiceData;

    @Override
    public RecordMassiveUpdateWorker save(RecordMassiveUpdateWorker recordLoadBulk) {
        return repository.save(recordLoadBulk);
    }

    @Override
    public List<RecordMassiveUpdateWorker> findAllByIdUser(Long idUser) {
        Specification<RecordMassiveUpdateWorker> spec = RecordMassiveUpdateWorkerSpecification.findByIdUser(idUser);
        return repository.findAll(spec);
    }

    @Override
    public Optional<RecordMassiveUpdateWorker> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ExportDocumentsDTO createDocument(Long id) {

        RecordMassiveUpdateWorker recordLoadBulk = repository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el registro"));
        ExportDocumentsDTO exportDocumentsDTO;

        if(Boolean.FALSE.equals(recordLoadBulk.getState())){

            List<ErrorFileExcelDTO> dataDetail = service.findByIdRecordLoadBulk(recordLoadBulk.getId()).stream().map(data -> {
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
