package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;

import java.util.List;

public interface MainOfficeService {
    List<MainOfficeGrillaDTO> getAllMainOffices(Long idUser);
    MainOffice getMainOfficeByCode(String officeCode);
    MainOffice saveMainOffice(MainOfficeDTO mainOffice);
    MainOffice saveMainOffice(MainOffice mainOffice);
    MainOffice findById(Long id);
    MainOfficeDTO findId(Long id);
    List<MainOfficeGrillaDTO> findByIdUserAndDepartmentAndCity(Long idUser, Long department, Long city);
    MainOffice update(MainOfficeDTO mainOfficeDTO, Long id, String filedNumber);
    String delete(Long id, String filedNumber);
    String findCode();
    Object findAffiliateMercantile(Long idAffiliate);

}