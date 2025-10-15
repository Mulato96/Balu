package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.config.ex.otp.OtpCodeInvalid;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.RequestCertificateBaluDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ResponseCertificateBaluDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;

public interface ConsultCertificateByUserService {

    ValidCodeCertificateDTO consultUser(String documentType, String documentNumber)
            throws MessagingException, IOException, IllegalAccessException;
    List<UserAffiliateDTO> validateCodeOTPCertificate(ValidCodeCertificateDTO consultUserDTO)
            throws MessagingException, IOException, OtpCodeInvalid, IllegalAccessException;

    void findUser(ValidCodeCertificateDTO validCodeCertificateDTO, String type);

    List<ResponseCertificateBaluDTO> generatecertificatebalu(RequestCertificateBaluDTO request);
}
