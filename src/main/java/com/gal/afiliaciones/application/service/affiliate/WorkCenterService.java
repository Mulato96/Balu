package com.gal.afiliaciones.application.service.affiliate;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;

import java.util.List;

public interface WorkCenterService {
    List<WorkCenter> getAllWorkCenters();
    WorkCenter getWorkCenterByCode(String workCenterCode);
    WorkCenter saveWorkCenter(WorkCenter workCenter);
    WorkCenter getWorkCenterById(Long id);
    WorkCenter getWorkCenterByCodeAndIdUser(String codeEconomicActivity, UserMain user);
    Long getNumberCode(UserMain userMain);

}