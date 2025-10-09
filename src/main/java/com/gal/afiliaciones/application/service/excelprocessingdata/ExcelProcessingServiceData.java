package com.gal.afiliaciones.application.service.excelprocessingdata;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public interface ExcelProcessingServiceData {

    List<Map<String, Object>> converterExcelToMap(MultipartFile excel, List<String> listColumns) throws IOException;
    <T> List<T> converterMapToClass(List<Map<String, Object>> data, Class<T> clazz) throws IOException;
    <T> List<Integer> findDataDuplicate(List<T> elementos, Function<T, String> getAttribute, ToIntFunction<T> getId);
    <T> ExportDocumentsDTO createDocumentExcelErrors(List<T> listErrors);
    List<LinkedHashMap<String, Object>> findByPensionOrEpsOrArl(String url);
    List<FundEpsDTO> findByEps(String url);
    List<FundAfpDTO> findByAfp(String url);
    void saveDetailRecordLoadBulk(List<String> dataDetail, Long idRecordLoadBulk);
    void saveDetailRecordMassiveUpdate(List<ErrorFileExcelDTO> dataDetail, Long idRecodLoadBulk);
    ExportDocumentsDTO exportDataGrid(RequestExportDTO requestExportDTO);
    String converterClassToString(Object data);
    <T> String createDocumentError(String base64, List<T> dataExcelIndependentDTOS, String type) throws IOException;

}
