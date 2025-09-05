package com.gal.afiliaciones.application.service.employer;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;

import java.util.List;

public interface DetailRecordMassiveUpdateWorkerService {

    List<DetailRecordMassiveUpdateWorker> findByIdRecordLoadBulk(Long id);
    void saveDetail(DetailRecordMassiveUpdateWorker recordLoadBulk);

}
