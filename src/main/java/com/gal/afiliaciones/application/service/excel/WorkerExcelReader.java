package com.gal.afiliaciones.application.service.excel;

import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.WorkerUpdateDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class WorkerExcelReader {

    public List<WorkerUpdateDTO> read(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Integer> headerMap = processHeader(rowIterator);

            List<WorkerUpdateDTO> dtos = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                WorkerUpdateDTO dto = mapRowToDTO(row, headerMap);
                dtos.add(dto);
            }
            return dtos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> processHeader(Iterator<Row> rowIterator) {
        if (!rowIterator.hasNext()) {
            throw new RuntimeException("Excel file is empty");
        }
        Row headerRow = rowIterator.next();
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
        validateHeader(headerMap);
        return headerMap;
    }

    private void validateHeader(Map<String, Integer> headerMap) {
        String[] expectedHeaders = {
                "TIPO_DOCUMENTO_TRABAJADOR", "DOCUMENTO_TRABAJADOR", "CODIGO_EPS", "CODIGO_AFP",
                "CORREO_ELECTRONICO_TRABAJADOR", "FECHA_DE_NACIMIENTO (AAAA/MM/DD)",
                "DIRECCION_DE_RESIDENCIA", "TELEFONO_DE_RESIDENCIA", "DEPARTAMENTO_DE_RESIDENCIA",
                "MUNICIPIO_DE_RESIDENCIA"
        };
        for (String expectedHeader : expectedHeaders) {
            if (!headerMap.containsKey(expectedHeader)) {
                throw new RuntimeException("Missing expected header: " + expectedHeader);
            }
        }
    }

    private WorkerUpdateDTO mapRowToDTO(Row row, Map<String, Integer> headerMap) {
        return WorkerUpdateDTO.builder()
                .tipoDocumento(getStringCellValue(row.getCell(headerMap.get("TIPO_DOCUMENTO_TRABAJADOR"))))
                .numeroIdentificacion(getStringCellValue(row.getCell(headerMap.get("DOCUMENTO_TRABAJADOR"))))
                .eps(getStringCellValue(row.getCell(headerMap.get("CODIGO_EPS"))))
                .afp(getStringCellValue(row.getCell(headerMap.get("CODIGO_AFP"))))
                .email(getStringCellValue(row.getCell(headerMap.get("CORREO_ELECTRONICO_TRABAJADOR"))))
                .fechaNacimiento(getDateCellValue(row.getCell(headerMap.get("FECHA_DE_NACIMIENTO (AAAA/MM/DD)"))))
                .direccion(getStringCellValue(row.getCell(headerMap.get("DIRECCION_DE_RESIDENCIA"))))
                .telefono(getStringCellValue(row.getCell(headerMap.get("TELEFONO_DE_RESIDENCIA"))))
                .departamento(getStringCellValue(row.getCell(headerMap.get("DEPARTAMENTO_DE_RESIDENCIA"))))
                .municipio(getStringCellValue(row.getCell(headerMap.get("MUNICIPIO_DE_RESIDENCIA"))))
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private java.time.LocalDate getDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }
        return null;
    }
}
