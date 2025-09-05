package com.gal.afiliaciones.application.service.alfresco;

import static java.lang.String.format;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlfrescoServiceImpl implements AlfrescoService{

    private final WebClient webClient;
    private final CollectProperties properties;

    @Override
    public AlfrescoUploadResponse uploadFileAlfresco(AlfrescoUploadRequest request) throws IOException {

        final String uri = format("%s", properties.getGetCreateFolderUri() + "/transversal/file/uploadFile?fileName=" + request.getFile().getOriginalFilename() + "&folderId=" + request.getFolderId());


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(request.getFile().getBytes()) {
            @Override
            public String getFilename() {
                return request.getFile().getOriginalFilename();
            }
        });


        Mono<AlfrescoUploadResponse> response = webClient
                .post()
                .uri(UriComponentsBuilder.fromHttpUrl(uri).toUriString())
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(AlfrescoUploadResponse.class);

        return response.blockOptional(Duration.ofSeconds(properties.getTimeoutInSeconds())).orElseThrow(() ->
                new RuntimeException("Error cargando documentos"));

    }

    @Override
    public AlfrescoUploadResponse createFolder(String idParentNode, String nameFolder) {
        final String url = format("%s", properties.getGetCreateFolderUri() + "/transversal/file/create/folder?parentFolderId=" + idParentNode + "&folderName=" + nameFolder);


        return webClient.post()
                .uri(UriComponentsBuilder.fromHttpUrl(url).toUriString())
                .retrieve()
                .bodyToMono(AlfrescoUploadResponse.class).blockOptional(Duration.ofSeconds(properties.getTimeoutInSeconds()))
                .orElseThrow(() -> new RuntimeException("Error creando la carpeta en alfresco"));
    }

    @Override
    public Optional<ConsultFiles> getIdDocumentsFolder(String idFolder) {
        return this.webClient.get()
                .uri(String.format(properties.getFolderId(), idFolder))
                .retrieve()
                .bodyToMono(ConsultFiles.class)
                .onErrorResume(Throwable.class, GenericWebClient::handleErrors).blockOptional();
    }

    @Override
    public String getDocument(String idDocument) {
        return this.webClient.get()
                .uri(String.format(properties.getDocument(), idDocument))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(Throwable.class, GenericWebClient::handleErrors)
                .block();
    }

    @Override
    public ResponseUploadOrReplaceFilesDTO uploadOrReplaceFiles(String idNode, String nameFolder, List<MultipartFile> files) throws IOException {
        final String urlService = format("%s", properties.getGetCreateFolderUri() + "/transversal/AlfrescoController/" +
                "uploadOrReplaceFiles?idNode=" + idNode + "&nameFolder=" + nameFolder);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for(MultipartFile file : files) {
            body.add("files", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
        }

        return this.webClient
                .post()
                .uri(urlService)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ResponseUploadOrReplaceFilesDTO.class)
                .timeout(Duration.ofSeconds(60))
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Error upload or replace documents in Alfresco"));
    }

    @Override
    public ResponseUploadOrReplaceFilesDTO uploadAffiliationDocuments(String idNode, String filedNumber, List<MultipartFile> files) throws IOException {
        final String urlService = format("%s", properties.getGetCreateFolderUri() + "/transversal/AlfrescoController/" +
                "uploadAffiliationDocuments?idNode=" + idNode + "&nameFolder=" + filedNumber);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for(MultipartFile file : files) {
            body.add("files", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
        }

        return this.webClient
                .post()
                .uri(urlService)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ResponseUploadOrReplaceFilesDTO.class)
                .timeout(Duration.ofSeconds(60))
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Error upload or replace documents in Alfresco"));
    }

}
