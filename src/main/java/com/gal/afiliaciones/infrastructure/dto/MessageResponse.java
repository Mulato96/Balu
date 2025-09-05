package com.gal.afiliaciones.infrastructure.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class MessageResponse {
    private HttpStatus status;
    private int codeStatus;
    private String message;
    private Object object;
}
