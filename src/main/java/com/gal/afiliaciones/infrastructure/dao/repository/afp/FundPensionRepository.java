package com.gal.afiliaciones.infrastructure.dao.repository.afp;

import com.gal.afiliaciones.domain.model.FundPension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundPensionRepository extends JpaRepository<FundPension, Long> {

    Optional<FundPension> findByNameAfp(String nameAfp);

}
