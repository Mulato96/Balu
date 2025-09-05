package com.gal.afiliaciones.infrastructure.dao.repository.arl;

import com.gal.afiliaciones.domain.model.ArlInformation;

import java.util.List;

public interface ArlInformationDao {

    List<ArlInformation> findAllArlInformation();

}
