package com.gal.afiliaciones.application.service.generalnovelty.impl;

import com.gal.afiliaciones.application.service.generalnovelty.GeneralNoveltyService;
import com.gal.afiliaciones.config.ex.generalnovelty.GeneralNoveltyException;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.generalnovelty.GeneralNovelty;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.generalNovelty.GeneralNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.config.ex.generalnovelty.PaymentsContributorsNotFoundException;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.GeneralNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.ExportNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.ExportWorkerNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.NoveltyContributorResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PageableDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SortDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import com.gal.afiliaciones.infrastructure.security.KeycloakTokenService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralNoveltyServiceImpl implements GeneralNoveltyService {

    private final GeneralNoveltyRepository generalNoveltyRepository;
    private final AffiliateRepository affiliateRepository;
    private final PermanentNoveltyRepository permanentNoveltyRepository;
    private final WebClient webClient;
    private final KeycloakTokenService keycloakTokenService;
    private final ExcelProcessingServiceData excelProcessingServiceData;

    @Value("${kong.balance.recaudo.url}")
    private String recaudoBaseUrl;

    @Override
    public void saveGeneralNovelty(SaveGeneralNoveltyRequest request) {
        Affiliate affiliate = affiliateRepository.findByIdAffiliate(request.getIdAffiliation())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el afiliado con id: " + request.getIdAffiliation()));

        String filedNumber = request.getFiledNumber();
        Long requestChannelId = affiliate.getRequestChannel();

        if (filedNumber == null || filedNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("El radicado no puede ser vacío o nulo");
        }


        GeneralNovelty generalNovelty = GeneralNovelty.builder()
                .requestChannelId(requestChannelId)
                .filedNumber(filedNumber)
                .affiliationDate(LocalDate.now())
                .noveltyType(request.getNoveltyType())
                .status(request.getStatus())
                .observation(request.getObservation())
                .idAffiliate(request.getIdAffiliation())
                .build();

        generalNoveltyRepository.findByFiledNumber(filedNumber).ifPresentOrElse(existing -> {

            existing.setRequestChannelId(generalNovelty.getRequestChannelId());
            existing.setFiledNumber(generalNovelty.getFiledNumber());
            existing.setAffiliationDate(generalNovelty.getAffiliationDate());
            existing.setNoveltyType(generalNovelty.getNoveltyType());
            existing.setStatus(generalNovelty.getStatus());
            existing.setObservation(generalNovelty.getObservation());
            existing.setIdAffiliate(generalNovelty.getIdAffiliate());
            generalNoveltyRepository.save(existing);
        }, () -> {
            generalNoveltyRepository.save(generalNovelty);
        });
    }


    @Override
    public List<GeneralNoveltyDTO> getGeneralNoveltiesByAffiliate(Long idAffiliate) {
        validateAffiliateId(idAffiliate);

    List<GeneralNoveltyDTO> result = new ArrayList<>();

    List<GeneralNovelty> generalNovelties = generalNoveltyRepository.findAllByIdAffiliate(idAffiliate);
    result.addAll(mapGeneralNovelties(generalNovelties));

    List<PermanentNovelty> permanentNovelties = permanentNoveltyRepository.findAllByIdAffiliate(idAffiliate);
    result.addAll(mapPermanentNovelties(permanentNovelties));

    if (result.isEmpty()) {
            throw GeneralNoveltyException.notFoundByAffiliate(idAffiliate);
    }

    return result;
}

private void validateAffiliateId(Long idAffiliate) {
    if (idAffiliate == null || idAffiliate <= 0) {
            throw GeneralNoveltyException.emptyInput(idAffiliate);
    }
}

private List<GeneralNoveltyDTO> mapGeneralNovelties(List<GeneralNovelty> novelties) {
    return novelties.stream()
                    .map(novelty -> GeneralNoveltyDTO.builder()
                                    .id(novelty.getId())
                                    .requestChannelId(novelty.getRequestChannelId())
                                    .requestChannelName(
                                                    novelty.getRequestChannel() != null
                                                                    ? novelty.getRequestChannel().getName()
                                                                    : null)
                                    .filedNumber(novelty.getFiledNumber())
                                    .affiliationDate(novelty.getAffiliationDate())
                                    .noveltyType(novelty.getNoveltyType())
                                    .status(novelty.getStatus())
                                    .observation(novelty.getObservation())
                                    .idAffiliate(novelty.getIdAffiliate())
                                    .build())
                    .toList();
}

private List<GeneralNoveltyDTO> mapPermanentNovelties(List<PermanentNovelty> novelties) {
        return novelties.stream()
                        .map(pn -> GeneralNoveltyDTO.builder()
                                        .id(pn.getId())
                                        .requestChannelId(pn.getChannel() != null ? pn.getChannel().getId() : null)
                                    .requestChannelName(pn.getChannel() != null ? pn.getChannel().getName() : null)
                                    .filedNumber(pn.getFiledNumber())
                                    .affiliationDate(pn.getRegistryDate() != null ? pn.getRegistryDate().toLocalDate()
                                                    : null)
                                    .noveltyType(pn.getNoveltyType() != null ? pn.getNoveltyType().getDescription()
                                                    : null)
                                    .status(pn.getStatus() != null ? pn.getStatus().getStatus() : null)
                                    .observation(pn.getCausal() != null ? pn.getCausal().getCausal() : null)
                                    .idAffiliate(pn.getIdAffiliate())
                                    .build())
                    .toList();
}

@Override
public PaymentsContributorsResponseDTO getPaymentsContributorsByFilter(PaymentsContributorsRequestDTO request) {
    try {
        PaymentsContributorsResponseDTO response = makePaymentsRequest(request);

            return response;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al consultar el servicio de recaudo", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar pagos de cotizantes", e);
        }
    }

    private PaymentsContributorsResponseDTO makePaymentsRequest(PaymentsContributorsRequestDTO request) {
        String url = recaudoBaseUrl + "general-consult-contributor/get-payments-contributors-by-filter";
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + keycloakTokenService.getAccessToken())
                .body(Mono.just(request), PaymentsContributorsRequestDTO.class)
                .retrieve()
                .bodyToMono(PaymentsContributorsResponseDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.empty())
                .block();
    }

    @Override
