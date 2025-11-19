package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent;

import java.util.List;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;

public interface BulkLoadingHelp {

    void affiliateData(List<DataExcelDependentDTO> dataDependent,
                       List<DataExcelIndependentDTO> dataIndependent,
                       String type,
                       Affiliate affiliate,
                       Long idUser,
                       Long idRecordLoadBulk);
}
