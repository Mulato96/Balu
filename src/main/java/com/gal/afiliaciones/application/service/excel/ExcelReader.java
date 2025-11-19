package com.gal.afiliaciones.application.service.excel;

import com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive.UpdateInfoBasicaDTO;
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
public class ExcelReader {

    public List<UpdateInfoBasicaDTO> read(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Integer> headerMap = processHeader(rowIterator);

            List<UpdateInfoBasicaDTO> dtos = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                UpdateInfoBasicaDTO dto = mapRowToDTO(row, headerMap);
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
            "TIPO DOCUMENTO", "NUMERO IDENTIFICACION", "PRIMER NOMBRE", "SEGUNDO NOMBRE",
            "PRIMER APELLIDO", "SEGUNDO APELLIDO", "FECHA NACIMIENTO", "NACIONALIDAD",
            "SEXO", "AFP", "EPS", "EMAIL", "TELEFONO 1", "TELEFONO 2",
            "ID DEPARTAMENTO", "ID CIUDAD", "DIRECCION", "OBSERVACIONES"
        };
        for (String expectedHeader : expectedHeaders) {
            if (!headerMap.containsKey(expectedHeader)) {
                throw new RuntimeException("Missing expected header: " + expectedHeader);
            }
        }
    }

    private UpdateInfoBasicaDTO mapRowToDTO(Row row, Map<String, Integer> headerMap) {
        return UpdateInfoBasicaDTO.builder()
                .tipoDocumento(getStringCellValue(row.getCell(headerMap.get("TIPO DOCUMENTO"))))
                .numeroIdentificacion(getStringCellValue(row.getCell(headerMap.get("NUMERO IDENTIFICACION"))))
                .primerNombre(getStringCellValue(row.getCell(headerMap.get("PRIMER NOMBRE"))))
                .segundoNombre(getStringCellValue(row.getCell(headerMap.get("SEGUNDO NOMBRE"))))
                .primerApellido(getStringCellValue(row.getCell(headerMap.get("PRIMER APELLIDO"))))
                .segundoApellido(getStringCellValue(row.getCell(headerMap.get("SEGUNDO APELLIDO"))))
                .fechaNacimiento(getDateCellValue(row.getCell(headerMap.get("FECHA NACIMIENTO"))))
                .nacionalidad(getStringCellValue(row.getCell(headerMap.get("NACIONALIDAD"))))
                .sexo(getStringCellValue(row.getCell(headerMap.get("SEXO"))))
                .afp(getStringCellValue(row.getCell(headerMap.get("AFP"))))
                .eps(getStringCellValue(row.getCell(headerMap.get("EPS"))))
                .email(getStringCellValue(row.getCell(headerMap.get("EMAIL"))))
                .telefono1(getStringCellValue(row.getCell(headerMap.get("TELEFONO 1"))))
                .telefono2(getStringCellValue(row.getCell(headerMap.get("TELEFONO 2"))))
                .idDepartamento(getIntegerCellValue(row.getCell(headerMap.get("ID DEPARTAMENTO"))))
                .idCiudad(getIntegerCellValue(row.getCell(headerMap.get("ID CIUDAD"))))
                .direccionTexto(getStringCellValue(row.getCell(headerMap.get("DIRECCION"))))
                .observaciones(getStringCellValue(row.getCell(headerMap.get("OBSERVACIONES"))))
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
