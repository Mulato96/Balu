package com.gal.afiliaciones.infrastructure.dto.certificate;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateWorkerByEmployerResponse {

    private String tpDocEmpresa;
    private String idEmpresa;
    private List<CertificateWorkerDTO> certificados;

}
