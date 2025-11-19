package com.gal.afiliaciones.application.service.excel;

import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.EmployerUpdateDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class EmployerExcelReader {

    public List<EmployerUpdateDTO> read(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Integer> headerMap = processHeader(rowIterator);

            List<EmployerUpdateDTO> dtos = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                EmployerUpdateDTO dto = mapRowToDTO(row, headerMap);
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
            "TIPO DOCUMENTO", "NUMERO IDENTIFICACION", "RAZON SOCIAL",
            "ID DEPARTAMENTO", "ID CIUDAD", "DIRECCION",
            "TELEFONO 1", "TELEFONO 2", "EMAIL"
        };
        for (String expectedHeader : expectedHeaders) {
            if (!headerMap.containsKey(expectedHeader)) {
                throw new RuntimeException("Missing expected header: " + expectedHeader);
            }
        }
    }

    private EmployerUpdateDTO mapRowToDTO(Row row, Map<String, Integer> headerMap) {
        return EmployerUpdateDTO.builder()
                .docType(getStringCellValue(row.getCell(headerMap.get("TIPO DOCUMENTO"))))
                .docNumber(getStringCellValue(row.getCell(headerMap.get("NUMERO IDENTIFICACION"))))
                .businessName(getStringCellValue(row.getCell(headerMap.get("RAZON SOCIAL"))))
                .departmentId(getStringCellValue(row.getCell(headerMap.get("ID DEPARTAMENTO"))))
                .cityId(getStringCellValue(row.getCell(headerMap.get("ID CIUDAD"))))
                .addressFull(getStringCellValue(row.getCell(headerMap.get("DIRECCION"))))
                .phone1(getStringCellValue(row.getCell(headerMap.get("TELEFONO 1"))))
                .phone2(getStringCellValue(row.getCell(headerMap.get("TELEFONO 2"))))
                .email(getStringCellValue(row.getCell(headerMap.get("EMAIL"))))
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }
}
