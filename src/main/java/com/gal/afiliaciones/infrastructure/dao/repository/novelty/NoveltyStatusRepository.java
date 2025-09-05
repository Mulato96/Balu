package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoveltyStatusRepository extends JpaRepository<NoveltyStatus, Long> {

    Optional<NoveltyStatus> findByStatus(String status);

}
