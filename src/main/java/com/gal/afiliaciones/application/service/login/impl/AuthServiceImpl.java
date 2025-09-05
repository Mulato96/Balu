package com.gal.afiliaciones.application.service.login.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.login.AuthService;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.ErrorExpirationTemporalPass;
import com.gal.afiliaciones.config.ex.LoginException;
import com.gal.afiliaciones.config.ex.PasswordExpiredException;
import com.gal.afiliaciones.config.ex.validationpreregister.*;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalClient;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.ResponseUserDTO;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.login.RoleResponseLoginDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    public static final String ROLES = "roles";
    private final RestTemplate restTemplate;
    private final AffiliationProperties affiliationProperties;
    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final RolesUserService rolesUserService;
    private final HttpServletRequest request;
    private final IUserRegisterService userRegisterService;
    private final CollectProperties properties;
    private final GenericWebClient webClient;
    private final ConsultUserPortalClient consultUserPortalClient;
    private final ConsultEmployerClient consultEmployerClient;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;

    private static final String NAME_SCREEN_OTP = "login";
    public Map<String, Object> login(String documentType, String username, String password, TypeUser userType) {
        try {

            UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byUsername(structureUserName(documentType,username,userType)))
                    .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_LOGIN));
            Boolean isInArrearsStatus = webClient.checkUserArrearsStatus(
                    user.getIdentification(), user.getIdentificationType());

            Optional<String> verifySessions = verifySessions(user.getEmail());

            if(user.getIsImport() == Boolean.TRUE){
                throw new PasswordExpiredException(Constant.PASSWORD_EXPIRED);
            }

            if(verifySessions.isPresent())
                throw new DuplicateSessionException(verifySessions.get());

            if (user.getLockoutTime() != null && user.getLockoutTime().isAfter(LocalDateTime.now())) {
                throw new LoginAttemptsError(Duration.between(LocalDateTime.now(), user.getLockoutTime()).toHours()+" horas y "+Duration.between(LocalDateTime.now(), user.getLockoutTime()).toMinutesPart()+" minutos");
            }

            if (user.getIsTemporalPassword() != null && user.getIsTemporalPassword() && ChronoUnit.DAYS.between(user.getCreatedAtTemporalPassword(), LocalDateTime.now()) > 10) {
                throw new ErrorExpirationTemporalPass(Constant.TEMPORAL_PASSWORD_EXPIRED);
            }

            if(Boolean.TRUE.equals(user.getInactiveByPendingAffiliation())) {
                OTPRequestDTO otpRequestDTO = OTPRequestDTO.builder()
                        .cedula(user.getIdentification())
                        .destinatario(user.getEmail())
                        .nombre(user.getFirstName() + " " + user.getSecondName())
                        .build();
                otpRequestDTO.setNameScreen(NAME_SCREEN_OTP);
                throw new PendingAffiliationError(Constant.USER_INACTIVE_PENDING_AFFILIATION, otpRequestDTO);
            }

            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.add(Constant.CLIENT_ID, affiliationProperties.getClientId());
            map.add(Constant.CLIENT_SECRET, affiliationProperties.getClientSecret());
            map.add(Constant.USERNAME, user.getUserName());
            map.add(Constant.PASSWORD, password);
            map.add(Constant.GRANT_TYPE, Constant.PASSWORD);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    affiliationProperties.getKeycloakUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(map, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            user.setLoginAttempts(0);
            user.setLockoutTime(null);
            user.setIsInArrearsStatus(isInArrearsStatus);
            userPreRegisterRepository.save(user);
            Map<String, Object> tokenInfo = response.getBody();
            if (Boolean.FALSE.equals(user.getStatusActive())) {
                throw new InactiveStatusError(Constant.USER_INACTIVE);
            }
            if (tokenInfo != null) {
                Optional<UserMain> userConsult = userPreRegisterRepository.findOne(UserSpecifications.byUsername(structureUserName(documentType,username,userType)));
                if (userConsult.isPresent()) {
                    ResponseUserDTO responseUserDTO = new ResponseUserDTO();
                    AddressDTO addressDTO = new AddressDTO();
                    BeanUtils.copyProperties(userConsult.get(), addressDTO);
                    BeanUtils.copyProperties(userConsult.get(), responseUserDTO);
                    responseUserDTO.setAddress(addressDTO);
                    responseUserDTO.setInfoOperator(userConsult.get().getInfoOperator());
                    responseUserDTO.setFinancialOperator(userConsult.get().getFinancialOperator());
                    responseUserDTO.setPhone2(userConsult.get().getPhoneNumber2());
                    responseUserDTO.setLastUpdateDate(user.getLastUpdate());
                    responseUserDTO.setProfileImage(getProfileImageByUserId(userConsult.get().getId()));
                    responseUserDTO.setIsInArrearsStatus(userConsult.get().getIsInArrearsStatus());
                    responseUserDTO.setTypeUser(userType);
                    responseUserDTO.setUserName(userConsult.get().getUserName());
                    tokenInfo.put(Constant.USER_INFO, responseUserDTO);
                    if(rolesUserService.getRolesByUser(user.getId()).isEmpty()){
                        tokenInfo.put(ROLES, getTokenRoles(rolesUserService.getRolesByRoleName("Pre registrado")));

                    }else{
                        tokenInfo.put(ROLES, getTokenRoles(rolesUserService.getRolesByUser(user.getId())));

                    }
                }
            }
            return tokenInfo;
        } catch (HttpClientErrorException e) {
            log.error(Constant.PASSWORD_INCORRECT, e);
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
                UserMain user = userPreRegisterRepository.findOne(UserSpecifications.byUsername(structureUserName(documentType,username,userType)))
                        .orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_LOGIN));
                user.setLoginAttempts(user.getLoginAttempts() + 1);
                if (user.getLoginAttempts() >= Constant.MAX_LOGIN_ATTEMPTS) {
                    user.setLockoutTime(LocalDateTime.now().plusHours(12));
                }
                userPreRegisterRepository.save(user);

                throw new UserNotRegisteredException(Constant.PASSWORD_INCORRECT);
            }
            throw new LoginException(Optional.ofNullable(e.getMessage()).orElse(Constant.PASSWORD_INCORRECT));
        }
    }

    private String getProfileImageByUserId(Long id){
        try {
            return webClient.getProfileImageByUserId(id).block();
        }catch (Exception ex){
            log.error("Error al consultar la imagen de perfil");
            return "";
        }
    }

    private String getTokenRoles(List<RoleResponseLoginDTO> roles){
        HashMap<String, Object> extraClaims = new HashMap<>();

        extraClaims.put(ROLES, roles);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256))
                .compact();
    }

    @Scheduled(cron = "${cron.execute.scheduled}")
    public void passwordExpired() {
        LocalDateTime today = LocalDateTime.now();
        int daysForcedUpdatePassword = userRegisterService.findDaysForcedUpdatePassword();
        LocalDateTime todayLastDays = today.minusDays(daysForcedUpdatePassword);

        Specification<UserMain> spec = UserSpecifications.usersPasswordExpired(todayLastDays);
        List<UserMain> users = userPreRegisterRepository.findAll(spec);

        users.forEach(userMain -> {
            userMain.setIsPasswordExpired(true);
            userPreRegisterRepository.save(userMain);
        });
    }

    @Override
    public String logout(String refreshToken) {
        try {
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_FORM_URLENCODED);

            String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/logout",
                    properties.getKeycloakAuthServerUrl(), properties.getRealm());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(Constant.REFRESH_TOKEN, refreshToken);
            body.add(Constant.CLIENT_SECRET, properties.getClientSecret());
            body.add(Constant.CLIENT_ID, properties.getClientId());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(logoutUrl, HttpMethod.POST, requestEntity, String.class);

            request.getSession().invalidate();
            SecurityContextHolder.clearContext();

            return response.getStatusCode().is2xxSuccessful() ? "Logout successful" : "Logout failed: " + response.getBody();
        } catch (Exception e) {
            return "Logout error: " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.add(Constant.GRANT_TYPE, Constant.REFRESH_TOKEN);
            map.add(Constant.CLIENT_ID, affiliationProperties.getClientId());
            map.add(Constant.CLIENT_SECRET, affiliationProperties.getClientSecret());
            map.add(Constant.REFRESH_TOKEN, refreshToken);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    affiliationProperties.getKeycloakUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(map, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            logout(refreshToken);
            return Map.of("refreshToken", e.getMessage());
        }
    }

    public Optional<String> verifySessions(String username) {
        try {
            String adminToken = getAdminToken().orElseThrow();
            String userId = getUserId(adminToken, username).orElseThrow();
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_JSON);
            headers.set(Constant.AUTHORIZATION, "Bearer " + getAdminToken().orElse(""));

            String url = String.format("%s/admin/realms/%s/users/%s/sessions", properties.getKeycloakAuthServerUrl(),
                    properties.getRealm(), userId);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            return (response.getStatusCode().is2xxSuccessful() && "[]".equals(response.getBody()))
                    ? Optional.empty()
                    : Optional.of("Sesión activa");
        } catch (Exception e) {
            return Optional.of("Error verificando sesión: " + e.getMessage());
        }
    }

    public Optional<String> getUserId(String adminToken, String username) {
        try {
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_JSON);
            headers.set(Constant.AUTHORIZATION, "Bearer " + adminToken);

            String url = String.format("%s/admin/realms/%s/users?email=%s", properties.getKeycloakAuthServerUrl(),
                    properties.getRealm(), username);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            return response.getStatusCode().is2xxSuccessful()
                    ? extractAccessToken(response.getBody(), "id", 1)
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isValidToken(String token) {
        try {
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_FORM_URLENCODED);

            String logoutUrl = String.format("%s/realms/%s/protocol/openid-connect/token/introspect",
                    properties.getKeycloakAuthServerUrl(), properties.getRealm());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(Constant.TOKEN, token);
            body.add(Constant.CLIENT_SECRET, properties.getClientSecret());
            body.add(Constant.CLIENT_ID, properties.getClientId());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(logoutUrl, HttpMethod.POST, requestEntity, String.class);

            return !Objects.equals(response.getBody(), "{\"active\":false}");

        } catch (Exception e) {
            return false;
        }
    }

    public Optional<String> getAdminToken() {
        try {
            HttpHeaders headers = createHeaders(MediaType.APPLICATION_FORM_URLENCODED);

            String url = String.format("%s/realms/master/protocol/openid-connect/token",
                    properties.getKeycloakAuthServerUrl());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(Constant.GRANT_TYPE, Constant.PASSWORD);
            body.add(Constant.CLIENT_ID, properties.getClientId());
            body.add(Constant.USERNAME, properties.getUseAdmin());
            body.add(Constant.PASSWORD, properties.getPasswordAdmin());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return response.getStatusCode().is2xxSuccessful()
                    ? extractAccessToken(response.getBody(), "access_token", 0)
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractAccessToken(String json, String key, int opt) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(json);
            return Optional.ofNullable(opt == 0 ? rootNode.path(key).asText() : rootNode.get(0).path(key).asText());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private HttpHeaders createHeaders(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        return headers;
    }

    private String structureUserName(String documentType,String username, TypeUser userType){
        return documentType + "-" + username +"-"+ userType;
    }
}
