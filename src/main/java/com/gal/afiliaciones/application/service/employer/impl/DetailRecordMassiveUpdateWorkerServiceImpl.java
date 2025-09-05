package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.DetailRecordMassiveUpdateWorkerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordMassiveUpdateWorkerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetailRecordMassiveUpdateWorkerServiceImpl implements DetailRecordMassiveUpdateWorkerService {

    private final DetailRecordMassiveUpdateWorkerRepository recordLoadBulkRepository;

    @Override
    public List<DetailRecordMassiveUpdateWorker> findByIdRecordLoadBulk(Long id) {

        Specification<DetailRecordMassiveUpdateWorker> spec = RecordMassiveUpdateWorkerSpecification.findByIdRecordLoadBulk(id);
        return recordLoadBulkRepository.findAll(spec);

    }

    @Override
    public void saveDetail(DetailRecordMassiveUpdateWorker recordLoadBulk) {
        recordLoadBulkRepository.save(recordLoadBulk);
    }
}
