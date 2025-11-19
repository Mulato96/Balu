package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateWorkerDTO {

    private String tipoDoc;
    private String idPersona;
    private String tipoVinculacion;
    private String nombreEmpresa;
    private String certificado;

}
