package com.gal.afiliaciones.application.service;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public interface IValidationService {

    List<String> validateRow(Row row);
}