package com.gal.afiliaciones.infrastructure.dao.repository.arl;

import com.gal.afiliaciones.domain.model.Arl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArlRepository extends JpaRepository<Arl, Long> {
    Optional<Arl> findByCodeARL(String codeARL);
}
