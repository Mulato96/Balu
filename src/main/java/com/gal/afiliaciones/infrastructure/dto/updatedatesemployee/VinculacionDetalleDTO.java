package com.gal.afiliaciones.infrastructure.dto.updatedatesemployee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VinculacionDetalleDTO {

    // Datos de la persona/empresa
    private String tipoDocumentoIdentificacion;
    private String numeroIdentificacion;
    private Integer digitoVerificacion;
    private String nombreCompletoORazonSocial;

    // Datos del Representante Legal (si aplica)
    private String tipoDocumentoRepLegal;
    private String numeroIdentificacionRepLegal;
    private String nombreCompletoRepLegal;

    // Datos de Contacto
    private String direccionCompleta;
    private String telefono1;
    private String telefono2;
    private String correoElectronico;

    // Fechas
    private LocalDate fechaAfiliacion;
    private LocalDate fechaInicioCobertura;
}