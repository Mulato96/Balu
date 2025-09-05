package com.gal.afiliaciones.application.service;

public interface CodeValidCertificationService {
    String consultCode(String numberDocument, String typeDocument);
    String consultCode(String numberDocument, String typeDocument, boolean isNotAfiliate);
}
