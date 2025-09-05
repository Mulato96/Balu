package com.gal.afiliaciones.infrastructure.dao.repository.legalnature;

import com.gal.afiliaciones.domain.model.legalnature.LegalNature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalNatureRepository extends JpaRepository<LegalNature, Long> {

    LegalNature findByDescription(String description);
}