package com.gal.afiliaciones.application.service.login.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;

import jakarta.servlet.http.HttpServletRequest;


@ExtendWith(SpringExtension.class)
class AuthServiceImplTest {

    @Mock private RestTemplate restTemplate;
    @Mock private AffiliationProperties affiliationProperties;
    @Mock private IUserPreRegisterRepository userPreRegisterRepository;
    @Mock private RolesUserService rolesUserService;
    @Mock private HttpServletRequest request;
    @Mock private IUserRegisterService userRegisterService;
    @Mock private CollectProperties properties;
    @Mock private GenericWebClient webClient;

    @InjectMocks private AuthServiceImpl authService;

    private UserMain user;
    private final String documentType = "CC";
    private final String username = "user1";
    private final String password = "pass";
    private final TypeUser userType = TypeUser.EXT; // example enum value

    @BeforeEach
    void setup() {
        user = new UserMain();
        user.setUserName(documentType + "-" + username + "-" + userType);
        user.setIdentification("123456");
        user.setIdentificationType(documentType);
        user.setEmail("email@example.com");
        user.setIsImport(false);
        user.setLoginAttempts(0);
        user.setStatusActive(true);
        user.setId(1L);
        user.setCreatedAtTemporalPassword(LocalDate.now().minusDays(5));
        user.setIsTemporalPassword(false);
        user.setInactiveByPendingAffiliation(false);
        user.setLockoutTime(null);
    }

    @Test
    void login_userNotFound_throws() {
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        UserNotFoundInDataBase ex = assertThrows(UserNotFoundInDataBase.class,
                () -> authService.login(documentType, username, password, userType));
        assertTrue(true);
    }

    @Test
    void logout_successful() {
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://keycloak");
        when(properties.getRealm()).thenReturn("realm");
        when(properties.getClientSecret()).thenReturn("secret");
        when(properties.getClientId()).thenReturn("clientId");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        authService.logout("refreshToken");

        assertTrue(true);
    }

    @Test
    void logout_exception() {
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://keycloak");
        when(properties.getRealm()).thenReturn("realm");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        String result = authService.logout("refreshToken");

        assertTrue(result.contains("Logout error"));
    }

    @Test
    void refreshToken_successful() {
        when(affiliationProperties.getClientId()).thenReturn("clientId");
        when(affiliationProperties.getClientSecret()).thenReturn("secret");
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://keycloak/token");

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("refresh_token", "newRefreshToken");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://keycloak/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        Map<String, Object> result = authService.refreshToken("oldRefreshToken");

        assertNotNull(result);
        assertEquals("newRefreshToken", result.get("refresh_token"));
    }

    @Test
    void refreshToken_exception_callsLogout() {
        when(affiliationProperties.getClientId()).thenReturn("clientId");
        when(affiliationProperties.getClientSecret()).thenReturn("secret");
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://keycloak/token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("error"));

        AuthServiceImpl spyService = Mockito.spy(authService);
        doReturn("Logout successful").when(spyService).logout(anyString());

        Map<String, Object> result = spyService.refreshToken("refreshToken");

        assertTrue(result.containsKey("refreshToken"));
        verify(spyService).logout("refreshToken");
    }

    @Test
    void isValidToken_active() {
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://keycloak");
        when(properties.getRealm()).thenReturn("realm");
        when(properties.getClientSecret()).thenReturn("secret");
        when(properties.getClientId()).thenReturn("clientId");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"active\":true}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        boolean valid = authService.isValidToken("token");
        assertTrue(valid);
    }

    @Test
    void isValidToken_inactive() {
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://keycloak");
        when(properties.getRealm()).thenReturn("realm");
        when(properties.getClientSecret()).thenReturn("secret");
        when(properties.getClientId()).thenReturn("clientId");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{\"active\":false}", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        boolean valid = authService.isValidToken("token");
        assertFalse(valid);
    }

    @Test
    void isValidToken_exception() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("error"));

        boolean valid = authService.isValidToken("token");
        assertFalse(valid);
    }
}