package com.gal.afiliaciones.config.ex.otp;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class OtpCodeExpired extends AffiliationsExceptionBase {
    public OtpCodeExpired(String message){
        super(Error.builder()
                .type(Error.Type.INVALID_CODE_OTP)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }

}
