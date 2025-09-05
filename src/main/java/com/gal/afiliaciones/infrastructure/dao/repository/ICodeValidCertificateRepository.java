package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.CodeValidCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ICodeValidCertificateRepository extends JpaRepository<CodeValidCertificate, Long>, JpaSpecificationExecutor<CodeValidCertificate> {
}
