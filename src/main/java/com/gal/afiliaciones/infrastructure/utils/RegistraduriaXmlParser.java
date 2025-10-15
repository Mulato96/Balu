package com.gal.afiliaciones.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RegistraduriaXmlParser {

    /**
     * Extract value from XML by tag name
     */
    public static String extractXmlValue(String xml, String tagName) {
        if (xml == null || tagName == null) {
            return null;
        }

        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) {
            log.debug("Start tag '{}' not found in XML", tagName);
            return null;
        }

        startIndex += startTag.length();
        int endIndex = xml.indexOf(endTag, startIndex);

        if (endIndex == -1) {
            log.debug("End tag '{}' not found in XML", tagName);
            return null;
        }

        String value = xml.substring(startIndex, endIndex).trim();
        log.debug("Extracted value for tag '{}': '{}'", tagName, value);
        return value;
    }

    /**
     * Extract value with default fallback
     */
    public static String extractXmlValueOrDefault(String xml, String tagName, String defaultValue) {
        String value = extractXmlValue(xml, tagName);
        return value != null ? value : defaultValue;
    }

    /**
     * Check if XML contains a specific tag
     */
    public static boolean containsTag(String xml, String tagName) {
        if (xml == null || tagName == null) {
            return false;
        }

        String startTag = "<" + tagName + ">";
        return xml.contains(startTag);
    }

    /**
     * Extract multiple values at once
     */
    public static String[] extractMultipleValues(String xml, String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            return new String[0];
        }

        String[] values = new String[tagNames.length];
        for (int i = 0; i < tagNames.length; i++) {
            values[i] = extractXmlValue(xml, tagNames[i]);
        }

        return values;
    }

    /**
     * Validate if XML has the expected Registraduria response structure
     */
    public static boolean isValidRegistraduriaResponse(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return false;
        }

        boolean hasEnvelope = xml.contains("<S:Envelope") || xml.contains("<soapenv:Envelope");
        boolean hasBody = xml.contains("<S:Body>") || xml.contains("<soapenv:Body>");
        boolean hasResponse = xml.contains("consultarCedulasResponse");

        return hasEnvelope && hasBody && hasResponse;
    }

    /**
     * Clean XML by removing extra whitespace
     */
    public static String cleanXml(String xml) {
        if (xml == null) {
            return null;
        }

        return xml.replaceAll("\\s+", " ")
                 .replaceAll(">\\s+<", "><")
                 .trim();
    }

    /**
     * Check if the response indicates a person was found
     */
    public static boolean isPersonFound(String xml) {
        if (xml == null) {
            return false;
        }

        String errorCode = extractXmlValue(xml, "codError");
        return "0".equals(errorCode);
    }

    /**
     * Check if the response indicates a person was not found
     */
    public static boolean isPersonNotFound(String xml) {
        if (xml == null) {
            return false;
        }

        String errorCode = extractXmlValue(xml, "codError");
        return "1".equals(errorCode);
    }
} 
