package com.gal.afiliaciones.infrastructure.dao.repository.arl;

import com.gal.afiliaciones.domain.model.ArlInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArlInformationRepository extends JpaRepository<ArlInformation, Long> {
}
