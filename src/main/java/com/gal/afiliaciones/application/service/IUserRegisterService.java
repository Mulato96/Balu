package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.SystemParam;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.ExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.ResponseUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateCredentialDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateEmailExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdatePasswordDTO;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.user.UserNameDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface IUserRegisterService {
    ResponseUserDTO userPreRegister(UserPreRegisterDto userPreRegisterDto) throws MessagingException, IOException, IllegalAccessException;
    UserPreRegisterDto consultUser(String documentType, String documentNumber);
    Map<String, Object> registerPassword(UpdatePasswordDTO registrarContrasenia);
    Optional<UserDtoApiRegistry> getByIdentification(String identification);
    void updateStatusPreRegister(String identificationType, String identification);
    void updateStatusActive(String identificationType, String identification);
    void updateStatusInactive(String identificationType, String identification);
    void updateStatusStartAffiliation(String identificationType, String identification);
    void updateStatusInactiveFalse(String identificationType, String identification);
    UserMain consultUserByIdentification(String identification);
    boolean validateEmployerRangeNaturalPerson(String documentType, String documentNumber);
    int calculateModulo11DV(@NotNull String baseNumber);
    boolean isEmployerPersonNatural(long idNumber);
    boolean isEmployerPersonJuridica(long idNumber);
    Map<String, Object> updatePassword(UpdateCredentialDTO newPassword);
    SystemParam registerDaysForcedUpdatePassword(Integer timeForcedUpdatePassword);
    int findDaysForcedUpdatePassword();
    UserMain consultUserByUserName(UserNameDTO userName);
    UserUpdateDTO findUserDataById(Long id);
    Boolean updateAffiliationPreRegister(UserUpdateDTO request);
    UserDtoApiRegistry searchUserInNationalRegistry(String identificationNumber);
    ExternalUserDTO consultExternalUser(String documentType, String documentNumber);
    Boolean updateExternalUser(UpdateEmailExternalUserDTO updateUser);
}

