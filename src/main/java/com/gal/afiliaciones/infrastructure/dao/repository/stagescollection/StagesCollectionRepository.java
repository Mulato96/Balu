package com.gal.afiliaciones.infrastructure.dao.repository.stagescollection;

import com.gal.afiliaciones.domain.model.StagesCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StagesCollectionRepository extends JpaRepository<StagesCollection, Long> {

    List<StagesCollection> findByContributorIdentificationTypeAndContributorIdentificationNumber(String identificationType, String identificationNumber);

}
