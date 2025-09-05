package com.gal.afiliaciones.application.service.daily.impl;

import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DateInterviewWebSpecification;
import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.TokenDailyDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class DailyServiceImpl implements DailyService {

    private final WebClient webClient;
    private final CollectProperties properties;
    private final DateInterviewWebRepository dateInterviewWebRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;

    @Override
    public DailyRoomsDTO createRoom() {

        String url = properties.getUrlTransversal().concat("daily/createRoom");

        return webClient.post()
                .uri(url) // Cambia a tu endpoint deseado
                .retrieve()
                .bodyToMono(DailyRoomsDTO.class) // Cambia el tipo según la respuesta esperada
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    @Override
    public String createTokenUser(TokenDailyDTO tokenDailyDTO) {

        try {

            String uri = properties.getUrlTransversal()
                    .concat("daily/tokenUser/")
                    .concat(String.valueOf(tokenDailyDTO.getIdRoom()))
                    .concat("/")
                    .concat(tokenDailyDTO.getName())
                    .concat("/")
                    .concat(String.valueOf(tokenDailyDTO.getStart()))
                    .concat("/")
                    .concat(String.valueOf(tokenDailyDTO.getEnd()));

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(new IllegalStateException(e.getMessage())))
                    .block();


        }catch (Exception e){
            throw  new AffiliationError(e.getMessage());
        }
    }

    @Override
    public String createTokenOfficial(Long idOfficial) {
        try {

            UserMain userMain = findUserMainById(idOfficial);
            String name = userMain.getFirstName().concat(" ").concat(userMain.getSurname());
            String uri = properties.getUrlTransversal()
                    .concat("daily/tokenOfficial/")
                    .concat(String.valueOf(idOfficial))
                    .concat("/")
                    .concat(name);

            String token =  webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(new IllegalStateException(e.getMessage())))
                    .block();

            assert token != null;
            return converterToken(token);

        }catch (Exception e){
            throw  new AffiliationError(e.getMessage());
        }
    }

    @Override
    public DataDailyDTO startMeet(Long idOfficial) {

        List<DateInterviewWeb> allInterviewOfficial = findByIdOfficial(idOfficial);

        if(allInterviewOfficial.isEmpty())
            throw new AffiliationError(Constant.NOT_FOUND_INTERVIEW_WEB);

       DateInterviewWeb interviewWeb = null;
       Long timeCurrent = convertHourLong(LocalTime.now());
       long diferenceTimes = timeCurrent;

       for(DateInterviewWeb interview : allInterviewOfficial){

           Long timeInterview = convertHourLong(interview.getHourStart());
           long time = timeCurrent - timeInterview;

           if(Math.abs(time) <= diferenceTimes){
               diferenceTimes = time;
               interviewWeb = interview;
           }

       }

       if(interviewWeb == null){
           throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
       }

       DataDailyDTO dataDailyDTO = dataDaily(interviewWeb.getIdRoom());
       dataDailyDTO.setToken(interviewWeb.getTokenInterview());

        return dataDailyDTO;
    }

    public DataDailyDTO dataDaily(Long idRoom) {
        try {

            String uri = properties.getUrlTransversal().concat("daily/dataDaily/").concat(String.valueOf(idRoom));

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(DataDailyDTO.class)
                    .onErrorResume(e -> Mono.error(new IllegalStateException(e.getMessage())))
                    .block();

        }catch (Exception e){
            throw  new AffiliationError(e.getMessage());
        }
    }

    @Override
    public List<DailyRoomsDTO> findAllRooms() {

        try {

            String uri = properties.getUrlTransversal() + "daily/findAllDailyRoom";

            return webClient.get()
                    .uri(String.format(uri))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<DailyRoomsDTO>>() {})
                    .timeout(Duration.ofSeconds(60))
                    .block();

        }catch(Exception e){
            throw new AffiliationError(e.getMessage());
        }

    }

    private List<DateInterviewWeb> findByIdOfficial(Long idOfficial){
        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByOfficialAndDay(idOfficial, LocalDate.now());
        return dateInterviewWebRepository.findAll(spec);
    }

    private Long convertHourLong(LocalTime time){
        return time.toNanoOfDay();
    }

    private UserMain findUserMainById(Long id){
        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private String converterToken(String token){

        return  token.replaceAll("[ {:}\"]","")
                .replace("token", "");
    }

}
