package com.gal.afiliaciones.infrastructure.dao.repository.tmp;

import com.gal.afiliaciones.domain.model.ExcelDependentTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExcelDependentTmpRepository extends JpaRepository<ExcelDependentTmp, Long>, JpaSpecificationExecutor<ExcelDependentTmp> {
    List<ExcelDependentTmp> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    List<ExcelDependentTmp> findByDocumentTypeAndDocumentNumberAndEmployerDocumentTypeAndEmployerDocumentNumber(
            String documentType, String documentNumber, String employerDocumentType, String employerDocumentNumber
    );
}


