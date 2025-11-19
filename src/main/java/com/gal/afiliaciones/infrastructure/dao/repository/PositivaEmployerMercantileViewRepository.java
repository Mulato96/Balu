package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.PositivaEmployerMercantileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PositivaEmployerMercantileViewRepository extends
        JpaRepository<PositivaEmployerMercantileView, Long>,
        JpaSpecificationExecutor<PositivaEmployerMercantileView> {
}


