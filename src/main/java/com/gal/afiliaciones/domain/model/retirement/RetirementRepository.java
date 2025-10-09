package com.gal.afiliaciones.domain.model.retirement;

import com.gal.afiliaciones.domain.model.Retirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RetirementRepository extends JpaRepository<Retirement, Long> {
    List<Retirement> findAllByRetirementDate(LocalDate retirementDate);
}