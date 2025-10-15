package com.gal.afiliaciones.infrastructure.dao.repository.personasPositiva;

import com.gal.afiliaciones.domain.model.PersonaPositivaTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaPositivaTmpRepository
        extends JpaRepository<PersonaPositivaTmp, Long> {
    List<PersonaPositivaTmp> findByEmployerDocumentNumberAndEmployerDocumentType(String documentNumber, String documentType);
}