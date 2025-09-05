package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PermanentNoveltyDao {

    PermanentNovelty createNovelty(PermanentNovelty novelty);
    Page<PermanentNovelty> findByFilters(FilterConsultNoveltyDTO filter);
    PermanentNovelty findById(Long id);
    List<PermanentNovelty> exportAllData(FilterConsultNoveltyDTO filter);

}
