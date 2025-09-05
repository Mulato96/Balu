package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.RecordInterviewWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordInterviewWebRepository extends JpaRepository<RecordInterviewWeb, Long>, JpaSpecificationExecutor<RecordInterviewWeb> {
}
