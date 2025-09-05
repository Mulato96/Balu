package com.gal.afiliaciones.application.service.excelprocessingdata.impl;

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
import lombok.AllArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExcelProcessingDataServiceImpl implements ExcelProcessingServiceData {

    private final WebClient webClient;
    private final CollectProperties properties;
    private final DetailRecordLoadBulkService recordLoadBulkService;
    private final DetailRecordMassiveUpdateWorkerService recordMassiveUpdateWorkerService;

    @Override
    public  List<Map<String, Object>> converterExcelToMap(MultipartFile excel,List<String> listColumn, int numberFile) throws IOException {

        List<Map<String, Object>> dataList = new ArrayList<>();
        InputStream is = excel.getInputStream();

        try(Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int counterRow = countRow(sheet, numberFile, sheet.getLastRowNum());

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

                    String record = getCellValue(row.getCell(j));

                    if(headerRow.getCell(j).getStringCellValue().equals(record)){
                        break;
                    }

                    String columnName = columnName(headerRow.getCell(j).getStringCellValue());
                    data.put(columnName, record.replaceAll("^\\s+|\\s+$", ""));
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

    @Override
    public void saveDetailRecordLoadBulk(List<ErrorFileExcelDTO> dataDetail, Long idRecodLoadBulk) {

        dataDetail.forEach(data -> {

            DetailRecordLoadBulk recordLoadBulk = new DetailRecordLoadBulk();

            BeanUtils.copyProperties(data, recordLoadBulk);
            recordLoadBulk.setIdRecordLoadBulk(idRecodLoadBulk);
            recordLoadBulkService.saveDetail(recordLoadBulk);

        });

    }

    private String columnName(String name){

        name = name.replaceAll("\\(.*?\\)","");
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

        List<String> nameColumns =  listColumns(headerColumns);
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

}
