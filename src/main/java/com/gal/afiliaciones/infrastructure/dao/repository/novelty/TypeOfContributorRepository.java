package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.TypeOfContributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TypeOfContributorRepository extends JpaRepository<TypeOfContributor, Long> {

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TypeOfContributor t WHERE t.code = :code")
    boolean existByCode(@Param("code") Short code);

    Optional<TypeOfContributor> findByCode(Short code);

}
