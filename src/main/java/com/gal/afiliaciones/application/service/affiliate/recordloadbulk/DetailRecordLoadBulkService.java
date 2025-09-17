package com.gal.afiliaciones.application.service.affiliate.recordloadbulk;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;

import java.util.List;

public interface DetailRecordLoadBulkService {

    List<DetailRecordLoadBulk> findByIdRecordLoadBulk(Long id);
    void saveDetail(List<DetailRecordLoadBulk> recordLoadBulk);
}
