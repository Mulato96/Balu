package com.gal.afiliaciones.application.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.converters.UserConverter;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.*;
import com.gal.afiliaciones.config.mapper.UpdatePreRegisterMapper;
import com.gal.afiliaciones.config.mapper.UserMapper;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Arl;
import com.gal.afiliaciones.domain.model.Gender;
import com.gal.afiliaciones.domain.model.SystemParam;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.GenderRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.systemparam.SystemParamRepository;
import com.gal.afiliaciones.infrastructure.dto.ExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.ResponseUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateCredentialDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateEmailExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdatePasswordDTO;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserNameDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.document.ValidationDocument;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.keycloak.jose.jwk.JWK.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreRegisterServiceImpl implements IUserRegisterService {
    private final GenericWebClient webClient;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final KeycloakServiceImpl keycloakServiceImpl;
    private final UserStatusUpdateService userStatusUpdateService;
    private final RestTemplate restTemplate;
    private final AffiliationProperties affiliationProperties;
    private final GenderRepository genderRepository;
    private final SystemParamRepository paramRepository;
    private final HttpServletRequest request;
    private final CollectProperties properties;
    private final SendEmails sendEmails;
    private final OtpService otpService;
    private final KeycloakService keycloakService;

    private static final Logger logger = LoggerFactory.getLogger(UserPreRegisterServiceImpl.class);
    private static final String NAME_SCREEN_OTP = "Usuario pre-registrado";
    private static final String EXT = "EXT";

    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliateRepository affiliateRepository;

    private final UpdatePreRegisterMapper updatePreRegisterMapper;
    private final UserMapper userMapper;
    private final ArlRepository arlRepository;

    @Override
    @Transactional
    public ResponseUserDTO userPreRegister(UserPreRegisterDto userPreRegisterDto) throws MessagingException, IOException, IllegalAccessException {
        ResponseUserDTO responseUserDTO = new ResponseUserDTO();

        Specification<UserMain> emailSpec = UserSpecifications.byEmail(userPreRegisterDto.getEmail());
        Specification<UserMain> phoneSpec = UserSpecifications.byPhone1(userPreRegisterDto.getPhoneNumber());
        Specification<UserMain> usernameSpec = UserSpecifications.byUsername(structureUsername(userPreRegisterDto.getIdentificationType(), userPreRegisterDto.getIdentification()));

        if (!ValidationDocument.isValidDocument(userPreRegisterDto.getIdentificationType())) {
            throw new ErrorDocumentType(Constant.INVALID_DOCUMENT_TYPE);
        }

        if (!ValidationDocument.isValid(userPreRegisterDto.getIdentification(), userPreRegisterDto.getIdentificationType())) {
            throw new ErrorDocumentConditions(Constant.INVALID_DOCUMENT_CONDITIONS);
        }

        if (iUserPreRegisterRepository.count(emailSpec) > 0) {
            throw new EmailAlreadyExists(Constant.EMAIL_ALREADY_EXISTS);
        }
        if (iUserPreRegisterRepository.count(phoneSpec) > 0) {
            throw new PhoneAlreadyExists(Constant.PHONE1_ALREADY_EXISTS);
        }
        if (iUserPreRegisterRepository.count(usernameSpec) > 0) {
            throw new UserAndTypeAlreadyExists(Constant.USER_AND_TYPE_ALREADY_EXISTS);
        }
        
        String numIdentification = userPreRegisterDto.getIdentification();
        
        int dvNit = this.calculateModulo11DV(numIdentification); 
        

        UserMain userRegister = new UserMain();
        userRegister.setStatusPreRegister(false);
        userRegister.setStatusActive(false);
        userRegister.setStatus(2L);
        userRegister.setValidAttempts(0);
        userRegister.setGenerateAttempts(0);
        userRegister.setLastUpdate(LocalDateTime.now());
        int age = calculateAge(userPreRegisterDto.getDateBirth());
        BeanUtils.copyProperties(userPreRegisterDto, userRegister);
        BeanUtils.copyProperties(userPreRegisterDto.getAddress(), userRegister);
        userRegister.setAge(age);
        userRegister.setPhoneNumber2(userPreRegisterDto.getPhone2());
        userRegister.setUserType(2L);
        userRegister.setIsPasswordExpired(false);
        userRegister.setUserName(structureUsername(userPreRegisterDto.getIdentificationType(),userPreRegisterDto.getIdentification()));
        userRegister.setVerificationDigit(dvNit);   
        Long idNationality = null;
        userPreRegisterDto.setUserName(structureUsername(userPreRegisterDto.getIdentificationType(),userPreRegisterDto.getIdentification()));
        try {
            if (userPreRegisterDto.getNationality() != null)
                idNationality = Long.parseLong(userPreRegisterDto.getNationality());
        } catch (Exception ex){
            logger.error("Error al convertir la nacionalidad a Long: {}", ex.getMessage());
        }
        userRegister.setNationality(idNationality);
        userRegister = iUserPreRegisterRepository.save(userRegister);

        responseUserDTO.setAddress(userPreRegisterDto.getAddress());
        BeanUtils.copyProperties(userRegister, responseUserDTO);
        OTPRequestDTO otpRequestDTO = new OTPRequestDTO();
        otpRequestDTO.setTypeDocument(userRegister.getIdentificationType());
        otpRequestDTO.setTypeUser(TypeUser.EXT);
        otpRequestDTO.setCedula(userRegister.getIdentification());
        otpRequestDTO.setDestinatario(userRegister.getEmail());
        otpRequestDTO.setNombre(userRegister.getFirstName());
        otpRequestDTO.setNameScreen(NAME_SCREEN_OTP);
        responseUserDTO.setOtpData(otpService.generarOtp(otpRequestDTO));
        keycloakServiceImpl.createUser(userPreRegisterDto);

        return responseUserDTO;
    }

    @Override
    public UserPreRegisterDto consultUser(String identificationType, String identification) {
        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(identificationType, identification);
        Optional<UserMain> user = iUserPreRegisterRepository.findOne(spec);
        if (user.isPresent()) {
            UserPreRegisterDto dto = new UserPreRegisterDto();
            BeanUtils.copyProperties(user.get(), dto);
            dto.setUserFromRegistry(false);
            return dto;
        }

        /*
         * If it is not found and the document is CC, consult the National Registry
         */
        if (identificationType.equals(Constant.CC)){
            UserDtoApiRegistry apiRegistry = searchUserInNationalRegistry(identification);
            if (apiRegistry.getFirstName()!=null) {
                UserPreRegisterDto userRegistryData = UserPreRegisterDto.builder().
                        identificationType(identificationType).
                        identification(identification).
                        firstName(apiRegistry.getFirstName()).
                        surname(apiRegistry.getSurname()).
                        phoneNumber("").build();
                BeanUtils.copyProperties(apiRegistry, userRegistryData);
                userRegistryData.setSex(findGenderByDescription(apiRegistry.getGender()));
                userRegistryData.setUserFromRegistry(true);
                userRegistryData.setStatusPreRegister(false);
                return userRegistryData;
            }
        }

        UserPreRegisterDto newUser = UserPreRegisterDto.builder().
                identificationType(identificationType).
                identification(identification).
                firstName("").
                surname("").
                phoneNumber("").build();
        newUser.setUserFromRegistry(false);
        newUser.setStatusPreRegister(false);
        return newUser;

    }

    @Override
    public Map<String, Object> registerPassword(UpdatePasswordDTO registerPassword) {

        Specification<UserMain> spec = UserSpecifications.byUsername(registerPassword.getDocumentType() + "-"
                + registerPassword.getDocumentNumber() + "-" + registerPassword.getTypeUser());

        UserMain user = iUserPreRegisterRepository.findOne(spec).orElseThrow(() ->
         new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));


        if(registerPassword.getContext() != null
                && registerPassword.getContext().equals("reset_password")
                && validPasswordOld( user.getUserName(), registerPassword.getPassword()))
            throw new UserNotRegisteredException(Constant.PASSWORD_EQUALS_TO_CURRENT);


        if(user.getCodeOtp() == null || !user.getCodeOtp().equals(registerPassword.getCodeOtp()))
            throw new AffiliationError("Error al validar el usuario");


        if (user.getIsImport() == Boolean.TRUE){
            user.setAssignedPassword(true);
        }
        user.setIsImport(false); // Se pasa en false por que en cualquier caso que se le asigne la contraseña seria false
        // Actualiza estados de usuario pre registrado
        user.setStatusActive(true);
        user.setStatus(1L);
        user.setStatusPreRegister(true);
        user.setLastPasswordUpdate(LocalDateTime.now());
        user.setIsPasswordExpired(false);
        user.setCodeOtp(null);
        iUserPreRegisterRepository.save(user);

        if (validatePassword(user, registerPassword.getPassword())) {
            throw new ErrorContainDataPersonal(Constant.PASSWORD_NOT_CONTAIN_DATA_PERSONAL);
        }

        //Envio correo bienvenida despues del preregistro
        sendEmails.emailWelcomeRegister(user);

        return keycloakServiceImpl.updateUser(user.getEmail(), registerPassword.getPassword());
    }

    @Override
    public Optional<UserDtoApiRegistry> getByIdentification(String identification) {
        return webClient.getByIdentification(identification);
    }

    @Transactional
    @Override
    public void updateStatusPreRegister(String identificationType, String identification) {
        Optional<UserMain> userOptional = iUserPreRegisterRepository.findOne(hasDocumentTypeAndNumber(identificationType, identification));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        UserMain user = userOptional.get();
        user.setLastAffiliationAttempt(LocalDateTime.now());
        user.setStatusPreRegister(true);
        iUserPreRegisterRepository.save(user);


    }

    @Transactional
    @Override
    public void updateStatusActive(String identificationType, String identification) {
        Optional<UserMain> userOptional = iUserPreRegisterRepository.findOne(hasDocumentTypeAndNumber(identificationType, identification));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        UserMain user = userOptional.get();
        user.setStatusActive(true);
        user.setStatus(1L);
        iUserPreRegisterRepository.save(user);
    }

    @Transactional
    @Override
    public void updateStatusInactive(String identificationType, String identification) {
        Optional<UserMain> userOptional = iUserPreRegisterRepository.findOne(hasDocumentTypeAndNumber(identificationType, identification));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        UserMain user = userOptional.get();
        user.setStatusInactiveSince(LocalDateTime.now());
        user.setStatusActive(false);
        user.setStatus(2L);
        iUserPreRegisterRepository.save(user);
    }

    @Override
    public void updateStatusStartAffiliation(String identificationType, String identification) {
        UserMain user = iUserPreRegisterRepository.findOne(hasDocumentTypeAndNumber(identificationType, identification))
                .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
        user.setStatusStartAfiiliate(true);
        iUserPreRegisterRepository.save(user);

    }

    @Transactional
    @Override
    public void updateStatusInactiveFalse(String identificationType, String identification) {
        Optional<UserMain> userOptional = iUserPreRegisterRepository.findOne(hasDocumentTypeAndNumber(identificationType, identification));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        UserMain user = userOptional.get();
        user.setStatusInactiveSince(LocalDateTime.now());
        user.setStatusActive(true);
        user.setStatus(1L);
        iUserPreRegisterRepository.save(user);
    }

    @Override
    public UserMain consultUserByIdentification(String identification) {
        Specification<UserMain> spec = UserSpecifications.byEmail(identification);
        Optional<UserMain> user = iUserPreRegisterRepository.findOne(spec);
        if (user.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        return user.get();
    }

    @Override
    public boolean validateEmployerRangeNaturalPerson(String documentType, String documentNumber) {
        if (documentNumber == null || documentNumber.isEmpty()) {
            return false;
        }

        documentNumber = documentNumber.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        return switch (documentType) {
            case Constant.CE -> isValidCEStructure(documentNumber);
            case Constant.PA -> isValidPAStructure(documentNumber);
            case Constant.NI -> isValidNumeric(documentNumber) && isValidNIT(documentNumber);
            case Constant.TI -> isValidTIStructure(documentNumber);
            case Constant.CD -> isValidCDStructure(documentNumber);
            case Constant.PE -> isValidPEStructure(documentNumber);
            case Constant.N, Constant.CC -> isValidNumeric(documentNumber);
            default -> false;
        };
    }

    private boolean isValidNumeric(String documentNumber) {
        return documentNumber.matches("\\d+");
    }

    private boolean isValidPAStructure(@NotNull String documentNumber) {
        return documentNumber.matches("^[A-Z0-9]{3,16}$");
    }

    private boolean isValidCEStructure(@NotNull String documentNumber) {
        return documentNumber.matches("^\\d{1,7}$");
    }

    private boolean isValidTIStructure(@NotNull String documentNumber) {
        return documentNumber.matches("^[A-Z0-9]{10,11}$");
    }

    private boolean isValidCDStructure(@NotNull String documentNumber) {
        return documentNumber.matches("^[A-Z0-9]{6,11}$");
    }

    private boolean isValidPEStructure(@NotNull String documentNumber) {
        return documentNumber.matches("^[A-Z0-9]{15}$");
    }

    private boolean isValidNIT(@NotNull String documentNumber) {
        try {
            String baseNumber = documentNumber.substring(0, documentNumber.length() - 1);
            long idNumber = Long.parseLong(baseNumber);
            int providedDV = Character.getNumericValue(documentNumber.charAt(documentNumber.length() - 1));
            int calculatedDV = calculateModulo11DV(baseNumber);

            return providedDV == calculatedDV && (isEmployerPersonNatural(idNumber) || isEmployerPersonJuridica(idNumber));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isEmployerPersonJuridica(long idNumber) {
        return idNumber >= 800000000 && idNumber <= 999999999;
    }

    @Override
    public boolean isEmployerPersonNatural(long idNumber) {
        return (idNumber >= 1 && idNumber <= 99999999) ||
                (idNumber >= 600000000 && idNumber <= 799999999) ||
                (idNumber >= 1000000000L && idNumber <= 999999999999L);
    }

    @Override
    public int calculateModulo11DV(@NotNull String baseNumber) {
        baseNumber = baseNumber.replaceAll("\\D", "");
        if (baseNumber.isEmpty()) {
            throw new IllegalArgumentException("El número ingresado no es válido.");
        }

        int[] pesosCedula = {71, 67, 59, 53, 47, 43, 41, 37, 29, 23, 19, 17, 13, 7, 3};
        int suma = 0;

        int offset = pesosCedula.length - baseNumber.length();

        for (int i = 0; i < baseNumber.length(); i++) {
            int digito = Character.getNumericValue(baseNumber.charAt(i));
            suma += digito * pesosCedula[i + offset];
        }

        int residuo = suma % 11;
        return (residuo > 1) ? (11 - residuo) : residuo;
    }


    /*
     * Method of Calculating Age
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void checkAndUpdateUserStatuses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeThreshold72Hours = now.minusHours(72);
        LocalDateTime timeThreshold30Days = now.minusDays(30);
        LocalDateTime timeThreshold60Days = now.minusDays(60);

        logger.info("Ejecutando checkAndUpdateUserStatuses a las {}", now);
        logger.info("Threshold 72 horas: {}", timeThreshold72Hours);
        logger.info("Threshold 30 días: {}", timeThreshold30Days);

        userStatusUpdateService.updateUsersInactiveAfter72Hours(timeThreshold72Hours);
        userStatusUpdateService.updateUsersInactiveByPendingAffiliation(timeThreshold30Days);
        userStatusUpdateService.deleteUsersInactiveAfter60Days(timeThreshold60Days);
    }

    private Specification<UserMain> hasDocumentTypeAndNumber(String identificationType, String identification) {
        return UserSpecifications.hasDocumentTypeAndNumber(identificationType, identification);
    }

    public static int calculateAge(LocalDate localDate) {
        LocalDate currentDate = LocalDate.now();
        Period age = Period.between(localDate, currentDate);
        return age.getYears();
    }

    private boolean validatePassword(UserMain user, String password) {
        return password.contains(user.getFirstName()) || password.contains(user.getSurname()) || password.contains(user.getIdentification());
    }

    @Override
    public Map<String, Object> updatePassword(UpdateCredentialDTO registerPassword) {
        UserMain user = new UserMain();
        Specification<UserMain> spec = UserSpecifications.byUsername(registerPassword.getDocumentType() +
                "-" + registerPassword.getDocumentNumber() + "-" + registerPassword.getTypeUser());
        Optional<UserMain> userSpecification = iUserPreRegisterRepository.findOne(spec);
        if (userSpecification.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        BeanUtils.copyProperties(userSpecification.get(), user);

        try{
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.add(Constant.CLIENT_ID, affiliationProperties.getClientId());
            map.add(Constant.CLIENT_SECRET, affiliationProperties.getClientSecret());
            map.add(Constant.USERNAME, registerPassword.getDocumentType() +
                    "-" + registerPassword.getDocumentNumber() + "-" + registerPassword.getTypeUser());
            map.add(Constant.PASSWORD, registerPassword.getCurrentPassword());
            map.add(Constant.GRANT_TYPE, Constant.PASSWORD);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    affiliationProperties.getKeycloakUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(map, headers),
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> tokenInfo = response.getBody();
            if(tokenInfo!=null)
                logout(tokenInfo.get("refresh_token").toString());
        }catch (HttpClientErrorException ex){
            throw new UserNotRegisteredException(Constant.CURRENT_PASSWORD_INCORRECT);
        }

        if (validatePassword(user, registerPassword.getPassword())) {
            throw new ErrorContainDataPersonal(Constant.PASSWORD_NOT_CONTAIN_DATA_PERSONAL);
        }

        if (registerPassword.getCurrentPassword().equals(registerPassword.getPassword())){
            throw new UserNotRegisteredException(Constant.PASSWORD_EQUALS_TO_CURRENT);
        }

        if (!registerPassword.getPassword().equals(registerPassword.getConfirmPassword())){
            throw new UserNotRegisteredException(Constant.PASSWORD_AND_CONFIRM_DIFFERENT);
        }

        user.setLastPasswordUpdate(LocalDateTime.now());
        user.setIsPasswordExpired(false);
        iUserPreRegisterRepository.save(user);

        return keycloakServiceImpl.updateUser(userSpecification.orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE)).getEmail(), registerPassword.getPassword());
    }

    private void logout(String refreshToken) {
        try {
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_FORM_URLENCODED);

            String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout",
                    properties.getKeycloakAuthServerUrl(), properties.getRealm());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(Constant.REFRESH_TOKEN, refreshToken);
            body.add(Constant.CLIENT_SECRET, properties.getClientSecret());
            body.add(Constant.CLIENT_ID, properties.getClientId());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            restTemplate.exchange(logoutUrl, HttpMethod.POST, requestEntity, String.class);

            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            throw new RuntimeException("Logout error: " + e.getMessage());
        }
    }

    private HttpHeaders createHeaders(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        return headers;
    }

    @Override
    public UserDtoApiRegistry searchUserInNationalRegistry(String identificationNumber) {
        UserDtoApiRegistry userRegistry = new UserDtoApiRegistry();
        List<RegistryOfficeDTO> registries = webClient.searchNationalRegistry(identificationNumber);

        ObjectMapper mapper = new ObjectMapper();
        List<RegistryOfficeDTO> registryOfficeDTOS = mapper.convertValue(registries,
                new TypeReference<>() {
                });

        if(!registryOfficeDTOS.isEmpty()){
            RegistryOfficeDTO registry = registryOfficeDTOS.get(0);
            userRegistry.setIdentificationType(Constant.CC);
            userRegistry.setIdentification(identificationNumber);
            userRegistry.setFirstName(capitalize(registry.getFirstName()));
            userRegistry.setSecondName(capitalize(registry.getSecondName()));
            userRegistry.setSurname(capitalize(registry.getFirstLastName()));
            userRegistry.setSecondSurname(capitalize(registry.getSecondLastName()));
            userRegistry.setPhoneNumber("");
            userRegistry.setEmail("");
            userRegistry.setDateBirth(LocalDate.parse(registry.getBirthDate()));
            userRegistry.setGender(registry.getGender());
        }

        return userRegistry;
    }

    public static String capitalize(String inputString) {

        if(inputString!=null && !inputString.isEmpty()) {
            return inputString.substring(0,1).toUpperCase() + inputString.substring(1).toLowerCase();
        }
        return "";

    }

    private String findGenderByDescription(String description) {
        if (description != null){
                Gender gender = genderRepository.findByDescription(description)
                        .orElse(null);
            return gender!=null ? gender.getGenderType() : "";
        }
        return null;
    }

    @Override
    public SystemParam registerDaysForcedUpdatePassword(Integer timeForcedUpdatePassword){
        SystemParam paramForcedUpdatePassword = paramRepository.findByParamName(Constant.PARAM_DAYS_FORCED_UPDATE_PASSWORD);

        if(paramForcedUpdatePassword!=null){
            paramForcedUpdatePassword.setParamValue(timeForcedUpdatePassword.toString());
            return paramRepository.save(paramForcedUpdatePassword);
        }

        SystemParam newParamForcedUpdatePassword = new SystemParam();
        newParamForcedUpdatePassword.setParamName(Constant.PARAM_DAYS_FORCED_UPDATE_PASSWORD);
        newParamForcedUpdatePassword.setParamValue(timeForcedUpdatePassword.toString());
        return paramRepository.save(newParamForcedUpdatePassword);
    }

    @Override
    public int findDaysForcedUpdatePassword(){
        SystemParam paramDaysForcedUpdatePassword = paramRepository.findByParamName(Constant.PARAM_DAYS_FORCED_UPDATE_PASSWORD);
        return paramDaysForcedUpdatePassword!=null ? Integer.parseInt(paramDaysForcedUpdatePassword.getParamValue()) : 90;
    }

    @Override
    public UserMain consultUserByUserName(UserNameDTO userName) {
        Specification<UserMain> spec = UserSpecifications.byUsername(userName.getUsername());
        Optional<UserMain> user = iUserPreRegisterRepository.findOne(spec);
        if (user.isEmpty()) {
            throw new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
        return user.get();
    }

    private String structureUsername(String documentType, String documentNumber) {
        return documentType + "-" + documentNumber + "-" + EXT;
    }

    @Override
    public UserUpdateDTO findUserDataById(Long id) {
        UserMain user = iUserPreRegisterRepository.findById(id).orElse(null);

        if (user != null) {
            List<RegistryOfficeDTO> registryData = webClient.searchNationalRegistry(user.getIdentification());
            if (registryData != null && !registryData.isEmpty()) {
                UserUpdateDTO response = UserConverter.entityToDto.apply(user);
                response.setIsRegistryData(true);
                return response;
            }
        }

        return UserConverter.entityToDto.apply(user);
    }

    @Override
    public Boolean updateAffiliationPreRegister(UserUpdateDTO request) {
        UserMain user = iUserPreRegisterRepository.findById(request.getId()).orElse(null);

        try {
            if (user != null) {
                affiliateRepository.findAllByDocumentTypeAndDocumentNumber(user.getIdentificationType(), user.getIdentification())
                        .forEach(affiliate -> {
                            affiliationDetailRepository
                                    .findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber(user.getIdentificationType(), user.getIdentification())
                                    .stream()
                                    .filter(affiliation -> affiliation.getFiledNumber() != null && !affiliation.getFiledNumber().isEmpty())
                                    .forEach(affiliation -> updateAffiliation(request, affiliation, affiliate));

                            affiliateMercantileRepository
                                    .findAllByTypeDocumentPersonResponsibleAndNumberDocumentPersonResponsible(user.getIdentificationType(), user.getIdentification())
                                    .stream()
                                    .filter(affiliateMercantile -> affiliateMercantile.getFiledNumber() != null && !affiliateMercantile.getFiledNumber().isEmpty())
                                    .forEach(affiliateMercantile -> updateAffiliation(request, affiliateMercantile, affiliate));
                        });

                userMapper.requestUpdateToUser(request, user);
                webClient.updateUser(UserConverter.entityToRequestUpdate.apply(user));
                iUserPreRegisterRepository.save(user);

                return true;
            }
        } catch (Exception e) {
            log.error("Error al actualizar {}", e.getMessage());
        }

        return false;
    }

    private void updateAffiliation(UserUpdateDTO request, Object affiliationObj, Affiliate affiliate) {
        if (affiliationObj instanceof AffiliateMercantile affiliateMercantile) {
            Arl arl = arlRepository.findById(request.getHealthPromotingEntity()).orElse(null);
            updatePreRegisterMapper.requestToMercantile(request, affiliateMercantile);

            if (arl != null)
                affiliateMercantile.setArl(arl.getCodeARL());

            affiliateMercantileRepository.save(affiliateMercantile);
        }

        if (affiliationObj instanceof Affiliation affiliation) {
            if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR) ||
                    affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES))
                updatePreRegisterMapper.requestToAffiliationDetailIndependent(request, affiliation);

            if (affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC))
                updatePreRegisterMapper.requestToAffiliationDetailDomestic(request, affiliation);

            if (affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER) ||
                    affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER))
                updatePreRegisterMapper.requestToAffiliationDriverOrVolunteer(request, affiliation);

            affiliationDetailRepository.save(affiliation);
        }
    }

    @Override
    public ExternalUserDTO consultExternalUser(String documentType, String documentNumber){
        ExternalUserDTO response = new ExternalUserDTO();
        Specification<UserMain> spec = UserSpecifications.findExternalUserByDocumentTypeAndNumber(documentType,
                documentNumber);
        List<UserMain> userList = iUserPreRegisterRepository.findAll(spec);
        if (!userList.isEmpty()) {
            if(userList.size()>1)
                throw new UserAndTypeAlreadyExists("User with more than one registration");

            UserMain user = userList.get(0);
            response.setTipoDocumento(documentType);
            response.setNumeroDocumento(documentNumber);
            response.setPrimerNombre(user.getFirstName());
            response.setSegundoNombre(user.getSecondName());
            response.setPrimerApellido(user.getSurname());
            response.setSegundoApellido(user.getSecondSurname());
            response.setCorreo(user.getEmail());
            return response;
        }else{
            throw new UserNotFoundInDataBase("User not found");
        }
    }

    @Override
    @Transactional
    public Boolean updateExternalUser(UpdateEmailExternalUserDTO updateUser){
        Specification<UserMain> spec = UserSpecifications.findExternalUserByDocumentTypeAndNumber(
                updateUser.getDocumentType(), updateUser.getDocumentNumber());
        List<UserMain> userList = iUserPreRegisterRepository.findAll(spec);
        if (!userList.isEmpty()) {
            if(userList.size()>1)
                throw new UserAndTypeAlreadyExists("User with more than one registration");

            UserMain user = userList.get(0);
            //Actualizacion en keycloak
            if(!user.getEmail().equalsIgnoreCase(updateUser.getEmail())) {
                UserPreRegisterDto userPreRegisterDto = new UserPreRegisterDto();
                BeanUtils.copyProperties(user, userPreRegisterDto);
                userPreRegisterDto.setEmail(user.getEmail());
                keycloakService.updateEmailUser(userPreRegisterDto, updateUser.getEmail());
            }
            //Actualizacion tabla usuario
            user.setEmail(updateUser.getEmail());
            iUserPreRegisterRepository.save(user);
        }else{
            return false;
        }
        return true;
    }

    public boolean validPasswordOld(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.add(Constant.CLIENT_ID, affiliationProperties.getClientId());
            map.add(Constant.CLIENT_SECRET, affiliationProperties.getClientSecret());
            map.add(Constant.USERNAME, username);
            map.add(Constant.PASSWORD, password);
            map.add(Constant.GRANT_TYPE, Constant.PASSWORD);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    affiliationProperties.getKeycloakUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(map, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody() != null;

        } catch (Exception e) {
            return false;
        }
    }

}
