package com.gal.afiliaciones.config.ex.otp;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OtpCodeInvalid extends AffiliationsExceptionBase {
    public OtpCodeInvalid(String message){
        super(Error.builder()
                .type(Error.Type.INVALID_CODE_OTP)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }

}
