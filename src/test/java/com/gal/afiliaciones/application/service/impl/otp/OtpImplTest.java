package com.gal.afiliaciones.application.service.impl.otp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.otp.OtpCodeInvalid;
import com.gal.afiliaciones.config.ex.validationpreregister.LoginAttemptsError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.otp.OtpCodeEntity;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.otp.OtpCodeRepository;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OtpDependentDataDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;


@ExtendWith(MockitoExtension.class)
class OtpImplTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;

    @Mock
    private ArlInformationDao arlInformationDao;

    @Mock
    private EmailService emailService;

    @Mock
    private AffiliationDependentRepository dependentRepository;

    @InjectMocks
    private OtpImpl otpService;

    private UserMain user;
    private OTPRequestDTO otpRequestDTO;
    private OtpCodeEntity otpCodeEntity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "longitudOtp", 6);
        ReflectionTestUtils.setField(otpService, "expiracionOtp", 5);
        ReflectionTestUtils.setField(otpService, "asuntoValidacionOTP", "Validación OTP para PRE_REGISTER");

        user = new UserMain();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setSurname("Doe");
        user.setGenerateAttempts(0);
        user.setValidAttempts(0);

        otpRequestDTO = new OTPRequestDTO();
        otpRequestDTO.setTypeDocument("CC");
        otpRequestDTO.setCedula("123456789");
        otpRequestDTO.setTypeUser(TypeUser.INT);
        otpRequestDTO.setDestinatario("test@example.com");
        otpRequestDTO.setNameScreen("TestScreen");

        otpCodeEntity = new OtpCodeEntity();
        otpCodeEntity.setNumberDocument("123456789");
        otpCodeEntity.setOtp("123456");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        otpCodeEntity.setExpiration(cal.getTime());
    }

    @Test
    @DisplayName("generarOtp - Success")
    void generarOtp_Success() throws Exception {
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByNumberDocument(anyString())).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(new ArlInformation()));
        doNothing().when(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());

        OTPDataResponseDTO response = otpService.generarOtp(otpRequestDTO);

        assertNotNull(response);
        assertEquals("Ok", response.getMensaje());
        assertNotNull(response.getExpiracion());
        verify(otpCodeRepository, times(1)).save(any(OtpCodeEntity.class));
        verify(userPreRegisterRepository, times(1)).save(any(UserMain.class));
        verify(emailService, times(1)).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    @DisplayName("generarOtp - User Not Found")
    void generarOtp_UserNotFound() {
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> otpService.generarOtp(otpRequestDTO));
    }

    @Test
    @DisplayName("generarOtp - Incorrect Email")
    void generarOtp_IncorrectEmail() {
        user.setEmail("another@example.com");
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        assertThrows(AffiliationError.class, () -> otpService.generarOtp(otpRequestDTO));
    }

    @Test
    @DisplayName("generarOtp - Max Generate Attempts Reached")
    void generarOtp_MaxGenerateAttemptsReached() {
        user.setGenerateAttempts(5);
        user.setGenerateOutTime(LocalDateTime.now().plusMinutes(30));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        assertThrows(LoginAttemptsError.class, () -> otpService.generarOtp(otpRequestDTO));
    }

    @Test
    @DisplayName("validarOtp - Success")
    void validarOtp_Success() {
        when(otpCodeRepository.findByNumberDocument(otpRequestDTO.getCedula())).thenReturn(Optional.of(otpCodeEntity));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        otpRequestDTO.setOtp("123456");

        OTPDataDTO response = otpService.validarOtp(otpRequestDTO);

        assertEquals(Constant.VALIDATION_SUCCESSFUL, response.getMensaje());
        ArgumentCaptor<UserMain> userCaptor = ArgumentCaptor.forClass(UserMain.class);
        verify(userPreRegisterRepository, times(2)).save(userCaptor.capture());
        UserMain savedUser = userCaptor.getValue();
        assertNull(savedUser.getGenerateOutTime());
        assertEquals(0, savedUser.getGenerateAttempts());
    }

    @Test
    @DisplayName("validarOtp - Invalid Code")
    void validarOtp_InvalidCode() {
        when(otpCodeRepository.findByNumberDocument(otpRequestDTO.getCedula())).thenReturn(Optional.of(otpCodeEntity));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        otpRequestDTO.setOtp("654321");

        assertThrows(OtpCodeInvalid.class, () -> otpService.validarOtp(otpRequestDTO));
    }

    @Test
    @DisplayName("validarOtp - Expired Code")
    void validarOtp_ExpiredCode() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);
        otpCodeEntity.setExpiration(cal.getTime());
        when(otpCodeRepository.findByNumberDocument(otpRequestDTO.getCedula())).thenReturn(Optional.of(otpCodeEntity));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        otpRequestDTO.setOtp("123456");

        assertThrows(OtpCodeInvalid.class, () -> otpService.validarOtp(otpRequestDTO));
        verify(otpCodeRepository, times(1)).delete(otpCodeEntity);
    }
    
    @Test
    @DisplayName("validarOtp - Max Validation Attempts Reached")
    void validarOtp_MaxValidationAttemptsReached() {
        user.setValidAttempts(5);
        user.setValidOutTime(LocalDateTime.now().minusMinutes(1)); // Locked out but within the window
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        assertThrows(LoginAttemptsError.class, () -> otpService.validarOtp(otpRequestDTO));
    }

    @Test
    @DisplayName("generateOtpDependent - Success")
    void generateOtpDependent_Success() throws Exception {
        OtpDependentDataDTO dependentDataDTO = new OtpDependentDataDTO();
        OTPRequestDependentDTO requestDependentDTO = new OTPRequestDependentDTO();
        requestDependentDTO.setCedula(otpRequestDTO.getCedula());
        requestDependentDTO.setDestinatario(otpRequestDTO.getDestinatario());
        requestDependentDTO.setNameScreen(otpRequestDTO.getNameScreen());
        dependentDataDTO.setRequestDTO(requestDependentDTO);
        dependentDataDTO.setFirstName("Jane");
        dependentDataDTO.setSurname("Doe");

        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByNumberDocument(anyString())).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(new ArlInformation()));
        doNothing().when(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());

        OTPDataResponseDTO response = otpService.generateOtpDependent(dependentDataDTO);

        assertNotNull(response);
        assertEquals("Ok", response.getMensaje());
        verify(otpCodeRepository, times(1)).save(any(OtpCodeEntity.class));
        verify(emailService, times(1)).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    @DisplayName("validateOtpDependent - Success")
    void validateOtpDependent_Success() {
        OTPRequestDependentDTO request = new OTPRequestDependentDTO();
        request.setCedula("123456789");
        request.setOtp("123456");

        when(otpCodeRepository.findByNumberDocument(request.getCedula())).thenReturn(Optional.of(otpCodeEntity));

        OTPDataDTO response = otpService.validateOtpDependent(request);

        assertEquals(Constant.VALIDATION_SUCCESSFUL, response.getMensaje());
    }

    @Test
    @DisplayName("validateOtpDependent - Invalid Code")
    void validateOtpDependent_InvalidCode() {
        OTPRequestDependentDTO request = new OTPRequestDependentDTO();
        request.setCedula("123456789");
        request.setOtp("654321");

        when(otpCodeRepository.findByNumberDocument(request.getCedula())).thenReturn(Optional.of(otpCodeEntity));

        assertThrows(OtpCodeInvalid.class, () -> otpService.validateOtpDependent(request));
    }

    @Test
    @DisplayName("updateAttemptsAndTime - Resets User Attempts")
    void updateAttemptsAndTime_ResetsUserAttempts() {
        otpService.updateAttemptsAndTime();

        ArgumentCaptor<UserMain> userCaptor = ArgumentCaptor.forClass(UserMain.class);
        verify(userPreRegisterRepository, times(1)).save(userCaptor.capture());

        UserMain savedUser = userCaptor.getValue();
        assertNull(savedUser.getGenerateOutTime());
        assertEquals(0, savedUser.getGenerateAttempts());
        assertNull(savedUser.getValidOutTime());
        assertEquals(0, savedUser.getValidAttempts());
    }
}