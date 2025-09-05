package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.Danger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DangerRepository extends JpaRepository<Danger, Long>, JpaSpecificationExecutor<Danger> {

    Danger findByIdAffiliation(Long idAffiliation);

}
