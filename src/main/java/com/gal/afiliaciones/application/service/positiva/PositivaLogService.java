package com.gal.afiliaciones.application.service.positiva;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.domain.model.PositivaInsertionLog;
import com.gal.afiliaciones.infrastructure.dao.repository.positiva.PositivaInsertionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositivaLogService {

    private final PositivaInsertionLogRepository positivaInsertionLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String serviceName,
                     String operation,
                     String documentType,
                     String documentNumber,
                     String fullName,
                     String affiliationType,
                     String affiliationSubtype,
                     String filedNumber,
                     String errorCode,
                     String errorMessage,
                     String responseBody,
                     Integer resultCode) {
        try {
            PositivaInsertionLog logEntity = PositivaInsertionLog.builder()
                    .serviceName(serviceName)
                    .operation(operation)
                    .documentType(documentType)
                    .documentNumber(documentNumber)
                    .fullName(fullName)
                    .affiliationType(affiliationType)
                    .affiliationSubtype(affiliationSubtype)
                    .filedNumber(filedNumber)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .responseBody(responseBody)
                    .resultCode(resultCode)
                    .build();

            positivaInsertionLogRepository.save(logEntity);
        } catch (Exception e) {
            // Nunca afectar el flujo principal por errores de logging
            log.debug("Error saving PositivaInsertionLog (ignored): {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveHttp(String serviceName,
                         String operation,
                         String documentType,
                         String documentNumber,
                         String fullName,
                         String affiliationType,
                         String affiliationSubtype,
                         String filedNumber,
                         int httpStatus,
                         String rawBody) {
        save(serviceName, operation, documentType, documentNumber, fullName, affiliationType, affiliationSubtype,
                filedNumber, String.valueOf(httpStatus), null, rawBody, httpStatus);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFromResponse(String serviceName,
                                 String operation,
                                 String documentType,
                                 String documentNumber,
                                 String fullName,
                                 String affiliationType,
                                 String affiliationSubtype,
                                 String filedNumber,
                                 Object response) {
        try {
            String responseBody = toJson(response);
            Integer result = PositivaResultUtil.deriveResultCode(response);
            save(serviceName, operation, documentType, documentNumber, fullName, affiliationType, affiliationSubtype,
                    filedNumber, null, null, responseBody, result);
        } catch (Exception e) {
            log.debug("Error saving PositivaInsertionLog from response (ignored): {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveError(String serviceName,
                          String operation,
                          String documentType,
                          String documentNumber,
                          String fullName,
                          String affiliationType,
                          String affiliationSubtype,
                          String filedNumber,
                          String errorCode,
                          String errorMessage) {
        save(serviceName, operation, documentType, documentNumber, fullName, affiliationType, affiliationSubtype,
                filedNumber, errorCode, errorMessage, null, -1);
    }

    private String toJson(Object response) {
        try {
            return response != null ? objectMapper.writeValueAsString(response) : null;
        } catch (Exception e) {
            return String.valueOf(response);
        }
    }
}


