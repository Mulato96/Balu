package com.gal.afiliaciones.infrastructure.controller.users;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.SystemParam;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.ExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.ResponseUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateCredentialDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateEmailExternalUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdatePasswordDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateStatus;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.user.UserNameDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserPreRegisterController {

    private final IUserRegisterService iUserRegisterService;

    /**
     * Validate if the employer is a natural person based on document type and number.
     *
     * @param documentType The type of the document.
     * @param documentNumber The number of the document.
     * @return True if the employer is a natural person, otherwise false.
     */
    @GetMapping("/validateEmployerRangeNaturalPerson/{documentType}/{documentNumber}")
    public ResponseEntity<Boolean> validateEmployerRangeNaturalPerson(
            @PathVariable String documentType,
            @PathVariable String documentNumber) {
        boolean isValid = iUserRegisterService.validateEmployerRangeNaturalPerson(documentType, documentNumber);
        return ResponseEntity.ok(isValid);
    }

    /**
     * View a user's data by document type and number.
     * If it is not in the local database, try to check with the National Registry if applicable.
     *
     * @param identificationType User's document type.
     * @param identification     User's document number.
     * @return User data or message not found.
     */
    @GetMapping("/consulting/{identificationType}/{identification}")
    public ResponseEntity<UserPreRegisterDto> consultUser(@PathVariable String identificationType, @PathVariable String identification) {
        UserPreRegisterDto userPreRegisterDto = iUserRegisterService.consultUser(identificationType, identification);
        return ResponseEntity.ok(userPreRegisterDto);
    }

    /**
     * View a user's data by document number.
     * If it is not in the local database, execute exception.
     */
    @GetMapping("/consulting/{identification}")
    public ResponseEntity<UserMain> consultUserByIdentification(@PathVariable String identification) {
        UserMain user = iUserRegisterService.consultUserByIdentification(identification);
        return ResponseEntity.ok(user);
    }

    /**
     * Register a new user in the system. If the data comes from the Registry, verify first and then save.
     *
     * @param userPreRegisterDto User data to be recorded.
     * @return Registered user or error message if something goes wrong.
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseUserDTO> registerUser(@Validated @RequestBody UserPreRegisterDto userPreRegisterDto) throws MessagingException, IOException, IllegalAccessException {
        ResponseUserDTO user = iUserRegisterService.userPreRegister(userPreRegisterDto);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/registerpassword")
    public ResponseEntity<Map<String, Object>> registerPassword(@Valid @RequestBody UpdatePasswordDTO registerPassword) {
        return ResponseEntity.ok().body(iUserRegisterService.registerPassword(registerPassword));
    }
    @PutMapping("/updateStatusPreRegister")
    public ResponseEntity<String> updateStatusPreRegister(@RequestBody UpdateStatus updateStatus) {
        iUserRegisterService.updateStatusPreRegister(updateStatus.getIdentificationType(), updateStatus.getIdentification());
        return ResponseEntity.ok(Constant.UPDATE_PRE_REGISTER_SUCCESSFUL);
    }
    @PutMapping("/updateStatusActive")
    public ResponseEntity<String> updateStatusActive(@RequestBody UpdateStatus updateStatus) {
        iUserRegisterService.updateStatusActive(updateStatus.getIdentificationType(), updateStatus.getIdentification());
        return ResponseEntity.ok(Constant.UPDATE_ACTIVE_SUCCESSFUL);
    }
    @PutMapping("/updateStatusInactive")
    public ResponseEntity<String> updateStatusInactive(@RequestBody UpdateStatus updateStatus) {
        iUserRegisterService.updateStatusInactive(updateStatus.getIdentificationType(), updateStatus.getIdentification());
        return ResponseEntity.ok(Constant.UPDATE_INACTIVE_SUCCESSFUL);
    }
    @PutMapping("/updateStatusInactiveFalse")
    public ResponseEntity<String> updateStatusInactiveFalse(@RequestBody UpdateStatus updateStatus) {
        iUserRegisterService.updateStatusInactiveFalse(updateStatus.getIdentificationType(), updateStatus.getIdentification());
        return ResponseEntity.ok(Constant.UPDATE_INACTIVE_SUCCESSFUL);
    }

    @PutMapping("/updatestatusaffiliation")
    public ResponseEntity<String> updateStatusStartAffiliation(@RequestBody UpdateStatus updateStatus){
        iUserRegisterService.updateStatusStartAffiliation(updateStatus.getIdentificationType(), updateStatus.getIdentification());
        return ResponseEntity.ok().body(Constant.UPDATE_ACTIVE_SUCCESSFUL);
    }

    @PutMapping("/updatepassword")
    public ResponseEntity<Map<String, Object>> updatePassword(@Valid @RequestBody UpdateCredentialDTO registerPassword) {
        return ResponseEntity.ok().body(iUserRegisterService.updatePassword(registerPassword));
    }

    @GetMapping("/consultDvNit/{nit}")
    public ResponseEntity<Integer> consultVerificationDigit(@PathVariable String nit) {
        int dvNit = iUserRegisterService.calculateModulo11DV(nit);
        return ResponseEntity.ok(dvNit);
    }

    @PostMapping("/daysForcedUpdatePassword/{timeForcedUpdatePassword}")
    public ResponseEntity<SystemParam> daysForcedUpdatePassword(@PathVariable Integer timeForcedUpdatePassword) {
        SystemParam param = iUserRegisterService.registerDaysForcedUpdatePassword(timeForcedUpdatePassword);
        return ResponseEntity.ok(param);
    }

    @GetMapping("getDaysForcedUpdatePassword")
    public ResponseEntity<Integer> getDaysForcedUpdatePassword() {
        int daysForcedUpdatePassword = iUserRegisterService.findDaysForcedUpdatePassword();
        return ResponseEntity.ok(daysForcedUpdatePassword);
    }

    @PostMapping("/consulting/username")
    public ResponseEntity<UserMain> consultUserByUserName(@RequestBody UserNameDTO userName) {
        UserMain user = iUserRegisterService.consultUserByUserName(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("find-preregister-affiliation/{idUser}")
    public ResponseEntity<BodyResponseConfig<UserUpdateDTO>> findPreRegisterAffiliation(@PathVariable Long idUser) {
        return ResponseEntity.ok(new BodyResponseConfig<>(iUserRegisterService.findUserDataById(idUser), ""));
    }

    @PostMapping("update-preregister-affiliation")
    public ResponseEntity<BodyResponseConfig<Boolean>> updatePreRegisterAffiliation(@RequestBody UserUpdateDTO request) {
        return ResponseEntity.ok(new BodyResponseConfig<>(iUserRegisterService.updateAffiliationPreRegister(request), ""));
    }

    @GetMapping("/consultExternalUser/{identificationType}/{identification}")
    public ResponseEntity<ExternalUserDTO> consultExternalUser(@PathVariable String identificationType, @PathVariable String identification) {
        ExternalUserDTO userPreRegisterDto = iUserRegisterService.consultExternalUser(identificationType, identification);
        return ResponseEntity.ok(userPreRegisterDto);
    }

    @PutMapping("/updateExternalUser")
    public ResponseEntity<Boolean> updateExternalUser(@Valid @RequestBody UpdateEmailExternalUserDTO updateUser) {
        return ResponseEntity.ok().body(iUserRegisterService.updateExternalUser(updateUser));
    }

}