public List<NoveltyContributorResponseDTO> getGeneralNoveltiesByContributorDocument(
        String contributorIdentificationType, String contributorIdentification) {
    List<PermanentNovelty> novelties = findNovelties(contributorIdentificationType, contributorIdentification);
    validateNovelties(novelties, contributorIdentification);
    return mapNovelties(novelties);
}

private List<PermanentNovelty> findNovelties(String contributorIdentificationType, String contributorIdentification) {
    return permanentNoveltyRepository
            .findAllByContributorIdentificationTypeAndContributorIdentification(
                    contributorIdentificationType, contributorIdentification);
}

private void validateNovelties(List<PermanentNovelty> novelties, String contributorIdentification) {
    if (novelties.isEmpty()) {
        throw GeneralNoveltyException.notFoundByEmployer(contributorIdentification);
    }
}

private List<NoveltyContributorResponseDTO> mapNovelties(List<PermanentNovelty> novelties) {
    return novelties.stream()
            .map(novelty -> NoveltyContributorResponseDTO.builder()
                    .channelName(novelty.getChannel() != null ? novelty.getChannel().getName() : null)
                    .receivedDate(novelty.getRegistryDate())
                    .contributantIdentification(
                            novelty.getContributantIdentificationType() + " " + novelty.getContributantIdentification())
                    .contributantFullName(String.format("%s %s %s %s",
                            novelty.getContributantFirstName() != null ? novelty.getContributantFirstName() : "",
                            novelty.getContributantSecondName() != null ? novelty.getContributantSecondName() : "",
                            novelty.getContributantSurname() != null ? novelty.getContributantSurname() : "",
                            novelty.getContributantSecondSurname() != null ? novelty.getContributantSecondSurname()
                                    : "")
                            .trim())
                    .noveltyType(novelty.getNoveltyType() != null ? novelty.getNoveltyType().getDescription() : null)
                    .status(novelty.getStatus() != null ? novelty.getStatus().getStatus() : null)
                    .causal(novelty.getCausal() != null ? novelty.getCausal().getCausal() : null)
                    .build())
            .collect(Collectors.toList());
}


@Override
public ExportDocumentsDTO exportNoveltiesByContributorDocument(
        String contributorIdentificationType, String contributorIdentification, String exportType) {

    // Obtenemos primero las novedades usando el método existente
    List<NoveltyContributorResponseDTO> novelties = getGeneralNoveltiesByContributorDocument(
            contributorIdentificationType, contributorIdentification);

    // Convertir las novedades al formato de exportación
    List<ExportNoveltyDTO> noveltiesForExport = processNoveltiesForExport(novelties);

    // Exportar usando el servicio de procesamiento de Excel
    return excelProcessingServiceData.exportDataGrid(RequestExportDTO.builder()
            .data(noveltiesForExport)
            .format(exportType)
            .prefixNameFile("novedades_" + contributorIdentificationType + "_" + contributorIdentification)
            .build());

}

private List<ExportNoveltyDTO> processNoveltiesForExport(List<NoveltyContributorResponseDTO> novelties) {
    return novelties.stream()
            .map(novelty -> ExportNoveltyDTO.builder()
                    .canalRadicacion(novelty.getChannelName())
                    .fechaRecibido(novelty.getReceivedDate() != null
                            ? novelty.getReceivedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                            : "")
                    .identificacionCotizante(novelty.getContributantIdentification())
                    .nombreCotizante(novelty.getContributantFullName())
                    .tipoNovedad(novelty.getNoveltyType())
                    .estado(novelty.getStatus())
                    .causal(novelty.getCausal())
                    .build())
            .collect(Collectors.toList());
}

@Override
public ExportDocumentsDTO exportNoveltiesByWorkerByIdAffiliate(Long idAffiliate, String exportType) {
    // Obtenemos primero las novedades usando el método existente
    List<GeneralNoveltyDTO> novelties = getGeneralNoveltiesByAffiliate(idAffiliate);

    // Convertimos al DTO de exportación requerido por la grilla
    List<ExportWorkerNoveltyDTO> noveltiesForExport = processWorkerNoveltiesForExport(novelties);

    // Exportar usando el servicio de procesamiento de Excel
    return excelProcessingServiceData.exportDataGrid(RequestExportDTO.builder()
            .data(noveltiesForExport)
            .format(exportType)
            .prefixNameFile("novedades_" + idAffiliate)
            .build());
}

private List<ExportWorkerNoveltyDTO> processWorkerNoveltiesForExport(List<GeneralNoveltyDTO> novelties) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    return novelties.stream()
            .map(novelty -> ExportWorkerNoveltyDTO.builder()
                    .canalRadicacion(novelty.getRequestChannelName())
                    .radicado(novelty.getFiledNumber())
                    .fechaRadicacion(novelty.getAffiliationDate() != null
                            ? novelty.getAffiliationDate().format(dateFormatter)
                            : "")
                    .tipoNovedad(novelty.getNoveltyType())
                    .estado(novelty.getStatus())
                    .observacion(novelty.getObservation())
                    .build())
            .collect(Collectors.toList());
}

}

