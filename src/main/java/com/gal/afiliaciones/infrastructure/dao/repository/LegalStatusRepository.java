package com.gal.afiliaciones.infrastructure.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gal.afiliaciones.domain.model.LegalStatus;
import com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO;

public interface LegalStatusRepository extends JpaRepository<LegalStatus, Long> {
    
    @Query("SELECT new com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO(ls.code, ls.name) FROM LegalStatus ls")
    List<LegalStatusDTO> findAllAsDTO();

}
