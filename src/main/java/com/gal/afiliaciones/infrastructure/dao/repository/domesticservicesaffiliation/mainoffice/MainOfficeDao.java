package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.mainoffice;


import com.gal.afiliaciones.domain.model.affiliate.MainOffice;

public interface MainOfficeDao {
    MainOffice findMainOfficeById(Long id);
}