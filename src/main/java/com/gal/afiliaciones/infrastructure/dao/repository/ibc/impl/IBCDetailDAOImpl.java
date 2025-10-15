package com.gal.afiliaciones.infrastructure.dao.repository.ibc.impl;

import com.gal.afiliaciones.domain.model.ibc.IBCDetail;
import com.gal.afiliaciones.infrastructure.dao.repository.ibc.IBCDetailDAO;
import com.gal.afiliaciones.infrastructure.dao.repository.ibc.IBCDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IBCDetailDAOImpl implements IBCDetailDAO {

    private final IBCDetailRepository ibcDetailRepository;

    @Override
    public IBCDetail createOrUpdateIBCDetail(IBCDetail ibcDetail) {
        return ibcDetailRepository.save(ibcDetail);
    }

    @Override
    public Optional<IBCDetail> findById(Long id) {
        return ibcDetailRepository.findById(id);
    }

}
