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

        // Validate Tipo documento trabajador (column 0)
        if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
            errors.add("El tipo de documento del trabajador es obligatorio.");
        }

        // Validate Número documento (column 1)
        if (row.getCell(1) == null || row.getCell(1).getStringCellValue().isEmpty()) {
            errors.add("El número de documento del trabajador es obligatorio.");
        }

        // Validate Nombre completo (column 2)
        if (row.getCell(2) == null || row.getCell(2).getStringCellValue().isEmpty()) {
            errors.add("El nombre completo del trabajador es obligatorio.");
        }

        // Validate Tipo de vinculación (column 3)
        if (row.getCell(3) == null || row.getCell(3).getStringCellValue().isEmpty()) {
            errors.add("El tipo de vinculación es obligatorio.");
        }

        // Validate Subtipo de vinculación (column 4)
        if (row.getCell(4) == null || row.getCell(4).getStringCellValue().isEmpty()) {
            errors.add("El subtipo de vinculación es obligatorio.");
        }

        // Validate Fecha de retiro (column 5)
        if (row.getCell(5) == null || row.getCell(5).getStringCellValue().isEmpty()) {
            errors.add("La fecha de retiro es obligatoria.");
        } else {
            try {
                LocalDate.parse(row.getCell(5).getStringCellValue());
            } catch (Exception e) {
                errors.add("El formato de la fecha de retiro es inválido.");
            }
        }
        // TODO: Add more specific validations based on business rules (HU #83868, etc.)
        return errors;
    }
}