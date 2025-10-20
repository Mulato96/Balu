package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IValidationService;
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

        // Validate Tipo documento empleador (column 0)
        if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
            errors.add("El tipo de documento del empleador es obligatorio.");
        }

        // Validate Número documento empleador (column 1)
        if (row.getCell(1) == null || row.getCell(1).getStringCellValue().isEmpty()) {
            errors.add("El número de documento del empleador es obligatorio.");
        }

        // Validate Código subempresa (column 2) - Assuming optional

        // Validate Tipo documento trabajador (column 3)
        if (row.getCell(3) == null || row.getCell(3).getStringCellValue().isEmpty()) {
            errors.add("El tipo de documento del trabajador es obligatorio.");
        }

        // Validate Número documento trabajador (column 4)
        if (row.getCell(4) == null || row.getCell(4).getStringCellValue().isEmpty()) {
            errors.add("El número de documento del trabajador es obligatorio.");
        }

        // Validate Tipo de vinculación (column 5)
        if (row.getCell(5) == null || row.getCell(5).getStringCellValue().isEmpty()) {
            errors.add("El tipo de vinculación es obligatorio.");
        } else {
            String affiliationTypeCode = row.getCell(5).getStringCellValue();
            if (!"1".equals(affiliationTypeCode) && !"2".equals(affiliationTypeCode)) {
                errors.add("El tipo de vinculación debe ser '1' (Dependiente) o '2' (Independiente).");
            }
        }

        // Validate Fecha de retiro (column 6)
        if (row.getCell(6) == null || row.getCell(6).getStringCellValue().isEmpty()) {
            errors.add("La fecha de retiro es obligatoria.");
        } else {
            try {
                LocalDate.parse(row.getCell(6).getStringCellValue());
            } catch (Exception e) {
                errors.add("El formato de la fecha de retiro es inválido.");
            }
        }
        // TODO: Add more specific validations based on business rules (HU #83868, etc.)
        return errors;
    }
}