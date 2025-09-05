package com.gal.afiliaciones.application.service.documentnamestandardization.impl;

import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.infrastructure.enums.DocumentNameStandardization;
import org.springframework.stereotype.Service;

@Service
public class DocumentNameStandardizationServiceImpl implements DocumentNameStandardizationService {

    @Override
    public String getName(String fileName, String nomenclature, String numberDocument) {

        if(fileName == null || !fileName.contains(".") || numberDocument == null || numberDocument.isEmpty())
            throw new AffiliationError("Error al generar el nombre del documento");


        String name = DocumentNameStandardization.findByDescription(fileName.substring(0, fileName.lastIndexOf('.')));

        if(!name.isEmpty())
            return name.concat("-").concat(numberDocument).concat(fileName.substring(fileName.lastIndexOf('.')));

        return nomenclature + "-" + numberDocument + fileName.substring(fileName.lastIndexOf('.'));
    }
}
