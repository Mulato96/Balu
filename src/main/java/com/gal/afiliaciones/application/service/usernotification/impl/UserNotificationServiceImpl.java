package com.gal.afiliaciones.application.service.usernotification.impl;

import com.gal.afiliaciones.application.service.usernotification.UserNotificationService;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.usernotification.UserNotificationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final AffiliateMercantileRepository mercantileRepository;
    private final IUserPreRegisterRepository userPreRegisterRepository;

    @Override
    public List<UserNotificationDTO> findAllAffiliatedUser(){

        List<UserNotificationDTO> response = new ArrayList<>();
        List<Affiliate> initList = affiliateRepository.findAllByAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        if(initList.isEmpty())
            return response;

        List<Affiliate> affiliateList = initList.stream().filter(affiliate -> affiliate.getFiledNumber()!=null).toList();

        affiliateList.forEach(affiliate -> {
            new UserNotificationDTO();
            UserNotificationDTO userNotificationDTO = switch (affiliate.getAffiliationType()) {
                case Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, Constant.TYPE_AFFILLATE_INDEPENDENT ->
                        convertDomesticOrIndependent(affiliate.getFiledNumber());
                case Constant.TYPE_AFFILLATE_EMPLOYER -> convertMercantile(affiliate.getFiledNumber());
                default -> new UserNotificationDTO();
            };

            if(userNotificationDTO.getIdentificationNumber()!=null) {
                userNotificationDTO.setAffiliationType(affiliate.getAffiliationType());
                userNotificationDTO.setAffiliationSubtype(affiliate.getAffiliationSubType());
                response.add(userNotificationDTO);
            }
        });

        return response;
    }

    private UserNotificationDTO convertDomesticOrIndependent(String filedNumber){
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();

        Affiliation affiliation = affiliationRepository.findByFiledNumber(filedNumber).orElse(null);
        if(affiliation!=null) {
            Long idUser = findUserId(affiliation.getIdentificationDocumentType(), affiliation.getIdentificationDocumentNumber());
            if(idUser!=null) {
                userNotificationDTO.setIdentificationType(affiliation.getIdentificationDocumentType());
                userNotificationDTO.setIdentificationNumber(affiliation.getIdentificationDocumentNumber());
                userNotificationDTO.setCompleteName(concatCompleteName(affiliation.getFirstName(),
                        affiliation.getSecondName(), affiliation.getSurname(), affiliation.getSecondSurname()));
                userNotificationDTO.setIdUser(idUser);
                userNotificationDTO.setAddress(affiliation.getAddress());
                userNotificationDTO.setPhone(affiliation.getPhone1());
                userNotificationDTO.setEmail(affiliation.getEmail());
            }
        }
        return userNotificationDTO;
    }

    private UserNotificationDTO convertMercantile(String filedNumber){
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        AffiliateMercantile mercantile = mercantileRepository.findByFiledNumber(filedNumber).orElse(null);
        if(mercantile!=null) {
            userNotificationDTO.setIdentificationType(mercantile.getTypeDocumentPersonResponsible());
            userNotificationDTO.setIdentificationNumber(mercantile.getNumberDocumentPersonResponsible());
            userNotificationDTO.setCompleteName(mercantile.getBusinessName());
            userNotificationDTO.setIdUser(mercantile.getIdUserPreRegister());
            userNotificationDTO.setAddress(mercantile.getAddress());
            userNotificationDTO.setPhone(mercantile.getPhoneOne());
            userNotificationDTO.setEmail(mercantile.getEmail());
        }
        return userNotificationDTO;
    }

    private String concatCompleteName(String firstname, String secondname, String surname, String secondsurname){
        String completeName = firstname.concat(" ");
        if(!secondname.isBlank())
            completeName = completeName.concat(secondname).concat(" ");
        completeName = completeName + surname;
        if(!secondsurname.isBlank())
            completeName = completeName.concat(" ").concat(secondsurname);
        return completeName;
    }

    private Long findUserId(String identificationType, String identificationNumber){
        Specification<UserMain> spec = UserSpecifications.findExternalUserByDocumentTypeAndNumber(identificationType,
                identificationNumber);
        UserMain user = userPreRegisterRepository.findOne(spec).orElse(null);

        return user!=null ? user.getId() : null;
    }

}
