package com.gal.afiliaciones.infrastructure.dto.otp.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDataDTO {

    private String destinatario;
    private String plantilla;
    private Map<String, Object> datos;
    private byte[] adjunto;
    private List<MultipartFile> adjuntos;
    private String[] concopia;
    private String de;
}