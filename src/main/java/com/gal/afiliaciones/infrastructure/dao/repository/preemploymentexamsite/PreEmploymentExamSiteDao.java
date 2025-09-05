package com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;

import java.util.List;

public interface PreEmploymentExamSiteDao {

    PreEmploymentExamSite createPreEmploymentExamSite(CreatePreEmploymentExamSiteRequest request);
    List<PreEmploymentExamSite> findByNameFilter(String name);
    PreEmploymentExamSite updatePreEmploymentExamSite(Long id, CreatePreEmploymentExamSiteRequest request);
    PreEmploymentExamSite findById(Long id);
    void deletePreEmploymentExamSite(PreEmploymentExamSite site);
    List<PreEmploymentExamSite> findAll();

}
