package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.WorkModality;

import java.util.List;

public interface WorkModalityDao {

    List<WorkModality> findAll();

}
