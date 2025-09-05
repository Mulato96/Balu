package com.gal.afiliaciones.application.service.novelty;

import com.gal.afiliaciones.domain.model.noveltyruaf.NoveltyRuaf;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.utils.ByteArrayToMultipartFile;



public interface NoveltyRuafService {

    NoveltyRuaf createNovelty(NoveltyRuafDTO noveltyRuafDTO);
    Boolean executeWorkerRetirement();
    String generateFileRNRA();
    ByteArrayToMultipartFile retryGeneratingFileRNRE();

}
