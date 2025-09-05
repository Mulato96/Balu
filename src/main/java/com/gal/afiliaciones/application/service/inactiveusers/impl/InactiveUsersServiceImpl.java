package com.gal.afiliaciones.application.service.inactiveusers.impl;

import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.application.service.inactiveusers.IInactiveUsersService;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorValidateCode;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class InactiveUsersServiceImpl implements IInactiveUsersService {

    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final OtpService otpService;

    private static final String NAME_SCREEN_OTP = "Inactivar usuarios";


    @Transactional
    @Override
    public String validOtpAndActiveAccount(OTPRequestDTO request) {

        try {

            request.setNameScreen(NAME_SCREEN_OTP);
            otpService.validarOtp(request);
        } catch (Exception e) {
            String message = e.getMessage().equals(Constant.VALIDATION_CODE_HAS_EXPIRED) ? Constant.CERTIFICATE_CODE_VALIDATION_MESSAGE : e.getMessage();
            throw new ErrorValidateCode(message);
        }
        UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byIdentification(request.getCedula()))
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND));

        user.setStatusActive(true);
        user.setStatus(1L);
        user.setInactiveByPendingAffiliation(false);
        userPreRegisterRepository.save(user);
        return "Ya puedes continuar tu proceso de afiliación";
    }


}
