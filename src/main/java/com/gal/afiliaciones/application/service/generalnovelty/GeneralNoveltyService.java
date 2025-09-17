package com.gal.afiliaciones.application.service.generalnovelty;

import java.util.List;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.GeneralNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.NoveltyContributorResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;

public interface GeneralNoveltyService {
    void saveGeneralNovelty(SaveGeneralNoveltyRequest request);

    List<GeneralNoveltyDTO> getGeneralNoveltiesByAffiliate(Long idAffiliate);

    PaymentsContributorsResponseDTO getPaymentsContributorsByFilter(PaymentsContributorsRequestDTO request);

    List<NoveltyContributorResponseDTO> getGeneralNoveltiesByContributorDocument(String contributorIdentificationType,
            String contributorIdentification);

    ExportDocumentsDTO exportNoveltiesByContributorDocument(String contributorIdentificationType,
            String contributorIdentification, String exportType);

    ExportDocumentsDTO exportNoveltiesByWorkerByIdAffiliate(Long idAffiliate, String exportType);
}
