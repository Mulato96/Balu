package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.QrDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IQrRepository extends JpaRepository<QrDocument, UUID> {

    List<QrDocument> findByIdentificationNumberAndIssueDate(String identificationNumber, LocalDate issueDate);
}
