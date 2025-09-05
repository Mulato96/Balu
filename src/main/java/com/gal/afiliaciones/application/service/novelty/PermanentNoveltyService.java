package com.gal.afiliaciones.application.service.novelty;

import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.CreatePermanentNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyGeneralDataDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.RequestApplyNoveltyDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PermanentNoveltyService {

    PermanentNovelty createPermanentNovelty(CreatePermanentNoveltyDTO dto);
    List<TypeOfUpdate> getNoveltyTypes();
    List<NoveltyStatus> getNoveltyStatus();
    Page<NoveltyGeneralDataDTO> getConsultByFilter(FilterConsultNoveltyDTO filter);
    NoveltyDetailDTO getNoveltyDetail(Long id);
    Boolean applyOrNotApplyNovelty(RequestApplyNoveltyDTO request);
    ExportDocumentsDTO export(String exportType, FilterConsultNoveltyDTO filter);

}
