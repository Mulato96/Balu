package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoCarguesMasivosRepository extends JpaRepository<HistoricoCarguesMasivos, Long> {

    List<HistoricoCarguesMasivos> findByEmpleador_IdAffiliate(Long empleadorId);

}