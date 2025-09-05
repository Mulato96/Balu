package com.gal.afiliaciones.infrastructure.dao.repository.Certificate;

import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String>, JpaSpecificationExecutor<Certificate> {

    Certificate findByNumberDocumentAndValidatorCode(String numberDocument, String validatorCode);
    List<Certificate> findByTypeDocumentAndNumberDocument(String typeDocument, String numberDocument);
    Certificate findByValidatorCode(String validatorCode);
    Optional<Certificate> findByNumberDocumentAndTypeDocument(String numberDocument, String typeDocument);

    @Query("SELECT a FROM Certificate a WHERE a.filedNumber = :filedNumber and a.company = :company")
    Optional<Certificate> findByFiledNumberAndCompany(String filedNumber, String company);

}