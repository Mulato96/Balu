package com.gal.afiliaciones.application.service.positiva;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.domain.model.PositivaInsertionLog;
import com.gal.afiliaciones.infrastructure.dao.repository.positiva.PositivaInsertionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;
import jakarta.persistence.PersistenceException;

@Slf4j
@Service
@RequiredArgsConstructor
// this services is deprecated
public class PositivaLogService {

    private final PositivaInsertionLogRepository positivaInsertionLogRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_RESPONSE_BODY_LENGTH = 5000;
    private String safeTruncate(String value) {
        if (value == null) return null;
        return value.length() <= MAX_RESPONSE_BODY_LENGTH ? value : value.substring(0, MAX_RESPONSE_BODY_LENGTH);
    }

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
            log.info("[PositivaLogService] Saving integration log service={}, operation={}, docType={}, docNum={}, resultCode={}",
                    serviceName, operation, documentType, documentNumber, resultCode);
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
                    .responseBody(safeTruncate(responseBody))
                    .resultCode(resultCode)
                    .build();

            PositivaInsertionLog saved = positivaInsertionLogRepository.save(logEntity);
            log.info("[PositivaLogService] Saved integration log id={} service={} operation={}",
                    saved != null ? saved.getId() : null, serviceName, operation);
        } catch (DataAccessException | PersistenceException e) {
            // Nunca afectar el flujo principal: registrar el error y conservar el cuerpo en logs
            log.error("[PositivaLogService] DB error saving log service={} operation={} docType={} docNum={} - {}",
                    serviceName, operation, documentType, documentNumber, e.getMessage(), e);
            if (responseBody != null && !responseBody.isBlank()) {
                log.warn("[PositivaLogService] Persist fallback (response body in logs) service={} operation={} body={}",
                        serviceName, operation, responseBody);
            }
        } catch (Exception e) {
            // Nunca afectar el flujo principal por errores de logging
            log.error("[PositivaLogService] Unexpected error saving log (ignored) service={} operation={} err={}",
                    serviceName, operation, e.getMessage(), e);
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

        log.info("[PositivaLogService] saveHttp service={} operation={} status={}", serviceName, operation, httpStatus);
        save(serviceName, operation, documentType, documentNumber, fullName, affiliationType, affiliationSubtype,
                filedNumber, String.valueOf(httpStatus), null, safeTruncate(rawBody), httpStatus);
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
            log.info("[PositivaLogService] saveFromResponse service={} operation={} docType={} docNum={}",
                    serviceName, operation, documentType, documentNumber);
            String responseBody = safeTruncate(toJson(response));
            Integer result = PositivaResultUtil.deriveResultCode(response);
            save(serviceName, operation, documentType, documentNumber, fullName, affiliationType, affiliationSubtype,
                    filedNumber, null, null, responseBody, result);
        } catch (Exception e) {
            log.error("[PositivaLogService] Error in saveFromResponse (ignored) service={} operation={} err={}",
                    serviceName, operation, e.getMessage(), e);
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
        log.warn("[PositivaLogService] saveError service={} operation={} code={} msg={}",
                serviceName, operation, errorCode, errorMessage);
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


