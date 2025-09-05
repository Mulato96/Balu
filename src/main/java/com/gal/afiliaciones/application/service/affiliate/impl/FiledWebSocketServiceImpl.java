package com.gal.afiliaciones.application.service.affiliate.impl;


import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import com.gal.afiliaciones.application.service.notification.RegistryConnectInterviewWebService;
import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FiledWebSocketServiceImpl implements FiledWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AffiliateRepository affiliateRepository;
    private final AffiliateMercantileRepository mercantileRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final RegistryConnectInterviewWebService registryConnectInterviewWebService;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    private static final String ERROR = "Error: ";
    private static final String FILED_NUMBER = "filedNumber";
    private static final String TYPE_NOTIFICATION = "typeNotification";
    private static final String USER = "user";
    private static final String OFFICIAL = "official";
    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";
    private static final String NUMBER_FILED = "numberFiled";
    private static final String ID = "id";

    @Override
    public void changeStateAffiliation(String filedNumber) {

        DataStatusAffiliationDTO dataStatusAffiliationDTO = new DataStatusAffiliationDTO();
        Affiliate affiliate = findByNumberFiled(filedNumber);

        if(affiliate != null){

            Optional<Affiliation> affiliation = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber());
            Optional<AffiliateMercantile> mercantile = mercantileRepository.findByFiledNumber(affiliate.getFiledNumber());

            if (affiliation.isPresent()) {

                BeanUtils.copyProperties(affiliation.get(), dataStatusAffiliationDTO);
                dataStatusAffiliationDTO.setDateAffiliateSuspend(affiliate.getDateAffiliateSuspend());

            }else if (mercantile.isPresent()) {

                BeanUtils.copyProperties(mercantile.get(), dataStatusAffiliationDTO);
                dataStatusAffiliationDTO.setDateAffiliateSuspend(affiliate.getDateAffiliateSuspend());
            }

            BeanUtils.copyProperties(affiliate, dataStatusAffiliationDTO);
            Map<String, Object> data = new HashMap<>();
            data.put("type", OFFICIAL);
            data.put("dataStatusAffiliation", dataStatusAffiliationDTO);
            data.put(TYPE_NOTIFICATION, "changeAffiliation");

            sendNotification(data, filedNumber);

        }


    }

    @Override
    public void connectOfficial(Map<String, String> dataUser, String state) {

        if(dataUser != null && dataUser.get(NUMBER_FILED) != null  && dataUser.get(ID) != null) {

            String filedNumber = dataUser.get(NUMBER_FILED);
            String id = dataUser.get(ID);

            saveRegistry(filedNumber, state, OFFICIAL, id);
            state = state.equals(CONNECTED) ? "officialConnected" : "officialDisconnected";

            Map<String, Object> data = new HashMap<>();
            data.put("type", OFFICIAL);
            data.put(FILED_NUMBER, filedNumber);
            data.put(TYPE_NOTIFICATION, state);
            sendNotification(data, filedNumber);

        }else {
            throw new AffiliationError("Datos incompletos");
        }

    }

    @Override
    public void connectUser(Map<String, String> dataUser, String state) {

        if(dataUser != null && dataUser.get(NUMBER_FILED) != null  && dataUser.get(ID) != null) {

            String filedNumber = dataUser.get(NUMBER_FILED);
            String id = dataUser.get(ID);

            saveRegistry(filedNumber, state, USER, id);
            state = state.equals(CONNECTED) ? "userConnected" : "userDisconnected";

            Map<String, Object> data = new HashMap<>();
            data.put("type", USER);
            data.put(FILED_NUMBER, filedNumber);
            data.put(TYPE_NOTIFICATION, state);
            sendNotification(data, filedNumber);
        }else {
            throw new AffiliationError("Datos incompletos");
        }

    }

    @Override
    public void reschedulingInterviewWeb(String numberFiled, LocalDateTime date) {

        DataStatusAffiliationDTO dataStatusAffiliationDTO = findDataStatusAffiliationDTO(numberFiled);

        Map<String, Object> data = new HashMap<>();
        data.put(NUMBER_FILED,numberFiled);
        data.put("dataStatusAffiliation", dataStatusAffiliationDTO);

        if(date == null){

            data.put("type", USER);
            data.put(TYPE_NOTIFICATION, "schedulingInterviewWeb");

            sendNotification(data, numberFiled);
            return;
        }

        data.put("type", OFFICIAL);
        data.put("date",date);
        data.put(TYPE_NOTIFICATION, "schedulingInterviewWeb");

        sendNotification(data, numberFiled);
    }

    @Override
    public Map<String, String> notificationByFiledNumber(String filedNumber) {

        List<RegistryConnectInterviewWeb> listRegistryConnectInterviewWeb = registryConnectInterviewWebService.findByFiledNumber(filedNumber)
                .stream()
                .sorted(Comparator.comparing(RegistryConnectInterviewWeb::getDate))
                .toList();

        Map<String, String> dates = new HashMap<>();

        dates.put(USER + CONNECTED, null);
        dates.put(OFFICIAL + CONNECTED, null);
        dates.put(USER + DISCONNECTED, null);
        dates.put(OFFICIAL + DISCONNECTED, null);

        listRegistryConnectInterviewWeb.forEach(registry -> {

            String name =(registry.getTypeUser() != null) ?  registry.getTypeUser() + registry.getState() : registry.getState();
            dates.put(name, registry.getDate().toString());

        });

        return dates;
    }



    private AffiliateMercantile findByNumberFiledMercantile(String filedNumber){

        Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        Optional<AffiliateMercantile> optinalAffiliateMercantile = affiliateMercantileRepository.findOne(spec);

        if(optinalAffiliateMercantile.isEmpty()){
            log.error(ERROR + Constant.AFFILIATE_NOT_FOUND);
            return null;
        }

        return optinalAffiliateMercantile.get();

    }

    private Affiliate findByNumberFiled(String filedNumber){

        Specification<Affiliate> spec = AffiliateSpecification.findByField(filedNumber);
        Optional<Affiliate> optionalAffiliate = affiliateRepository.findOne(spec);

        if(optionalAffiliate.isEmpty()){
            log.error(ERROR + Constant.AFFILIATE_NOT_FOUND);
            return null;
        }

        return optionalAffiliate.get();
    }

    private void sendNotification(Map<String, Object> message, String numberFiled){

        messagingTemplate.convertAndSend("/filed/" + numberFiled, message);
    }

    private void saveRegistry(String filedNumber, String state, String type, String id){

        AffiliateMercantile affiliateMercantile = findByNumberFiledMercantile(filedNumber);

        if(affiliateMercantile != null){

            RegistryConnectInterviewWeb registryConnectInterviewWeb = new RegistryConnectInterviewWeb();
            registryConnectInterviewWeb.setDate(LocalDateTime.now());
            registryConnectInterviewWeb.setNumberFiled(filedNumber);
            registryConnectInterviewWeb.setIdUser(Long.parseLong(id));
            registryConnectInterviewWeb.setState(state);
            registryConnectInterviewWeb.setTypeUser(type);
            registryConnectInterviewWebService.save(registryConnectInterviewWeb);


        }

    }

    private DataStatusAffiliationDTO findDataStatusAffiliationDTO(String filedNumber){

        DataStatusAffiliationDTO dataStatusAffiliationDTO = new DataStatusAffiliationDTO();
        AffiliateMercantile affiliateMercantile = findByNumberFiledMercantile(filedNumber);
        Affiliate affiliate = findByNumberFiled(filedNumber);

        if(affiliateMercantile != null && affiliate != null){
            BeanUtils.copyProperties(affiliateMercantile, dataStatusAffiliationDTO);
            BeanUtils.copyProperties(affiliate, dataStatusAffiliationDTO);
            return dataStatusAffiliationDTO;
        }

        return null;
    }

}
