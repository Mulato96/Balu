package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdetail.AffiliateBasicInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<MainOfficeGrillaDTO> getAllMainOfficesOptimized(Long idAffiliate, String companyName, Pageable pageable);
    Page<MainOfficeGrillaDTO> getAllMainOfficesByAffiliateAndFilters(Long idAffiliate, Long department, Long city, String companyName, Pageable pageable);
    Page<MainOfficeGrillaDTO> findByDocumentWithFilters(
            String type,
            String number,
            Long department,
            Long city,
            Pageable pageable
    );
    AffiliateBasicInfoDTO getAffiliateBasicInfo(String documentType, String documentNumber);
}