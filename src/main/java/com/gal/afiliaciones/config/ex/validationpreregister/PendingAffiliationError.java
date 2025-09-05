package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PendingAffiliationError extends AffiliationsExceptionBase {

    private final OTPRequestDTO otpRequestDTO;

    public PendingAffiliationError(String message, OTPRequestDTO otpRequestDTO) {
        super(com.gal.afiliaciones.config.ex.Error.builder()
                .type(Error.Type.PENDING_AFFILIATION)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
        this.otpRequestDTO = otpRequestDTO;
    }


}
