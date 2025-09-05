package com.gal.afiliaciones.infrastructure.dao.repository;


import com.gal.afiliaciones.domain.model.ContributionCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestCorrectionRepository extends JpaRepository<ContributionCorrection, Long> {

    Optional<ContributionCorrection> findByFiledNumber(String filedNumber);

    @Query("select c from ContributionCorrection c where c.user.id = ?1")
    Optional<ContributionCorrection> findByUser_Id(Long id);
}
