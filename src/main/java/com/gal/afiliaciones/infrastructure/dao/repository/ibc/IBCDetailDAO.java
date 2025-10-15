package com.gal.afiliaciones.infrastructure.dao.repository.ibc;

import com.gal.afiliaciones.domain.model.ibc.IBCDetail;

import java.util.Optional;

public interface IBCDetailDAO {

    IBCDetail createOrUpdateIBCDetail(IBCDetail ibcDetail);

    Optional<IBCDetail> findById(Long id);

}
