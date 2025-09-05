package com.gal.afiliaciones.application.service.preemploymentexamsite;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.dto.MessageResponse;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.PreEmploymentExamSiteDTO;

import java.util.List;

public interface PreEmploymentExamSiteService {

    PreEmploymentExamSite createPreEmploymentExamSite(CreatePreEmploymentExamSiteRequest request);
    List<PreEmploymentExamSiteDTO> getPreEmploymentExamSitesByFilter(String name);
    PreEmploymentExamSite updatePreEmploymentExamSite(Long id, CreatePreEmploymentExamSiteRequest request);
    MessageResponse deleteupdatePreEmploymentExamSite(Long id);
    List<PreEmploymentExamSiteDTO> getPreEmploymentExamSitesByCity(String city);
    PreEmploymentExamSite getPreEmploymentExamSitesById(Long id);

}
