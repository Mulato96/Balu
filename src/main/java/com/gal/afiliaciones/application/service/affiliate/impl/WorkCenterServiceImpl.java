package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.WorkCenterSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkCenterServiceImpl implements WorkCenterService {

    private final WorkCenterRepository repository;

    @Override
    public List<WorkCenter> getAllWorkCenters() {
        return repository.findAll();
    }

    @Override
    public WorkCenter getWorkCenterByCode(String workCenterCode) {
        return repository.findByCode(workCenterCode);
    }

    @Override
    public WorkCenter saveWorkCenter(WorkCenter workCenter){
        return repository.save(workCenter);
    }

    @Override
    public WorkCenter getWorkCenterById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work center not found"));
    }

    @Override
    public WorkCenter getWorkCenterByCodeAndIdUser(String codeEconomicActivity, UserMain user) {
        return repository.findOne(WorkCenterSpecification.findByUserMainAndCode(codeEconomicActivity, user.getId())).orElse(null);
    }

    @Override
    public Long getNumberCode(UserMain userMain) {

        try {

            return repository.findAll(WorkCenterSpecification.findByUserMain( userMain.getId()))
                    .stream()
                    .map(main -> Long.parseLong(main.getCode()))
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .orElse(0L);

        }catch (Exception e){
            throw new AffiliationError("Ocurrio un error");
        }

    }

    @Override
    public List<WorkCenter> getWorkCenterByMainOffice(MainOffice idMainOffice) {
        return repository.findByMainOffice(idMainOffice);
    }

    @Override
    public WorkCenter getWorkCenterByEconomicActivityAndMainOffice(String economicActivity, Long idMainOffice){
        return repository.findByeconomicActivityCodeAndMainOffice(economicActivity, idMainOffice);
    }

    @Override
    public List<WorkCenter> getWorkCenterActiveByMainOffice(Long idMainOffice) {
        return repository.findWorkCenterActiveByMainOffice(idMainOffice);
    }

}