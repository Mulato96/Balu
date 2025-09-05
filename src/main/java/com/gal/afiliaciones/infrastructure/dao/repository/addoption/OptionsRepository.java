package com.gal.afiliaciones.infrastructure.dao.repository.addoption;

import com.gal.afiliaciones.domain.model.Options;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionsRepository extends JpaRepository<Options, Long> {
}
