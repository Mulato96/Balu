package com.gal.afiliaciones.infrastructure.dao.repository.form;

import com.gal.afiliaciones.domain.model.ApplicationForm;

public interface ApplicationFormDao {

    String findLastConsecutiveForm(String prefix);
    ApplicationForm saveFormRegistry(ApplicationForm form);

}
