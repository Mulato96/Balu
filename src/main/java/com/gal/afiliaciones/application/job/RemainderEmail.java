package com.gal.afiliaciones.application.job;

import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemainderEmail {

    private final IUserPreRegisterRepository userPreRegisterRepository;
    private final EmailService emailService;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationDomesticRepository;

    private final AffiliateRepository affiliateRepository;


    @Scheduled(cron = "0 0 8 * * ?")
    public void sendEmailsToInactiveUsers() {
        List<UserMain> users = userPreRegisterRepository.findAll();
        for (UserMain user : users) {
            List<Affiliate> affiliations = affiliateRepository.findByUserId(user.getId());
            for (Affiliate affiliate : affiliations) {
                long daysElapsed = getDaysElapsed(affiliate);
                if (!isAffiliationCompleted(affiliate) && daysElapsed >= Constant.REMINDER_START_DAYS && daysElapsed <= Constant.REMINDER_END_DAYS) {
                    sendReminderEmail(user, affiliate);
                }
            }
        }
    }

    private long getDaysElapsed(Affiliate currentAffiliate) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime affiliationDate = currentAffiliate.getAffiliationDate();
        if(currentAffiliate.getAffiliationDate()!=null) {
            return ChronoUnit.DAYS.between(affiliationDate, currentDateTime);
        }else if(currentAffiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)){
            AffiliateMercantile mercantile = affiliateMercantileRepository.findByFiledNumber(
                    currentAffiliate.getFiledNumber()).orElse(null);
            return mercantile!=null ? ChronoUnit.DAYS.between(mercantile.getDateCreateAffiliate(), currentDateTime) :
                    30;
        }
        return 30 ;
    }

    private boolean isAffiliationCompleted(Affiliate affiliate) {
        String op = affiliate.getAffiliationType();
        return switch (op) {
            case Constant.TYPE_AFFILLATE_EMPLOYER ->
                    affiliateMercantileRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .map(m -> m.getStageManagement().equals(Constant.ACCEPT_AFFILIATION) ||
                                    m.getStageManagement().equals(Constant.SUSPENDED))
                            .orElse(false);
            case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, Constant.TYPE_AFFILLATE_INDEPENDENT ->
                    affiliationDomesticRepository.findByFiledNumber(affiliate.getFiledNumber())
                            .map(d -> d.getStageManagement().equals(Constant.ACCEPT_AFFILIATION) ||
                                    d.getStageManagement().equals(Constant.SUSPENDED))
                            .orElse(false);
            default -> true;
        };
    }

    private void sendReminderEmail(UserMain user, Affiliate affiliate) {
        EmailDataDTO emailDataDTO = new EmailDataDTO();

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", user.getFirstName() + " " + user.getSecondName());
        if(affiliate.getAffiliationType().equals(Constant.TYPE_AFFILLATE_EMPLOYER)) {
            AffiliateMercantile mercantile = affiliateMercantileRepository.findByFiledNumber(
                    affiliate.getFiledNumber()).orElse(null);
            LocalDateTime requestDate = mercantile!=null ? mercantile.getDateCreateAffiliate().atStartOfDay() :
                    LocalDateTime.now();
            data.put("limitDate", requestDate.plusDays(31).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }else {
            data.put("limitDate", affiliate.getAffiliationDate().plusDays(31).format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        emailDataDTO.setPlantilla("afiliacion-pendiente.html");
        emailDataDTO.setDestinatario(user.getEmail());
        emailDataDTO.setDatos(data);

        try {
            emailService.sendSimpleMessage(emailDataDTO, Constant.REMAINDER_AFFILIATION);
        }catch (Exception io){
            log.error("Log timer affiliation: " + Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }

    }


}
