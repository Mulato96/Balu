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
                if (isRowEmpty(row)) {
                    continue;
                }
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
                "TIPO_DOCUMENTO_EMPLEADOR", "DOCUMENTO_EMPLEADOR", "RAZON_SOCIAL",
                "CODIGO_SUBEMPRESA_(SOLO PARA EL NIT 899999061)", "CORREO_ELECTRONICO",
                "DIRECCION", "TELEFONO", "CODIGO_DEPARTAMENTO", "CODIGO_MUNICIPIO",
                "TIPO_DOCUMENTO_REPRESENTANTE_LEGAL", "DOCUMENTO_REPRESENTANTE_LEGAL",
                "PRIMER_NOMBRE_REPRESENTANTE_LEGAL", "SEGUNDO_NOMBRE_REPRESENTANTE_LEGAL",
                "PRIMER_APELLIDO_REPRESENTANTE_LEGAL", "SEGUNDO_APELLIDO_REPRESENTANTE_LEGAL"
        };
        for (String expectedHeader : expectedHeaders) {
            if (!headerMap.containsKey(expectedHeader)) {
                throw new RuntimeException("Missing expected header: " + expectedHeader);
            }
        }
    }

    private EmployerUpdateDTO mapRowToDTO(Row row, Map<String, Integer> headerMap) {
        return EmployerUpdateDTO.builder()
                .docType(getStringCellValue(row.getCell(headerMap.get("TIPO_DOCUMENTO_EMPLEADOR"))))
                .docNumber(getStringCellValue(row.getCell(headerMap.get("DOCUMENTO_EMPLEADOR"))))
                .businessName(getStringCellValue(row.getCell(headerMap.get("RAZON_SOCIAL"))))
                .subCompanyCode(getStringCellValue(row.getCell(headerMap.get("CODIGO_SUBEMPRESA_(SOLO PARA EL NIT 899999061)"))))
                .email(getStringCellValue(row.getCell(headerMap.get("CORREO_ELECTRONICO"))))
                .addressFull(getStringCellValue(row.getCell(headerMap.get("DIRECCION"))))
                .phone(getStringCellValue(row.getCell(headerMap.get("TELEFONO"))))
                .departmentId(getStringCellValue(row.getCell(headerMap.get("CODIGO_DEPARTAMENTO"))))
                .cityId(getStringCellValue(row.getCell(headerMap.get("CODIGO_MUNICIPIO"))))
                .legalRepDocType(getStringCellValue(row.getCell(headerMap.get("TIPO_DOCUMENTO_REPRESENTANTE_LEGAL"))))
                .legalRepDocNumber(getStringCellValue(row.getCell(headerMap.get("DOCUMENTO_REPRESENTANTE_LEGAL"))))
                .legalRepFirstName(getStringCellValue(row.getCell(headerMap.get("PRIMER_NOMBRE_REPRESENTANTE_LEGAL"))))
                .legalRepSecondName(getStringCellValue(row.getCell(headerMap.get("SEGUNDO_NOMBRE_REPRESENTANTE_LEGAL"))))
                .legalRepLastName(getStringCellValue(row.getCell(headerMap.get("PRIMER_APELLIDO_REPRESENTANTE_LEGAL"))))
                .legalRepSecondLastName(getStringCellValue(row.getCell(headerMap.get("SEGUNDO_APELLIDO_REPRESENTANTE_LEGAL"))))
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
