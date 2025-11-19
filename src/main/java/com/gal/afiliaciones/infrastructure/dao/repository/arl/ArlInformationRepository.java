package com.gal.afiliaciones.infrastructure.dao.repository.arl;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gal.afiliaciones.domain.model.ArlInformation;

public interface ArlInformationRepository extends JpaRepository<ArlInformation, Long> {
    
    Optional<ArlInformation> findFirstByOrderById();

}
