package com.gal.afiliaciones.application.service.affiliate.recordloadbulk.impl;

import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.DetailRecordLoadBulkRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RecordLoadBulkSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DetailRecordLoadBulkServiceImpl implements DetailRecordLoadBulkService {

    private final DetailRecordLoadBulkRepository recordLoadBulkRepository;

    @Override
    public List<DetailRecordLoadBulk> findByIdRecordLoadBulk(Long id) {

        Specification<DetailRecordLoadBulk> spec = RecordLoadBulkSpecification.findByIdRecordLoadBulk(id);
        return recordLoadBulkRepository.findAll(spec);

    }

    @Override
    public void saveDetail(DetailRecordLoadBulk recordLoadBulk) {
        recordLoadBulkRepository.save(recordLoadBulk);
    }
}
