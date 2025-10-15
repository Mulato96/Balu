package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCertificateBaluDTO {

    private String identificationType;
    private String identification;

}
