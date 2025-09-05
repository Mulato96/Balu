package com.gal.afiliaciones.infrastructure.validation.document;

import com.gal.afiliaciones.infrastructure.utils.Constant;

import java.util.regex.Pattern;

public final class ValidationDocument {

    private ValidationDocument() {
        // Prevent instantiation
    }

    private static final String INITIAL_NUMBER_JURIDICA = "8";

    private static final String REGEX_CC = "^[1-9][0-9]{3,10}$";
    private static final String REGEX_NI_NATURAL = "^[0-9]{9}$";
    private static final String REGEX_NI_JURIDICA = "^[8-9][0-9]{9}$";
    private static final String REGEX_CE = "^(0|[1-9][0-9]*){3,10}$";
    private static final String REGEX_CD = "^[a-zA-Z0-9]{3,11}$";
    private static final String REGEX_SC = "^[0-9]{9}$";
    private static final String REGEX_PT = "^[0-9]{1,8}$";
    
    public static boolean isValid(String document, String documentType) {
        return (documentType.equals(Constant.NI) && document.startsWith(INITIAL_NUMBER_JURIDICA) && Pattern.matches(REGEX_NI_JURIDICA, document))
            || (documentType.equals(Constant.NI) && Pattern.matches(REGEX_NI_NATURAL, document))
            || (documentType.equals(Constant.CC) && Pattern.matches(REGEX_CC, document))
            || (documentType.equals(Constant.CE) && Pattern.matches(REGEX_CE, document))
            || (documentType.equals(Constant.CD) && Pattern.matches(REGEX_CD, document))
            || (documentType.equals(Constant.SC) && Pattern.matches(REGEX_SC, document))
            || (documentType.equals(Constant.PT) && Pattern.matches(REGEX_PT, document));
    }

    public static boolean isValidDocument(String documentType) {
        return documentType.equals(Constant.CC) || documentType.equals(Constant.NI) || documentType.equals(Constant.CE)
                || documentType.equals(Constant.CD) || documentType.equals(Constant.SC) || documentType.equals(Constant.PT);
    }
    
}
