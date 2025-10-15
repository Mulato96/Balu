package com.gal.afiliaciones.infrastructure.dao.repository.ibc;

import com.gal.afiliaciones.domain.model.ibc.IBCDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBCDetailRepository extends JpaRepository<IBCDetail, Long> {
}
