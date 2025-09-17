package com.gal.afiliaciones.infrastructure.dao.repository.decree1563;

import com.gal.afiliaciones.domain.model.OccupationDecree1563;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OccupationDecree1563Repository extends JpaRepository<OccupationDecree1563, Long> {

    Optional<OccupationDecree1563> findByCode(Long code);
    Optional<OccupationDecree1563> findByOccupation(String occupation);

}
