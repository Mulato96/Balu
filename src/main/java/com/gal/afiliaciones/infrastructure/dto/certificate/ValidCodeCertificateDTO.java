package com.gal.afiliaciones.infrastructure.dto.certificate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidCodeCertificateDTO {

    @NotNull(message = "This field cannot be null")
    private String identificationType;
    @NotNull(message = "This field cannot be null")
    private String identification;
    @NotNull(message = "This field cannot be null")
    private DataCertificateDTO details;
    @NotNull(message = "This field cannt be null")
    private String code;
    private String firstName;
    private String surname;
}
