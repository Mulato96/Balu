package com.gal.afiliaciones.infrastructure.client.generic;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.RequestServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ConsultIndependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Query;
import com.gal.afiliaciones.infrastructure.dto.alfresco.SearchRequest;
import com.gal.afiliaciones.infrastructure.dto.alfrescoDTO.AlfrescoResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.ViewingAssociatedDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.AffiliationWorkerIndependentArlDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.EmployerTransferResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.user.RequestUserUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserDTO;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericWebClient {

    private final WebClient webClientBuilder;
    private final CollectProperties properties;
    private final WebClient.Builder webClient;

    public static final String ERROR_MSG = "Error Calling bus API for get user for identification";
    private static final String CONTENT_TYPE_LABEL = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String URL_NODE = "%s/nodes/%s/content";
    private static final String HEADER = "Authorization";
    private static final String TYPE = "Basic ";
    private static final String ERROR_TEXT = "Error: ";
    private static final String URL_CHILDREN = "%s/nodes/%s/children?skipCount=0&maxItems=2000";
    private static final String TELEMETRY_REQUEST_BODY = "telemetryRequestBody";

    public Optional<UserDtoApiRegistry> getByIdentification(String identification) {
        return webClientBuilder.get()
                .uri(String.format(properties.getIdentificationUrl(), identification))
                .retrieve()
                .bodyToMono(UserDtoApiRegistry.class)
                .onErrorResume(Throwable.class, GenericWebClient::handleErrors).blockOptional();
    }

    public String generateReportCertificate(CertificateReportRequestDTO reportRequestDto) {
        return webClientBuilder
                .post()
                .uri(properties.getReportServiceUrl())
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, reportRequestDto)
                .body(Mono.just(reportRequestDto), CertificateReportRequestDTO.class)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(Throwable.class, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        return Mono.just(ERROR_TEXT + " " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                    } else {
                        return Mono.just(ERROR_TEXT + " " + throwable.getMessage());
                    }
                })
                .blockOptional()
                .orElse(ERROR_TEXT + "Report generation failed");
    }

    public BodyResponseConfig<List<MunicipalityDTO>> getMunicipalities() {
        return webClientBuilder
                .get()
                .uri(properties.getMunicipalitiesUrl())
                .retrieve()
                .bodyToMono(BodyResponseConfig.class).block();
    }

    public BodyResponseConfig<List<MunicipalityDTO>> getMunicipalitiesByName(String name) {
        if (name != null && !name.isEmpty()) {
            String url = String.format(properties.getMunicipalitiesByNameUrl(), name);
            return webClientBuilder
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(BodyResponseConfig.class).block();
        }
        return getMunicipalities();
    }

    public static <T> Mono<T> handleErrors(Throwable ex) {
        log.error(ERROR_MSG, ex);
        return Mono.empty();
    }

    public Mono<byte[]> getWithHeadersBytesArray(String url, Map<String, String> headers) {
        WebClient.RequestHeadersSpec<?> request = webClientBuilder
                .get()
                .uri(url);
        headers.forEach(request::header);

        return request.retrieve().bodyToMono(byte[].class);
    }

    public Mono<String> getWithHeaders(String url, Map<String, String> headers) {
        WebClient.RequestHeadersSpec<?> request = webClientBuilder
                .get()
                .uri(url);
        request.headers(httpHeaders -> headers.forEach(httpHeaders::add));
        request.headers(httpHeaders -> httpHeaders.add("accept", CONTENT_TYPE_JSON));

        return request.retrieve().bodyToMono(String.class);
    }

    public BodyResponseConfig<UserDTO> createUser(UserDTO request) {
        return webClientBuilder
                .post()
                .uri(properties.getCreateUserServiceUrl())
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, request)
                .body(Mono.just(request), UserDTO.class)
                .retrieve()
                .bodyToMono(BodyResponseConfig.class)
                .block();
    }

    public ResponseConsultWorkerDTO consultWorkerDTO(ConsultIndependentWorkerDTO request) {
        return webClientBuilder
                .post()
                .uri(properties.getConsultIndependentWorkerUrl())
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, request)
                .body(Mono.just(request), ConsultIndependentWorkerDTO.class)
                .retrieve()
                .bodyToMono(ResponseConsultWorkerDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Log del error o cualquier otra acción necesaria
                    log.error("Error en la llamada WebClient: " + ex.getMessage());
                    return Mono.empty(); // Devuelve un Mono vacío en caso de error
                })
                .block();
    }

    public SalaryDTO getSmlmvByYear(int year) {
        return webClientBuilder
                .get()
                .uri(String.format(properties.getUrlSalary(), year))
                .retrieve()
                .bodyToMono(SalaryDTO.class)
                .onErrorResume(Throwable.class, GenericWebClient::handleErrors).blockOptional()
                .orElseThrow(() -> new NotFoundException("Error consulting the smlmv"));
    }

    public EmployerTransferResponseDTO sendAffiliationIndependentToSat(AffiliationWorkerIndependentArlDTO request) {
        return webClientBuilder
                .post()
                .uri(properties.getAffiliationIndependentSat())
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, request)
                .body(Mono.just(request), AffiliationWorkerIndependentArlDTO.class)
                .retrieve()
                .bodyToMono(EmployerTransferResponseDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Log del error o cualquier otra acción necesaria
                    log.error("Error enviando la afiliacion de independiente a SAT: " + ex.getMessage());
                    return Mono.empty(); // Devuelve un Mono vacío en caso de error
                })
                .block();
    }

    public BodyResponseConfig<List<OccupationDecree1563DTO>> getOccupationsByVolunteer() {
        return webClientBuilder
                .get()
                .uri(properties.getOccupationsDecree1563Url())
                .retrieve()
                .bodyToMono(BodyResponseConfig.class).block();
    }

    public List<Occupation> getAllOccupations() {
        return webClientBuilder
                .get()
                .uri(properties.getAllOccupationsUrl())
                .retrieve()
                .bodyToMono(List.class).block();
    }

    public Mono<String> getFileBase64(String documentId) {
        String url = String.format(URL_NODE, properties.getAlfrescoUrl(), documentId);

        String authStr = properties.getUsernameAlfresco() + ":" + properties.getPasswordAlfresco();
        String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER, TYPE + base64Creds);
        return getWithHeadersBytesArray(url, headers).map(bytes -> Base64.getEncoder().encodeToString(bytes));
    }

    //TODO: No borrar porque se usara cuando se ajusten los servicios de SAT
    /*
    public EmployerTransferResponseDTO sendAffiliationDependentToSat(SendBeginningLaborRelationshipOrTrainingPracticeRequest request) {
        return webClientBuilder.webClient().mutate().build()
                .post()
                .uri(properties.getAffiliationDependentSat())
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .body(Mono.just(request), AffiliationWorkerIndependentArlDTO.class)
                .retrieve()
                .bodyToMono(EmployerTransferResponseDTO.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Log del error o cualquier otra acción necesaria
                    log.error("Error enviando la afiliacion de independiente a SAT: " + ex.getMessage());
                    return Mono.empty(); // Devuelve un Mono vacío en caso de error
                })
                .block();
    }*/

    public Long getProcessId(String typeIdentification, String identification) {
        String url = String.format(properties.getIdProcessFromCollect(), typeIdentification, identification);

        return webClientBuilder
                .get()
                .uri(url)
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .retrieve()
                .bodyToMono(Long.class)
                .onErrorResume(Throwable.class, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.error(ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                    } else {
                        log.error(throwable.getMessage());
                    }
                    return Mono.empty();
                })
                .blockOptional()
                .orElse(null);
    }

    public List<ViewingAssociatedDocumentsDTO> getAllViewingDocuments(Long processId) {
        String url = String.format(properties.getViewAssociatedDocuments(), processId);

        return webClientBuilder
                .get()
                .uri(url)
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .retrieve()
                .bodyToFlux(ViewingAssociatedDocumentsDTO.class)
                .collectList()
                .onErrorResume(Throwable.class, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.error(ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                        return Mono.just(Collections.emptyList());
                    } else {
                        log.error(throwable.getMessage());
                        return Mono.just(Collections.emptyList());
                    }
                })
                .blockOptional()
                .orElse(Collections.emptyList());
    }

    public Optional<String> folderExists(String parentFolderId, String folderName) {
        AlfrescoResponseDTO response = getChildrenNode(parentFolderId);
        if (response != null && response.getList() != null) {
            return response.getList().getEntries().stream()
                    .filter(entry -> entry.getEntry().getName().equals(folderName) && "cm:folder".equals(entry.getEntry().getNodeType()))
                    .map(entry -> entry.getEntry().getId())
                    .findFirst();
        }

        return Optional.empty();
    }

    public AlfrescoResponseDTO getChildrenNode(String folderId) {

        String url = String.format(URL_CHILDREN, properties.getAlfrescoUrl(), folderId);

        String authStr = properties.getUsernameAlfresco() + ":" + properties.getPasswordAlfresco();
        String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER, TYPE + base64Creds);
        Gson gson = new Gson();


        return gson.fromJson(getWithHeaders(url, headers).block(), AlfrescoResponseDTO.class);

    }

    public Mono<String> getProfileImageByUserId(Long userId) {
        return webClientBuilder
                .get()
                .uri(String.format(properties.getProfileImageUser(), userId))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(Throwable.class, GenericWebClient::handleErrors);
    }

    public boolean checkUserArrearsStatus(String identificationNumber, String identificationType) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getUrlCheckUserInArrears())
                .queryParam("typeDocument", identificationType)
                .queryParam("identification", identificationNumber)
                .build()
                .toUri();

        return webClientBuilder
                .get()
                .uri(uri)
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(Throwable.class, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.error(ERROR_TEXT + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                    } else {
                        log.error(ERROR_TEXT + throwable.getMessage());
                    }
                    return Mono.empty();
                })
                .blockOptional().orElse(false);
    }

    public Optional<ExportDocumentsDTO> exportDataGrid(RequestServiceDTO requestServiceDTO) {
        return webClientBuilder.post()
                .uri(properties.getExportDataGrid())
                .attribute(TELEMETRY_REQUEST_BODY, requestServiceDTO)
                .body(Mono.just(requestServiceDTO), RequestServiceDTO.class)
                .retrieve()
                .bodyToMono(ExportDocumentsDTO.class)
                .blockOptional();
    }
    public void assignRolesToUser(Long idUser, List<Long> roleIds) {

        try {
            webClientBuilder.put()
                    .uri(properties.getUrlTransversal()+"user-role/assignment/"+ idUser)
                    .attribute(TELEMETRY_REQUEST_BODY, roleIds)
                    .bodyValue(roleIds)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Roles asignados correctamente al usuario con ID: {}", idUser);
        } catch (Exception e) {
            log.error("Error al asignar roles al usuario {}: {}", idUser, e.getMessage());
        }
    }

    public String generateAffiliateCard(CertificateReportRequestDTO reportRequestDto) {
        return webClientBuilder
                .post()
                .uri(properties.getReportServiceUrl() + "/card")
                
                .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, reportRequestDto)
                .body(Mono.just(reportRequestDto), CertificateReportRequestDTO.class)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(Throwable.class, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        return Mono.just(ERROR_TEXT + " " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                    } else {
                        return Mono.just(ERROR_TEXT + " " + throwable.getMessage());
                    }
                })
                .blockOptional()
                .orElse(ERROR_TEXT + "Report generation failed");
    }

    public void updateUser(RequestUserUpdateDTO request) {
        webClientBuilder
            .post()
            .uri(properties.getUpdateUser() + "/" + request.idUser())
            .header(CONTENT_TYPE_LABEL, CONTENT_TYPE_JSON)
            .attribute(TELEMETRY_REQUEST_BODY, request)
            .body(Mono.just(request), RequestUserUpdateDTO.class)
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(Throwable.class, throwable -> {
                if (throwable instanceof WebClientResponseException ex) {
                    return Mono.just(ERROR_TEXT + " " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                } else {
                    return Mono.just(ERROR_TEXT + " " + throwable.getMessage());
                }
            })
            .blockOptional();
    }


    public Optional<String> folderExistsByName(String parentFolderId, String folderName) {
        AlfrescoResponseDTO response = searchFolderByName(folderName, parentFolderId);
        if (response != null && response.getList() != null) {
            return response.getList().getEntries().stream().filter(entry ->  entry.getEntry().getParentId().equals(parentFolderId))
                    .map(entry -> entry.getEntry().getId())
                    .findFirst();
        }

        return Optional.empty();
    }

    public AlfrescoResponseDTO searchFolderByName(String folderName, String parentNodeId) {
        String url = properties.getAlfrescoBaseUrl();
        String authStr = properties.getUsernameAlfresco() + ":" + properties.getPasswordAlfresco();
        String authHeader = TYPE + Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));;

        String consulta = String.format("TYPE:'cm:folder' AND cm:name:'%s'", folderName);
        SearchRequest request = new SearchRequest(new Query(consulta));

        return webClient.baseUrl(url).build()
                .post()
                .uri("/alfresco/api/-default-/public/search/versions/1/search")
                .header(HEADER, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .attribute(TELEMETRY_REQUEST_BODY, request)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AlfrescoResponseDTO.class)
                .block();
    }

}
