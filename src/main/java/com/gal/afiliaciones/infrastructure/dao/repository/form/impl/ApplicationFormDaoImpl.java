package com.gal.afiliaciones.infrastructure.dao.repository.form.impl;

import com.gal.afiliaciones.domain.model.ApplicationForm;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormDao;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplicationFormDaoImpl implements ApplicationFormDao {

    private final ApplicationFormRepository repository;

    @Override
    public String findLastConsecutiveForm(String prefix){
        return repository.findLastConsecutiveForm(prefix);
    }

    @Override
    public ApplicationForm saveFormRegistry(ApplicationForm form){
        return repository.save(form);
    }

}
