package com.gal.afiliaciones.application.service.inactiveusers;

import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;

public interface IInactiveUsersService {

    String validOtpAndActiveAccount(OTPRequestDTO request);
}
