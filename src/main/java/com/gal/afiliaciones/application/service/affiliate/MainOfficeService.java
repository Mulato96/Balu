package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;

import java.util.List;

public interface MainOfficeService {
    List<MainOfficeGrillaDTO> getAllMainOffices(Long idAffiliateEmployer);
    MainOffice getMainOfficeByCode(String officeCode);
    MainOffice saveMainOffice(MainOfficeDTO mainOffice);
    MainOffice saveMainOffice(MainOffice mainOffice);
    MainOffice findById(Long id);
    MainOfficeDTO findId(Long id);
    List<MainOfficeGrillaDTO> findByIdUserAndDepartmentAndCity(Long idUser, Long department, Long city);
    MainOffice update(MainOfficeDTO mainOfficeDTO, Long id);
    String delete(Long id, Long idAffiliateEmployer);
    String findCode();
    Object findAffiliateMercantile(Long idAffiliate);
    List<MainOfficeGrillaDTO> getAllMainOfficesByIdAffiliate(Long idAffiliate);
    List<MainOfficeGrillaDTO> findByNumberAndTypeDocument(String number, String type);
    MainOffice saveMainOfficeOfficial(MainOfficeOfficialDTO mainOfficeOfficialDTO);
    MainOffice updateOfficial(MainOfficeDTO mainOfficeDTO, Long id, String number);
    String deleteOfficial(Long id, String number);
    List<MainOffice> findAll();

}