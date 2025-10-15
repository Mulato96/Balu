package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IRadicadoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RadicadoServiceImpl implements IRadicadoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public String getNextRadicado() {
        Query query = entityManager.createNativeQuery("SELECT nextval('consecutive_worker_displacement_seq')");
        return "RET-" + query.getSingleResult().toString();
    }
}