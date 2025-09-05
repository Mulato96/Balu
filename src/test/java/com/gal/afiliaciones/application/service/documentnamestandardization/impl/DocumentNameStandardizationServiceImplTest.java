package com.gal.afiliaciones.application.service.documentnamestandardization.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;


class DocumentNameStandardizationServiceImplTest {

    private DocumentNameStandardizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentNameStandardizationServiceImpl();
    }

    @Test
    void getName_shouldReturnStandardizedName_whenValidInputs() {
        String fileName = "document.pdf";
        String nomenclature = "NOM";
        String numberDocument = "12345";
        String expected = "NOM-12345.pdf";

        String result = service.getName(fileName, nomenclature, numberDocument);

        assertEquals(expected, result);
    }

    @Test
    void getName_shouldThrowAffiliationError_whenFileNameIsNull() {
        assertThrows(AffiliationError.class, () ->
                service.getName(null, "NOM", "12345"));
    }

    @Test
    void getName_shouldThrowAffiliationError_whenFileNameHasNoExtension() {
        assertThrows(AffiliationError.class, () ->
                service.getName("document", "NOM", "12345"));
    }

    @Test
    void getName_shouldThrowAffiliationError_whenNumberDocumentIsNull() {
        assertThrows(AffiliationError.class, () ->
                service.getName("document.pdf", "NOM", null));
    }

    @Test
    void getName_shouldThrowAffiliationError_whenNumberDocumentIsEmpty() {
        assertThrows(AffiliationError.class, () ->
                service.getName("document.pdf", "NOM", ""));
    }

    @Test
    void getName_shouldHandleFileNameWithMultipleDots() {
        String fileName = "my.document.v2.pdf";
        String nomenclature = "NOM";
        String numberDocument = "67890";
        String expected = "NOM-67890.pdf";

        String result = service.getName(fileName, nomenclature, numberDocument);

        assertEquals(expected, result);
    }
}