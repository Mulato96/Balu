package com.gal.afiliaciones.application.service.alfresco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadRequest;
import com.gal.afiliaciones.infrastructure.dto.alfresco.AlfrescoUploadResponse;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;

import reactor.core.publisher.Mono;


class AlfrescoServiceImplTest {

    private WebClient webClient;
    private CollectProperties properties;
    private AlfrescoServiceImpl alfrescoService;

    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        properties = mock(CollectProperties.class);
        alfrescoService = new AlfrescoServiceImpl(webClient, properties);

        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
    }

    @Test
    void uploadFileAlfresco_success() throws IOException {
        AlfrescoUploadRequest request = mock(AlfrescoUploadRequest.class);
        MultipartFile file = mock(MultipartFile.class);
        AlfrescoUploadResponse expectedResponse = new AlfrescoUploadResponse();

        when(request.getFile()).thenReturn(file);
        when(request.getFolderId()).thenReturn("folder123");
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getBytes()).thenReturn("content".getBytes());
        when(properties.getGetCreateFolderUri()).thenReturn("http://localhost");
        when(properties.getTimeoutInSeconds()).thenReturn(5L);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.MultipartInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AlfrescoUploadResponse.class)).thenReturn(Mono.just(expectedResponse));

        AlfrescoUploadResponse response = alfrescoService.uploadFileAlfresco(request);

        assertEquals(expectedResponse, response);
    }

    @Test
    void getIdDocumentsFolder_success() {
        ConsultFiles consultFiles = new ConsultFiles();
        when(properties.getFolderId()).thenReturn("http://localhost/folder/%s");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultFiles.class)).thenReturn(Mono.just(consultFiles));

        Optional<ConsultFiles> result = alfrescoService.getIdDocumentsFolder("folderId");
        assertTrue(result.isPresent());
        assertEquals(consultFiles, result.get());
    }

    @Test
    void getDocument_success() {
        String expectedContent = "document-content";
        when(properties.getDocument()).thenReturn("http://localhost/document/%s");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedContent));

        String result = alfrescoService.getDocument("docId");
        assertEquals(expectedContent, result);
    }

    @Test
    void uploadOrReplaceFiles_success() throws IOException {
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("file1.txt");
        when(file2.getOriginalFilename()).thenReturn("file2.txt");
        when(file1.getBytes()).thenReturn("abc".getBytes());
        when(file2.getBytes()).thenReturn("def".getBytes());

        ResponseUploadOrReplaceFilesDTO expectedResponse = new ResponseUploadOrReplaceFilesDTO();
        when(properties.getGetCreateFolderUri()).thenReturn("http://localhost");
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(MultiValueMap.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ResponseUploadOrReplaceFilesDTO.class)).thenReturn(Mono.just(expectedResponse));

        ResponseUploadOrReplaceFilesDTO response = alfrescoService.uploadOrReplaceFiles("nodeId", "folderName", List.of(file1, file2));
        assertEquals(expectedResponse, response);
    }

    @Test
    void uploadAffiliationDocuments_success() throws IOException {
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("file1.txt");
        when(file2.getOriginalFilename()).thenReturn("file2.txt");
        when(file1.getBytes()).thenReturn("abc".getBytes());
        when(file2.getBytes()).thenReturn("def".getBytes());

        ResponseUploadOrReplaceFilesDTO expectedResponse = new ResponseUploadOrReplaceFilesDTO();
        when(properties.getGetCreateFolderUri()).thenReturn("http://localhost");
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(MultiValueMap.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ResponseUploadOrReplaceFilesDTO.class)).thenReturn(Mono.just(expectedResponse));

        ResponseUploadOrReplaceFilesDTO response = alfrescoService.uploadAffiliationDocuments("nodeId", "folderName", List.of(file1, file2));
        assertEquals(expectedResponse, response);
    }
}
