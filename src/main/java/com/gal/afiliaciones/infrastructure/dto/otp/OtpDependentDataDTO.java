package com.gal.afiliaciones.infrastructure.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpDependentDataDTO {
    private OTPRequestDependentDTO requestDTO;
    private String firstName;
    private String surname;
}
