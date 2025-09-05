package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MandatoryDanger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MandatoryDangerRepository extends JpaRepository<MandatoryDanger, Long> {

    Optional<MandatoryDanger> findByFkOccupationId(Long fkOccupationId);

}
