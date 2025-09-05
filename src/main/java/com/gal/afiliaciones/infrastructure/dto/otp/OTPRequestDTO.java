package com.gal.afiliaciones.infrastructure.dto.otp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.soap.SAAJResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OTPRequestDTO {
    private String cedula;
    private String otp;
    private String destinatario;
    private String nombre;
    private String nameScreen;
    private String typeDocument;
    private TypeUser typeUser;
}