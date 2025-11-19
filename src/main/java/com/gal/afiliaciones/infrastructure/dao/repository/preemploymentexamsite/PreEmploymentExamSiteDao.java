package com.gal.afiliaciones.infrastructure.dao.repository.preemploymentexamsite;

import java.util.List;

import com.gal.afiliaciones.domain.model.PreEmploymentExamSite;
import com.gal.afiliaciones.infrastructure.dto.preemploymentexamsite.CreatePreEmploymentExamSiteRequest;

public interface PreEmploymentExamSiteDao {

    PreEmploymentExamSite createPreEmploymentExamSite(CreatePreEmploymentExamSiteRequest request);
    List<PreEmploymentExamSite> findByNameFilter(String name);
    PreEmploymentExamSite updatePreEmploymentExamSite(Long id, CreatePreEmploymentExamSiteRequest request);
    PreEmploymentExamSite findById(Long id);
    void deletePreEmploymentExamSite(PreEmploymentExamSite site);
    List<PreEmploymentExamSite> findAll();
    List<PreEmploymentExamSite> findByMunicipalityIds(List<Long> municipalityIds);

}
