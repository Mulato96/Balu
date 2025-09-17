package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;

import java.util.List;

public interface BulkLoadingHelp {

    void affiliateData(List<DataExcelDependentDTO> dataDependent, List<DataExcelIndependentDTO> dataIndependent, String type, Affiliate affiliate);
}
