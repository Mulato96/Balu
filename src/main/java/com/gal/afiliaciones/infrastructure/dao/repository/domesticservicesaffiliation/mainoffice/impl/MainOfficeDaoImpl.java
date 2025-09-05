package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.mainoffice.impl;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.mainoffice.MainOfficeDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MainOfficeDaoImpl implements MainOfficeDao {

    private final MainOfficeRepository mainOfficeRepository;

    @Override
    public MainOffice findMainOfficeById(Long id) {
        return mainOfficeRepository.findById(id).orElse(null);
    }
}