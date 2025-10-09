package com.gal.afiliaciones.application.service.excelprocessingdata.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.DetailRecordLoadBulkService;
import com.gal.afiliaciones.application.service.employer.DetailRecordMassiveUpdateWorkerService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadDependent;
import com.gal.afiliaciones.infrastructure.enums.FieldsExcelLoadIndependent;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.Base64;

@Slf4j
@Service
@AllArgsConstructor
public class ExcelProcessingDataServiceImpl implements ExcelProcessingServiceData {

    private final WebClient webClient;
    private final CollectProperties properties;
    private final DetailRecordLoadBulkService recordLoadBulkService;
    private final DetailRecordMassiveUpdateWorkerService recordMassiveUpdateWorkerService;

    @Override
    public <T> String createDocumentError(String base64, List<T> dataExcelIndependentDTOS, String type) throws IOException {

        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            AtomicInteger lastRowNum = new AtomicInteger(0);

            Row rowHeader = sheet.getRow(0);
            rowHeader.createCell(rowHeader.getLastCellNum()).setCellValue("ERROR");

            List<Map<Integer, String>> listMap = toMapWithJsonProperty(dataExcelIndependentDTOS, type);

            listMap.forEach(data -> {
                Row row = sheet.createRow(lastRowNum.incrementAndGet());

                String error = "";
                for (Map.Entry<Integer, String> entry : data.entrySet()){

                    if(entry.getKey() < 0){
                        error = entry.getValue();
                        continue;
                    }

                    row.createCell(entry.getKey()-1).setCellValue(entry.getValue());
                }

                int lastColumn = row.getLastCellNum();

                if(lastColumn >= 0)
                    row.createCell(lastColumn).setCellValue(error);

            });


            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            byte[] fileBytes = outputStream.toByteArray();
            return  Base64.getEncoder().encodeToString(fileBytes);
        }
    }

    @Override
    public  List<Map<String, Object>> converterExcelToMap(MultipartFile excel, List<String> listColumn) throws IOException {

        List<Map<String, Object>> dataList = new ArrayList<>();
        InputStream is = excel.getInputStream();

        try(Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int counterRow = countRow(sheet, 0, sheet.getLastRowNum());

            if(counterRow <= -1)
                throw new AffiliationError("Error al leer el documento cargado.");

            Row headerRow = sheet.getRow(counterRow);

            if(validColumns(listColumn, headerRow)){
                throw new AffiliationError("El documento no cuenta con las columnas solicitadas.");
            }

            for (int i = counterRow; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if(isRowEmpty(row))
                    continue;

                Map<String, Object> data = new HashMap<>();
                int numFiled = i;
                ++numFiled;

                for (int j = 0; j < headerRow.getLastCellNum(); j++) {

                    String value = getCellValue(row.getCell(j));

                    if(headerRow.getCell(j).getStringCellValue().equals(value)){
                        break;
                    }

                    String columnName = columnName(headerRow.getCell(j).getStringCellValue());
                    data.put(columnName, value.replaceAll("^\\s+|\\s+$", ""));
                }

                if(!data.isEmpty()){
                    data.put("ID REGISTRO", numFiled);
                    dataList.add(data);
                }

            }

            return dataList;

        }catch (AffiliationError affiliation) {
            throw affiliation;
        }catch (Exception e) {
            throw new AffiliationError("Error al leer el documento cargado.");
        }
    }

    @Override
    public <T> List<T> converterMapToClass(List<Map<String, Object>> dataMap, Class<T> clazz) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return dataMap.stream().map(data -> mapper.convertValue(data,clazz)).toList();
    }

    @Override
    public  <T> List<Integer> findDataDuplicate(List<T> elementos, Function<T, String> getAttribute, ToIntFunction<T> getId) {

        // Agrupar por el atributo y contar las ocurrencias
        Map<String, Long> countByAttribute = elementos.stream()
                .collect(Collectors.groupingBy(getAttribute, Collectors.counting()));

        // Filtrar los elementos con el atributo repetido y obtener los ids correspondientes
        return elementos.stream()
                .filter(elemento -> countByAttribute.get(getAttribute.apply(elemento)) > 1)  // Verificar si el atributo se repite
                .map(getId::applyAsInt)  // Mapear a los ids
                .distinct()  // Para que no se repitan los ids
                .toList();
    }

    @Override
    public <T> ExportDocumentsDTO createDocumentExcelErrors(List<T> listErrors){

        try {

            String url = properties.getUrlTransversal().concat("download/exportDataGrid");

            Map<String, Object> data = new HashMap<>();
            data.put("data", listErrors);
            data.put("format", "Excel");
            data.put("prefixNameFile", "ErrorReport");

            return webClient.post()
                    .uri(url)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(ExportDocumentsDTO.class)
                    .onErrorResume(e -> Mono.empty())
                    .block();

        }catch (Exception e){
            throw  new AffiliationError("Error, no se pudo generar el documento con los errores presentados durante la carga masiva");
        }

    }

    @Override
    public ExportDocumentsDTO exportDataGrid(RequestExportDTO requestExportDTO) {

        try {
            String url = properties.getUrlTransversal().concat("download/exportDataGrid");

            // Crear un ObjectMapper para convertir el Map a JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Convertir el Map a JSON String
            objectMapper.writeValueAsString(requestExportDTO);

            return webClient.post()
                    .uri(url)
                    .bodyValue(requestExportDTO)
                    .retrieve()
                    .bodyToMono(ExportDocumentsDTO.class)
                    .onErrorResume(e -> Mono.empty())
                    .block();
        } catch (Exception e){
            throw  new AffiliationError("Error, no se pudieron exportar los datos de la grilla");
        }
    }

    @Override
    public String converterClassToString(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new AffiliationError("Error procesando el documento");
        }

    }

    @Override
    public List<LinkedHashMap<String, Object>> findByPensionOrEpsOrArl(String url){

        url = properties.getUrlTransversal().concat(url);

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(List.class).block();

    }

    @Override
    public List<FundEpsDTO> findByEps(String url){

        url = properties.getUrlTransversal().concat(url);

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FundEpsDTO>>() {})
                .block();

    }

    @Override
    public List<FundAfpDTO> findByAfp(String url){

        url = properties.getUrlTransversal().concat(url);

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FundAfpDTO>>() {})
                .block();

    }

    @Async
    @Override
    public void saveDetailRecordLoadBulk(List<String> dataDetail, Long idRecordLoadBulk) {

        List<DetailRecordLoadBulk> detail = dataDetail.stream()
                .map(data -> {
                        DetailRecordLoadBulk recordLoadBulk = new DetailRecordLoadBulk();
                        recordLoadBulk.setError(data);
                        recordLoadBulk.setIdRecordLoadBulk(idRecordLoadBulk);
                        return recordLoadBulk;
                })
                .toList();

        recordLoadBulkService.saveDetail(detail);

    }

    private String columnName(String name){

        name = name.replace("(DD/MM/AAA)", "").replace("(AAAA/MM/DD)", "").replaceAll("[^\\p{L}\\s]", "") .trim();
        String[] vec = name.split("\\n");
        return ((vec.length >= 3) ? vec[0].concat(vec[1]) : vec[0]).replace("Obligatorio", "").trim();

    }

    private int countRow(Sheet sheet, int counterRow, int totalRow){

        if(counterRow <= totalRow){

            Row row = sheet.getRow(counterRow);

            for (int j = 0; j < row.getLastCellNum(); j++) {

                if(row.getCell(j).getStringCellValue() != null && !Objects.equals(row.getCell(j).getStringCellValue(),"")){
                    return counterRow;
                }

            }

            return countRow(sheet, ++counterRow, totalRow);
        }

        return -1;
    }

    private String getCellValue(Cell cell) {

        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));


        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private boolean validColumns(List<String> listColumns, Row headerColumns){

        List<String> nameColumns = new ArrayList<>(listColumns(headerColumns)
                .stream()
                .filter(name -> !name.equalsIgnoreCase("error"))
                .toList());
        Collections.sort(nameColumns);
        Collections.sort(listColumns);

        return !nameColumns.equals(listColumns);

    }

    private List<String> listColumns(Row row){

        try{

            List<String> listColumns = new ArrayList<>();

            for(int i = 0; i < row.getLastCellNum(); i++){
                listColumns.add(columnName(getCellValue(row.getCell(i))));
            }

            return listColumns;

        }catch (Exception e){
            throw  new AffiliationError("Error al leer el documento cargado");
        }
    }

    @Override
    public void saveDetailRecordMassiveUpdate(List<ErrorFileExcelDTO> dataDetail, Long idRecodLoadBulk) {

        dataDetail.forEach(data -> {

            DetailRecordMassiveUpdateWorker recordLoadBulk = new DetailRecordMassiveUpdateWorker();

            BeanUtils.copyProperties(data, recordLoadBulk);
            recordLoadBulk.setIdRecordLoadBulk(idRecodLoadBulk);
            recordMassiveUpdateWorkerService.saveDetail(recordLoadBulk);

        });

    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        DataFormatter formatter = new DataFormatter();

        for (Cell cell : row) {
            String cellValue = formatter.formatCellValue(cell);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private <T> List<Map<Integer, String>> toMapWithJsonProperty(List<T> list, String type) {

       return  list.stream().map(data -> {

            Class<?> clazz = data.getClass();
            Map<String,Integer> mapHeader = (type.equals(Constant.TYPE_AFFILLATE_INDEPENDENT))
                    ? FieldsExcelLoadIndependent.map()
                    : FieldsExcelLoadDependent.map();

            Map<Integer, String> map =  new HashMap<>();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                if (annotation != null) {
                    try {

                        map.putAll(createMap(field.get(data), annotation.value(), mapHeader));

                    } catch (IllegalAccessException e) {
                        log.error("Error in the method toMapWithJsonProperty, record: {}, error: {}", data, e.getMessage());
                    }
                }
            }

           return map;
       }).toList();

    }

    private Map<Integer, String> createMap(Object value, String header, Map<String, Integer> map){

        Map<Integer, String> mapValue = new HashMap<>();
        String data = (value != null)
                ? value.toString()
                : "";

        if(header.equalsIgnoreCase("error")){
            mapValue.put(-1, data);
            return mapValue;
        }

        Integer key = map.get(header);

        if(key != null)
          mapValue.put(key, data);

        return mapValue;

    }

}
