package com.gal.afiliaciones.infrastructure.dao.repository.form;

import com.gal.afiliaciones.domain.model.ApplicationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {

    @Query("SELECT a.filedNumberDocument FROM ApplicationForm a WHERE SUBSTRING(a.filedNumberDocument,0,13) = :prefix " +
            "ORDER BY a.filedNumberDocument DESC LIMIT 1")
    String findLastConsecutiveForm(String prefix);

}
