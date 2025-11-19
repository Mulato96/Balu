package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IValidationService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationServiceImpl implements IValidationService {

    @Override
    public List<String> validateRow(Row row) {
        List<String> errors = new ArrayList<>();

        // Helper para obtener valores seguros
        String tipoDocumentoEmpleador = getCellValue(row.getCell(0));
        String numeroDocumentoEmpleador = getCellValue(row.getCell(1));
        String tipoDocumentoTrabajador = getCellValue(row.getCell(3));
        String numeroDocumentoTrabajador = getCellValue(row.getCell(4));
        String tipoVinculacion = getCellValue(row.getCell(5));
        String fechaRetiro = getCellValue(row.getCell(6));

        // Validate Tipo documento empleador (column 0)
        if (tipoDocumentoEmpleador.isEmpty()) {
            errors.add("El tipo de documento del empleador es obligatorio.");
        }

        // Validate Número documento empleador (column 1)
        if (numeroDocumentoEmpleador.isEmpty()) {
            errors.add("El número de documento del empleador es obligatorio.");
        }

        // Validate Tipo documento trabajador (column 3)
        if (tipoDocumentoTrabajador.isEmpty()) {
            errors.add("El tipo de documento del trabajador es obligatorio.");
        }

        // Validate Número documento trabajador (column 4)
        if (numeroDocumentoTrabajador.isEmpty()) {
            errors.add("El número de documento del trabajador es obligatorio.");
        }

        // Validate Tipo de vinculación (column 5)
        if (tipoVinculacion.isEmpty()) {
            errors.add("El tipo de vinculación es obligatorio.");
        } else if (!"1".equals(tipoVinculacion) && !"2".equals(tipoVinculacion)) {
            errors.add("El tipo de vinculación debe ser '1' (Dependiente) o '2' (Independiente).");
        }

        // Validate Fecha de retiro (column 6)
        if (fechaRetiro.isEmpty()) {
            errors.add("La fecha de retiro es obligatoria.");
        } else {
            try {
                LocalDate.parse(fechaRetiro);
            } catch (Exception e) {
                errors.add("El formato de la fecha de retiro es inválido.");
            }
        }

        return errors;
    }

    /**
     * Método auxiliar seguro para obtener el valor textual de cualquier celda.
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);

            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case BLANK:
            default:
                return "";
        }
    }
}