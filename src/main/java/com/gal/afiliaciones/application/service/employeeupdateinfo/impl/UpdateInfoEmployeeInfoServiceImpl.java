package com.gal.afiliaciones.application.service.employeeupdateinfo.impl;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.employeeupdateinfo.IUpdateInfoEmployeeInfoService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.employeeupdateinfo.UpdateInfoEmployeeIndependentRequest;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateInfoEmployeeInfoServiceImpl implements IUpdateInfoEmployeeInfoService {

    private final EmailService emailService;
    private final AffiliateRepository affiliateRepository;
    private final IUserPreRegisterRepository userMainRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final KeycloakService keycloakService;

    @Transactional
    @Override
    public String updateInfoEmployeeIndependet(UpdateInfoEmployeeIndependentRequest request) throws MessagingException, IOException {

        List<Affiliate> affiliationsWorker = affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE, Constant.TYPE_AFFILLATE_INDEPENDENT);

        UserMain user = userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                        request.getIdentification()).orElseThrow(() -> new AffiliateNotFound(Constant.USER_NOT_FOUND));
        String currentEmail = user.getEmail();
        updateInfoUser(request, user);

        if (affiliationsWorker.isEmpty()) {
            throw new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND);
        }

        Affiliation lastUpdatedWorkerDependent = null;

        for (Affiliate affiliate : affiliationsWorker) {
            Optional<Affiliation> workerIndependent = affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber());

            if (workerIndependent.isPresent()) {
                updateWorkerDetails(workerIndependent.get(), request);
                lastUpdatedWorkerDependent = workerIndependent.get();
            }
        }

        //Actualizacion en keycloak
        if(!currentEmail.equalsIgnoreCase(request.getEmail())) {
            UserPreRegisterDto userPreRegisterDto = new UserPreRegisterDto();
            BeanUtils.copyProperties(user, userPreRegisterDto);
            userPreRegisterDto.setEmail(currentEmail);
            keycloakService.updateEmailUser(userPreRegisterDto, request.getEmail());
        }

        if (lastUpdatedWorkerDependent != null) {
            sendEmail(lastUpdatedWorkerDependent);
        }

        return Constant.UPDATE_INFO_EMPLOYEE_INDEPENDENT;
    }

    private void updateInfoUser(UpdateInfoEmployeeIndependentRequest request, UserMain user) {

        user.setIdDepartment(request.getDepartment());
        user.setIdCity(request.getCity());
        user.setIdMainStreet(request.getMainStreet());
        user.setIdNumberMainStreet(request.getMainStreetNumber());
        user.setIdLetter1MainStreet(request.getMainStreetLetter1());
        user.setIdLetter2MainStreet(request.getMainStreetLetter2());
        user.setIsBis(request.isBis());
        user.setIdCardinalPointMainStreet(request.getMainStreetDirection());
        user.setIdNum1SecondStreet(request.getIdNum1SecondStreet());
        user.setIdLetterSecondStreet(request.getIdLetterSecondStreet());
        user.setIdLetterSecondStreet(request.getIdNum2SecondStreet());
        user.setIdCardinalPoint2(request.getIdCardinalPoint2());
        user.setIdHorizontalProperty1(request.getAdditionalHorizontalProperty1());
        user.setIdNumHorizontalProperty1(request.getAdditionalHorizontalProperty1Number());
        user.setIdHorizontalProperty2(request.getAdditionalHorizontalProperty2());
        user.setIdNumHorizontalProperty2(request.getAdditionalHorizontalProperty2Number());
        user.setIdHorizontalProperty3(request.getAdditionalHorizontalProperty3());
        user.setIdNumHorizontalProperty3(request.getAdditionalHorizontalProperty3Number());
        user.setIdHorizontalProperty4(request.getAdditionalHorizontalProperty4());
        user.setIdNumHorizontalProperty4(request.getAdditionalHorizontalProperty4Number());
        user.setAddress(request.getFullAddress());
        user.setPhoneNumber(request.getPrimaryPhone());
        user.setPhoneNumber2(request.getSecondaryPhone());
        user.setEmail(request.getEmail());
        user.setHealthPromotingEntity(request.getEps());
        user.setPensionFundAdministrator(request.getAfp());
        user.setLastUpdate(LocalDateTime.now());

        userMainRepository.save(user);
    }

    private void updateWorkerDetails(Affiliation worker, UpdateInfoEmployeeIndependentRequest request) {
        worker.setIdDepartmentIndependentWorker((request.getDepartment()));
        worker.setIdCityIndependentWorker(request.getCity());
        worker.setIdMainStreet(request.getMainStreet());
        worker.setIdNumberMainStreet(request.getMainStreetNumber());
        worker.setIdLetter1MainStreet(request.getMainStreetLetter1());
        worker.setIdLetter2MainStreet(request.getMainStreetLetter2());
        worker.setIsBis(request.isBis());
        worker.setIdCardinalPointMainStreet(request.getMainStreetDirection());
        worker.setIdNum1SecondStreet(request.getIdNum1SecondStreet());
        worker.setIdLetterSecondStreet(request.getIdLetterSecondStreet());
        worker.setIdLetterSecondStreet(request.getIdNum2SecondStreet());
        worker.setIdCardinalPoint2(request.getIdCardinalPoint2());
        worker.setIdHorizontalProperty1(request.getAdditionalHorizontalProperty1());
        worker.setIdNumHorizontalProperty1(request.getAdditionalHorizontalProperty1Number());
        worker.setIdHorizontalProperty2(request.getAdditionalHorizontalProperty2());
        worker.setIdNumHorizontalProperty2(request.getAdditionalHorizontalProperty2Number());
        worker.setIdHorizontalProperty3(request.getAdditionalHorizontalProperty3());
        worker.setIdNumHorizontalProperty3(request.getAdditionalHorizontalProperty3Number());
        worker.setIdHorizontalProperty4(request.getAdditionalHorizontalProperty4());
        worker.setIdNumHorizontalProperty4(request.getAdditionalHorizontalProperty4Number());
        worker.setAddress(request.getFullAddress());
        worker.setPhone1(request.getPrimaryPhone());
        worker.setPhone2(request.getSecondaryPhone());
        worker.setEmail(request.getEmail());
        worker.setHealthPromotingEntity(request.getEps());
        worker.setPensionFundAdministrator(request.getAfp());

        affiliationDetailRepository.save(worker);
    }

    private void sendEmail(@NotNull Affiliation worker) throws MessagingException, IOException {
        EmailDataDTO emailDto = buildEmailData(worker);
        emailService.sendSimpleMessage(emailDto, "Actualización de datos trabajador independiente");
    }

    private EmailDataDTO buildEmailData(Affiliation worker) {
        EmailDataDTO emailDto = new EmailDataDTO();
        emailDto.setDestinatario(worker.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", worker.getFirstName() + " " + worker.getSecondName());
        data.put("currentDate", LocalDate.now());

        emailDto.setPlantilla("plantilla-actualizacion-datos-trabajador-independiente.html");
        emailDto.setDatos(data);

        return emailDto;
    }

}