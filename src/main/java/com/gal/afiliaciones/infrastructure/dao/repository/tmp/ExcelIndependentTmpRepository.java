package com.gal.afiliaciones.infrastructure.dao.repository.tmp;

import com.gal.afiliaciones.domain.model.ExcelIndependentTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExcelIndependentTmpRepository extends JpaRepository<ExcelIndependentTmp, Long>, JpaSpecificationExecutor<ExcelIndependentTmp> {
    List<ExcelIndependentTmp> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    List<ExcelIndependentTmp> findByDocumentTypeAndDocumentNumberAndContractorDocumentTypeAndContractorDocumentNumber(
            String documentType, String documentNumber, String contractorDocumentType, String contractorDocumentNumber
    );
}


