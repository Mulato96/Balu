package com.gal.afiliaciones.application.service.impl.otp;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.otp.OtpCodeInvalid;
import com.gal.afiliaciones.config.ex.validationpreregister.LoginAttemptsError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.otp.OtpCodeEntity;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.otp.OtpCodeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OtpDependentDataDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import com.gal.afiliaciones.infrastructure.utils.Otp;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
@RequiredArgsConstructor
@Slf4j
public class OtpImpl implements OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String USER = "usuario";
    private static final String CODE = "codigo";
    private static final String VALIDITY = "vigencia";
    private static final String OK = "Ok";
    private static final Long TIME_GENERATE = 30l;
    private static final String INVALID_EMAIL_MESSAGE = "El correo electronico no es correcto";
    private static final String STRING_LITERAL = "string";
    private static final String DIGIT_PREFIX = "digit";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_DOCUMENT_TYPE = "documentType";
    private static final String KEY_DOCUMENT_NUMBER = "documentNumber";
    private static final String KEY_HAS_OTP = "hasOtp";
    @Value("${pqrd.otp.longitud}")
    private int longitudOtp;
    @Value("${pqrd.otp.expiracion}")
    private int expiracionOtp;
    @Value("${spring.mail.otp.asunto.validacionOTP}")
    private String asuntoValidacionOTP;
    private final EmailService emailService;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final ArlInformationDao arlInformationDao;
    private final List<String> list = List.of("Recuperación de contraseña", "Registro");
    private final AffiliationDependentRepository dependentRepository;

    @Override
    public OTPDataResponseDTO generarOtp(OTPRequestDTO requestDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase {

        //busca el usuario pre registrado en la bd
        UserMain user = findUserMain(requestDTO.getTypeDocument(), requestDTO.getCedula(), requestDTO.getTypeUser());

        String  email = requestDTO.getDestinatario();

        //valida que el email del token, el email del request, y el email de bd sea el mismo
        if(!email.equals(user.getEmail()) || !user.getEmail().equals(requestDTO.getDestinatario()))
            throw new AffiliationError(INVALID_EMAIL_MESSAGE);

        OtpCodeEntity otpCodeEntity = findByNumberDocument(requestDTO.getCedula());

        if (otpCodeEntity != null) {
            deleteOtp(otpCodeEntity);
        }

        String otp = generarCodigoAlfanumerico();

        Calendar tiempoExpiracion = Calendar.getInstance();
        tiempoExpiracion.add(Calendar.MINUTE, expiracionOtp);


         OtpCodeEntity otpDataDTO = mapOtp(requestDTO.getCedula());
         otpDataDTO.setExpiration(tiempoExpiracion.getTime());
         otpDataDTO.setOtp(otp);
         otpCodeRepository.save(otpDataDTO);

        EmailDataDTO emailDataDTO = new EmailDataDTO();
        emailDataDTO.setDestinatario(requestDTO.getDestinatario());
        emailDataDTO.setPlantilla(Constant.PLANTILLA_OTP);


        Map<String, Object> datos = Otp.convertirDTOaMap(otpDataDTO);
        datos.put(USER, user.getFirstName() + " " + user.getSurname());
        datos.put(CODE, otp);
        datos.put(VALIDITY, expiracionOtp);

        if(requestDTO.getNameScreen() == null || requestDTO.getNameScreen().isEmpty() || requestDTO.getNameScreen().equals(STRING_LITERAL))
            requestDTO.setNameScreen(Constant.PRE_REGISTER);

        datos.put("nameScreen", requestDTO.getNameScreen());

        emailDataDTO.setDatos(datos);

        for (int i = 0; i < otp.length(); i++) {
            datos.put(DIGIT_PREFIX + (i + 1), String.valueOf(otp.charAt(i)));
        }

        int generateAttemps = user.getGenerateAttempts()!=null ? user.getGenerateAttempts(): 0;
        if (generateAttemps < 5) {
            user.setGenerateAttempts(generateAttemps + 1);
        }
        if (generateAttemps >= Constant.MAX_GENERATE_ATTEMPTS && user.getGenerateOutTime() == null){
            user.setGenerateOutTime(LocalDateTime.now());
        }

        user.setCodeOtp(otp);

        log.info(String.valueOf(user.getGenerateOutTime()));
        if (user.getGenerateOutTime() != null && user.getGenerateOutTime().plusMinutes(TIME_GENERATE).isAfter(LocalDateTime.now())) {
            Duration duration = Duration.between(LocalDateTime.now(), user.getGenerateOutTime().plusMinutes(TIME_GENERATE));
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            Object message = String.format("%d horas y %d minutos.", hours, minutes);
            throw new LoginAttemptsError(message.toString());
        }
        ArlInformation arlInformation = getArlInformation();
        String emailSubject = asuntoValidacionOTP.replace(Constant.PRE_REGISTER, requestDTO.getNameScreen());
        emailService.sendSimpleMessage(emailDataDTO, emailSubject.concat(" "+arlInformation.getName()));

        Date expiracion = otpDataDTO.getExpiration() != null ? otpDataDTO.getExpiration() : null;
        if(user.getGenerateOutTime() != null && user.getGenerateOutTime().isBefore(LocalDateTime.now())) {
            user.setGenerateAttempts(0);
            user.setGenerateOutTime(null);
        }
        userPreRegisterRepository.save(user);

        return OTPDataResponseDTO.builder().expiracion(expiracion).mensaje(OK).build();
    }

    @Override
    public OTPDataDTO validarOtp(OTPRequestDTO request) {
        OtpCodeEntity otpDataDTO = findByNumberDocument(request.getCedula());

        UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byUsername(structureUserName(request.getTypeDocument(), request.getCedula(), request.getTypeUser()))).orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_LOGIN));
        int validAttemps = user.getValidAttempts() != null ? user.getValidAttempts() : 0;
        if (validAttemps >= 5 && user.getValidOutTime()==null) {
            user.setValidOutTime(LocalDateTime.now());
        }else{
            user.setValidAttempts(validAttemps + 1);
        }
        if(user.getValidOutTime() != null) {
            LocalDateTime newDateTimeAttemp = user.getValidOutTime().plusMinutes(TIME_GENERATE);
            if (validAttemps >= Constant.MAX_GENERATE_ATTEMPTS && newDateTimeAttemp.isBefore(LocalDateTime.now())) {
                user.setValidOutTime(null);
                user.setValidAttempts(0);
            }
        }
        userPreRegisterRepository.save(user);
        if (user.getValidOutTime() != null && user.getValidOutTime().plusMinutes(TIME_GENERATE).isAfter(LocalDateTime.now())) {
            Duration duration = Duration.between(LocalDateTime.now(), user.getValidOutTime().plusMinutes(TIME_GENERATE));
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            Object message = String.format("%d horas y %d minutos.", hours, minutes);
            throw new LoginAttemptsError(message.toString());
        }
        if (otpDataDTO == null || !otpDataDTO.getOtp().equals(request.getOtp())) {
            throw new OtpCodeInvalid(Constant.VALIDATION_CODE_INCORRECT);
        }

        if (!isOtpValid(otpDataDTO)) {
            deleteOtp(otpDataDTO);
            throw new OtpCodeInvalid(Constant.VALIDATION_CODE_HAS_EXPIRED);
        }

        user.setGenerateOutTime(null);
        user.setGenerateAttempts(0);
        user.setValidOutTime(null);
        user.setValidAttempts(0);
        userPreRegisterRepository.save(user);

        return OTPDataDTO.builder()
                .mensaje(Constant.VALIDATION_SUCCESSFUL)
                .build();
    }

    @Override
    public OTPDataResponseDTO generateOtpDependent(OtpDependentDataDTO otpDependentDataDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase {

        //busca el usuario en bd
        Optional<UserMain> optionalUser = findUserMain(otpDependentDataDTO.getRequestDTO().getCedula());
        Optional<AffiliationDependent> optionalAffiliationDependent = findAffiliationDependent(otpDependentDataDTO.getRequestDTO().getCedula());
        String email = null;

        if(optionalUser.isPresent())
            email = optionalUser.get().getEmail();
        else if(optionalAffiliationDependent.isPresent())
            email = optionalAffiliationDependent.get().getEmail();

        //compara que el email del usuario pre registrado sea el mismo del request, no requiere token por eso no compara
        if(!otpDependentDataDTO.getRequestDTO().getDestinatario().equals(email))

            throw new AffiliationError(INVALID_EMAIL_MESSAGE);

        OtpCodeEntity verificarOtpDate = findByNumberDocument(otpDependentDataDTO.getRequestDTO().getCedula());
        if (verificarOtpDate != null) {
           deleteOtp(verificarOtpDate);
        }

        String otp = generarCodigoAlfanumerico();

        Calendar tiempoExpiracion = Calendar.getInstance();
        tiempoExpiracion.add(Calendar.MINUTE, expiracionOtp);

        OtpCodeEntity otpDataDTO = mapOtp(otpDependentDataDTO.getRequestDTO().getCedula());
        otpDataDTO.setExpiration(tiempoExpiracion.getTime());
        otpDataDTO.setOtp(otp);
        otpCodeRepository.save(otpDataDTO);

        EmailDataDTO emailDataDTO = new EmailDataDTO();
        emailDataDTO.setDestinatario(otpDependentDataDTO.getRequestDTO().getDestinatario());
        emailDataDTO.setPlantilla(Constant.PLANTILLA_OTP);

        Map<String, Object> datos = Otp.convertirDTOaMap(otpDataDTO);
        datos.put(USER, otpDependentDataDTO.getFirstName() + " " + otpDependentDataDTO.getSurname());
        datos.put(CODE, otp);
        datos.put(VALIDITY, expiracionOtp);

        if(otpDependentDataDTO.getRequestDTO().getNameScreen() == null || otpDependentDataDTO.getRequestDTO().getNameScreen().isEmpty() || otpDependentDataDTO.getRequestDTO().getNameScreen().equals(STRING_LITERAL))
            otpDependentDataDTO.getRequestDTO().setNameScreen(Constant.PRE_REGISTER);

        datos.put("nameScreen", otpDependentDataDTO.getRequestDTO().getNameScreen());

        emailDataDTO.setDatos(datos);

        for (int i = 0; i < otp.length(); i++) {
            datos.put(DIGIT_PREFIX + (i + 1), String.valueOf(otp.charAt(i)));
        }

        ArlInformation arlInformation = getArlInformation();
        String emailSubject = asuntoValidacionOTP.replace(Constant.PRE_REGISTER, otpDependentDataDTO.getRequestDTO().getNameScreen());
        emailService.sendSimpleMessage(emailDataDTO, emailSubject.concat(" "+arlInformation.getName()));

        Date expiracion = otpDataDTO.getExpiration() != null ? otpDataDTO.getExpiration() : null;

        return OTPDataResponseDTO.builder().expiracion(expiracion).mensaje(OK).build();    }

    @Override
    public OTPDataDTO validateOtpDependent(OTPRequestDependentDTO request) {
        OtpCodeEntity otpDataDTO = findByNumberDocument(request.getCedula());

        if (otpDataDTO == null || !otpDataDTO.getOtp().equals(request.getOtp())) {
            throw new OtpCodeInvalid(Constant.VALIDATION_CODE_INCORRECT);
        }

        if (!isOtpValid(otpDataDTO)) {
           deleteOtp(otpDataDTO);
           throw new OtpCodeInvalid(Constant.VALIDATION_CODE_HAS_EXPIRED);
        }

        return OTPDataDTO.builder()
                .mensaje(Constant.VALIDATION_SUCCESSFUL)
                .build();
    }

    private boolean isOtpValid(OtpCodeEntity otpDataDTO) {
        Date currentTime = new Date();
        return currentTime.compareTo(otpDataDTO.getExpiration()) <= 0;
    }

    private String generarCodigoAlfanumerico() {
        String resultado = "";
        while (resultado.length() < longitudOtp) {
            byte[] randomBytes = new byte[8]; // Generamos un poco más por seguridad
            secureRandom.nextBytes(randomBytes);

            String parcial = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes)
                    .replaceAll("[^a-zA-Z0-9]", "");

            resultado += parcial;
        }

        return resultado.substring(0, longitudOtp);
    }

    @Scheduled(cron = "0 0 0,12 * * ?")
    public void updateAttemptsAndTime() {
        UserMain user = new UserMain();
        user.setGenerateOutTime(null);
        user.setGenerateAttempts(0);
        user.setValidOutTime(null);
        user.setValidAttempts(0);
        userPreRegisterRepository.save(user);
    }

    private ArlInformation getArlInformation(){
        List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();
        return allArlInformation.get(0);
    }

    private String structureUserName(String typeDocument, String document, TypeUser typeUser){
        return typeDocument + "-" + document + "-" + typeUser;
    }

    private String getEmailUserPreRegister() {
        try{
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return jwt.getClaim("email");
        }catch (Exception e){
            log.error("Error method getEmailUserPreRegister : {}", e.getMessage());
            throw new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
    }

    private UserMain findUserMain(String typeDocument, String numberDocument, TypeUser typeUser){

        return userPreRegisterRepository.findOne(UserSpecifications.byUsername(structureUserName(typeDocument, numberDocument, typeUser)))
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_LOGIN));
    }

    private Optional<UserMain> findUserMain(String document){
        return userPreRegisterRepository.findOne(UserSpecifications.byIdentification(document));
    }

    private Optional<AffiliationDependent> findAffiliationDependent(String document){
        return dependentRepository.findByIdentificationDocumentNumber(document);
    }

    private OtpCodeEntity findByNumberDocument(String numberDocument){
        return otpCodeRepository.findByNumberDocument(numberDocument).orElse(null);
    }

    private void deleteOtp(OtpCodeEntity otpCodeEntity){
        otpCodeRepository.delete(otpCodeEntity);
    }

    private OtpCodeEntity mapOtp(String numberDocument){

        OtpCodeEntity otpCodeEntity = new OtpCodeEntity();
        otpCodeEntity.setNumberDocument(numberDocument);
        return otpCodeEntity;
    }

    /**
     * TEST ONLY - Retrieves OTP code for testing purposes.
     * This method should only be called after host validation in controller.
     * 
     * @param documentType Document type (e.g., "CC")
     * @param documentNumber Document number
     * @param typeUser User type (can be null or "null")
     * @return Map containing OTP code and user information
     */
    @Override
    public Map<String, Object> getOtpForTesting(String documentType, String documentNumber, String typeUser) {
        try {
            // Normalize typeUser (convert "null" string to null)
            String normalizedTypeUser = (typeUser == null || "null".equalsIgnoreCase(typeUser)) ? null : typeUser;
            
            // Build username in the same format as the application uses
            String username = documentType + "-" + documentNumber + "-" + normalizedTypeUser;
            
            // Find user by username
            UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byUsername(username))
                    .orElseThrow(() -> new UserNotFoundInDataBase("User not found with username: " + username));
            
            // Get OTP from user
            String otpCode = user.getCodeOtp();
            
            if (otpCode == null || otpCode.isEmpty()) {
                return Map.of(
                    KEY_SUCCESS, false,
                    KEY_MESSAGE, "No OTP code found for this user. Generate OTP first.",
                    "username", username,
                    KEY_DOCUMENT_TYPE, documentType,
                    KEY_DOCUMENT_NUMBER, documentNumber,
                    KEY_HAS_OTP, false
                );
            }
            
            return Map.of(
                KEY_SUCCESS, true,
                "otpCode", otpCode,
                "username", username,
                KEY_DOCUMENT_TYPE, documentType,
                KEY_DOCUMENT_NUMBER, documentNumber,
                "email", user.getEmail() != null ? user.getEmail() : "N/A",
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "surname", user.getSurname() != null ? user.getSurname() : "",
                KEY_HAS_OTP, true,
                KEY_MESSAGE, "OTP retrieved successfully"
            );
            
        } catch (UserNotFoundInDataBase e) {
            return Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, e.getMessage(),
                KEY_DOCUMENT_TYPE, documentType,
                KEY_DOCUMENT_NUMBER, documentNumber,
                KEY_HAS_OTP, false
            );
        } catch (Exception e) {
            log.error("Error retrieving OTP for testing: {}", e.getMessage(), e);
            return Map.of(
                KEY_SUCCESS, false,
                KEY_MESSAGE, "Error retrieving OTP: " + e.getMessage(),
                KEY_DOCUMENT_TYPE, documentType,
                KEY_DOCUMENT_NUMBER, documentNumber,
                KEY_HAS_OTP, false
            );
        }
    }
}
