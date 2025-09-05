package com.gal.afiliaciones.infrastructure.dao.repository.eps;

import com.gal.afiliaciones.domain.model.Health;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthPromotingEntityRepository extends JpaRepository<Health,Long> {

    Optional<Health> findByNameEPS(String name);
    Optional<Health> findByCodeEPS(String codeEPS);

}
