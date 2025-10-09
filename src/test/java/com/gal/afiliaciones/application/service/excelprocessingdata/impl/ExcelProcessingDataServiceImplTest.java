package com.gal.afiliaciones.application.service.excelprocessingdata.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ExcelProcessingDataServiceImplTest {

    @Mock
    private WebClient webClient;
    @Mock
    private CollectProperties properties;
    @Mock
    private DetailRecordLoadBulkService recordLoadBulkService;
    @Mock
    private DetailRecordMassiveUpdateWorkerService recordMassiveUpdateWorkerService;
    @InjectMocks
    private ExcelProcessingDataServiceImpl excelProcessingDataService;

    private MultipartFile excelFile;
    private List<String> listColumn;

    @BeforeEach
    void setUp() throws IOException {
        String excelContent = "Column1,Column2\nValue1,Value2";
        excelFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelContent.getBytes());
        listColumn = List.of("Column1", "Column2");
    }

    @Test
    void converterExcelToMap_Error() throws IOException {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        assertThrows(AffiliationError.class, () -> excelProcessingDataService.converterExcelToMap(emptyFile, listColumn));
    }

    @Test
    void converterMapToClass_Success() throws IOException {
        List<Map<String, Object>> dataMap = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        dataMap.add(map);

        List<TestClass> result = excelProcessingDataService.converterMapToClass(dataMap, TestClass.class);
        assertEquals("test", result.get(0).getName());
    }

    @Test
    void findDataDuplicate_Success() {
        List<TestClass> elements = new ArrayList<>();
        elements.add(new TestClass("test1", 1));
        elements.add(new TestClass("test2", 2));
        elements.add(new TestClass("test1", 3));

        Function<TestClass, String> getAttribute = TestClass::getName;
        ToIntFunction<TestClass> getId = TestClass::getId;

        List<Integer> duplicates = excelProcessingDataService.findDataDuplicate(elements, getAttribute, getId);
        assertEquals(List.of(1, 3), duplicates);
    }

    @Test
    void createDocumentExcelErrors_Success() {
        List<ErrorFileExcelDTO> listErrors = new ArrayList<>();
        ErrorFileExcelDTO error = new ErrorFileExcelDTO();
        listErrors.add(error);

        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ExportDocumentsDTO.class)).thenReturn(Mono.just(new ExportDocumentsDTO()));

        ExportDocumentsDTO result = excelProcessingDataService.createDocumentExcelErrors(listErrors);
        assertEquals(ExportDocumentsDTO.class, result.getClass());
    }

    @Test
    void createDocumentExcelErrors_Error() {
        List<ErrorFileExcelDTO> listErrors = new ArrayList<>();
        ErrorFileExcelDTO error = new ErrorFileExcelDTO();
        listErrors.add(error);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.post()).thenThrow(new RuntimeException("Test Exception"));

        assertThrows(AffiliationError.class, () -> excelProcessingDataService.createDocumentExcelErrors(listErrors));
    }

    @Test
    void exportDataGrid_Success() {
        RequestExportDTO requestExportDTO = RequestExportDTO.builder().build();

        WebClient.RequestBodyUriSpec requestBodyUriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ExportDocumentsDTO.class)).thenReturn(Mono.just(new ExportDocumentsDTO()));

        ExportDocumentsDTO result = excelProcessingDataService.exportDataGrid(requestExportDTO);
        assertEquals(ExportDocumentsDTO.class, result.getClass());
    }

    @Test
    void exportDataGrid_Error() {
        RequestExportDTO requestExportDTO = RequestExportDTO.builder().build();

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.post()).thenThrow(new RuntimeException("Test Exception"));

        assertThrows(AffiliationError.class, () -> excelProcessingDataService.exportDataGrid(requestExportDTO));
    }

    @Test
    void findByPensionOrEpsOrArl_Success() {
        String url = "test";
        List<LinkedHashMap<String, Object>> expected = new ArrayList<>();

        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(List.class)).thenReturn(Mono.just(expected));

        List<LinkedHashMap<String, Object>> result = excelProcessingDataService.findByPensionOrEpsOrArl(url);
        assertEquals(expected, result);
    }

    @Test
    void findByEps_Success() {
        String url = "test";
        List<LinkedHashMap<String, Object>> expected = new ArrayList<>();

        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new ArrayList<>()));

        List result = excelProcessingDataService.findByEps(url);
        assertEquals(ArrayList.class, result.getClass());
    }

    @Test
    void findByAfp_Success() {
        String url = "test";
        List<LinkedHashMap<String, Object>> expected = new ArrayList<>();

        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(properties.getUrlTransversal()).thenReturn("http://test.com/");
        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(new ArrayList<>()));

        List result = excelProcessingDataService.findByAfp(url);
        assertEquals(ArrayList.class, result.getClass());
    }


    @Test
    void saveDetailRecordMassiveUpdate_Success() {
        List<ErrorFileExcelDTO> dataDetail = new ArrayList<>();
        dataDetail.add(new ErrorFileExcelDTO());
        Long idRecodLoadBulk = 1L;

        excelProcessingDataService.saveDetailRecordMassiveUpdate(dataDetail, idRecodLoadBulk);
    }

    static class TestClass {
        private String name;
        private int id;

        public TestClass() {
        }

        public TestClass(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}