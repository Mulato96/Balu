package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.InterviewsOfficialsView;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.DateInterviewWebSpecification;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.TokenDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.ActiveRoleDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.ActiveRolesResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.RoleResponseDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ScheduleInterviewWebServiceImpl implements ScheduleInterviewWebService {

    private final SendEmails sendEmails;
    private final DailyService dailyService;
    private final CollectProperties properties;
    private final WebClient webClient;
    private final AffiliateRepository affiliateRepository;
    private final DateInterviewWebRepository dateInterviewWebRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    private static final long MINUTE = 60000;
    private static final String MESSAGE_NOT_FIND_SCHEDUL = "No se encontro el registro de la agenda";
    private static final String NAME_ROLE = "Funcionario";
    private static final String NAME_PROFILE ="Entrevista web";

    @Override
    public Map<String, Object> createScheduleInterviewWeb(DateInterviewWebDTO dateInterviewWebDTO) {


        if(findOne(dateInterviewWebDTO.getIdAffiliate()).isPresent())
            throw new AffiliationError("La afiliacion ya tiene una entrevista web agendada");

        if(validDay(dateInterviewWebDTO.getDay())) {
            throw new AffiliationError("El día de la fecha no es hábil para agendar reuniones");
        }

        if(validateDay(dateInterviewWebDTO.getDay(), dateInterviewWebDTO.getHourStart())) {
            throw new AffiliationError("La fecha es mayor al tiempo permitido, el tiempo permitido es " + properties.getInterviewWebDaysLimit() + " dias");
        }

        if(validateMeshWithRequest(calculateMeshTimetable(dateInterviewWebDTO.getDay()), LocalDateTime.of(dateInterviewWebDTO.getDay(),dateInterviewWebDTO.getHourStart()))) {
            throw new AffiliationError("La hora agendada no corresponde con ninguna de las horas registradas en la malla de los horarios disponibles");
        }

        if(validationGeneral(dateInterviewWebDTO)) {
            throw new AffiliationError("Las Horas o el dia no se encuentran disponibles");
        }

        if(validateTimeMeet(dateInterviewWebDTO)) {
            throw new AffiliationError("La duracion de la reunion no es la permitida, la reunion debe ser de " + properties.getInterviewWebTimeDuration() + " minutos");
        }

        if(calculateSchedule(dateInterviewWebDTO.getHourStart(), dateInterviewWebDTO.getDay()))
            throw new AffiliationError("El horario ya se encuentra agendado!!");

        if(dateInterviewWebDTO.getName() == null && dateInterviewWebDTO.getSurname() == null){
            throw new AffiliationError("El nombre y apellido no deben ser vacios");
        }

        // Correo confirmar entrevista Web
        DateInterviewWebDTO dateInterviewWeb = schedule(dateInterviewWebDTO);


        sendEmail(dateInterviewWebDTO.getIdAffiliate(), LocalDateTime.of(dateInterviewWeb.getDay(), dateInterviewWeb.getHourStart()) ,dateInterviewWeb.getIdOfficial());
        return Map.of("dataDailyDTO", Objects.requireNonNull(findDataDaily(dateInterviewWebDTO.getIdAffiliate())), "dateInterview", LocalDateTime.of(dateInterviewWeb.getDay(), dateInterviewWeb.getHourStart()));

    }

    @Override
    public List<DateInterviewWeb> listScheduleInterviewWeb() {
        return findAllInterview();
    }

    @Override
    public String deleteInterviewWeb(String idAffiliate) {

        DateInterviewWeb interview = findOne(idAffiliate)
                .orElseThrow(() -> new AffiliationError(MESSAGE_NOT_FIND_SCHEDUL));

        Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.of(interview.getDay(), interview.getHourStart()));

        if(duration.abs().toMinutes() >= 61){
            dateInterviewWebRepository.delete(interview);
            AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(idAffiliate);
            affiliateMercantile.setStageManagement(Constant.SCHEDULING);

            return "Entrevista Web cancelada correctamente. Tu cita para la entrevista web del " + interview.getDay() + ", quedó cancelada";
        }

        throw new AffiliationError("Algo salió mal. Tu cita para la entrevista web del día " + interview.getDay() + " , no se pudo cancelar");
    }

    @Override
    public String deleteInterviewWebReSchedule(String idAffiliate) {

        DateInterviewWeb interview = findOne(idAffiliate)
                .orElseThrow(() -> new AffiliationError(MESSAGE_NOT_FIND_SCHEDUL));

            dateInterviewWebRepository.delete(interview);
            AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(idAffiliate);
            affiliateMercantile.setStageManagement(Constant.SCHEDULING);

            return "Entrevista Web cancelada correctamente. Tu cita para la entrevista web del " + interview.getDay() + ", quedó cancelada";

    }

    @Override
    public void delete(String idAffiliate) {

        DateInterviewWeb interview = findOne(idAffiliate)
                .orElseThrow(() -> new AffiliationError(MESSAGE_NOT_FIND_SCHEDUL));
        dateInterviewWebRepository.delete(interview);

    }

    @Override
    public List<Map<String, LocalDateTime>> meshTimetable(LocalDate date) {

        if(validDay(date)){
            throw new AffiliationError("El día de la fecha no es hábil para agendar reuniones");
        }

        return calculateMeshTimetable(date);
    }

    @Override
    public LocalDate calculateDaysSkilled(LocalDate date) {

        if(!validateDay(date, null)){

            int i = 0;
            LocalDate dateCount = LocalDate.now();

            while( i < properties.getInterviewWebDaysLimit()){

                if(!validDay(dateCount))
                    i++;

                dateCount = dateCount.plusDays(1);

            }

            return dateCount;
        }

        throw new AffiliationError("La fecha es mayor al tiempo permitido, el tiempo permitido es " + properties.getInterviewWebDaysLimit() + " dias");
    }

    private DateInterviewWebDTO schedule(@NotNull DateInterviewWebDTO requestSchedule){

        Long idRoom = validRoom(requestSchedule.getHourStart(), requestSchedule.getDay());

        requestSchedule.setIdRoom((idRoom == null ? createRoom() : idRoom));

        if(requestSchedule.getIdRoom() == null)
            throw new AffiliationError("No se encontraron salas disponibles");

        List<UserMain> listUserMain = findAllOfficial();

        requestSchedule.setOnlyAuthorized(listUserMain.size() == 1);

        Map<Long, Long> ids = dateInterviewWebRepository.findInterviewsOfficial()
                .stream()
                .collect(
                        Collectors.toMap(
                                InterviewsOfficialsView::getId,
                                InterviewsOfficialsView::getTotalInterviewWeb
                        )
                );

        if(Boolean.TRUE.equals(ids.isEmpty())){
            requestSchedule.setIdOfficial(listUserMain.get(0).getId());
            return saveInterview(requestSchedule);
        }


        listUserMain.forEach(official -> ids.putIfAbsent(official.getId(), 0L));

        Map<Long, Long> officialIds = ids.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new)
                );

        boolean uniqueOfficial = requestSchedule.getOnlyAuthorized();

        Long listOfficials = officialIds.entrySet()
                .stream()
                .map(idOfficial ->  listUserMain
                                    .stream()
                                    .filter(user -> (Objects.equals(user.getId(), idOfficial.getKey()) && validOfficial(uniqueOfficial, requestSchedule, idOfficial.getKey())))
                                    .findFirst()
                                    .orElse(null))
                .filter(Objects::nonNull)
                .map(UserMain::getId)
                .findFirst()
                .orElse(null);


        requestSchedule.setIdOfficial(listOfficials);


        if(requestSchedule.getIdOfficial() != null)
            return saveInterview(requestSchedule);

       throw new AffiliationError("Error!, no se puede agendar la entrevista porque no hay funcionarios disponibles");
    }

    private boolean validOfficial(boolean uniqueOfficial, DateInterviewWebDTO requestSchedule,Long idInterviewWeb){

        List<DateInterviewWeb> listDateInterviewWeb = dateInterviewWebRepository.findByIdOfficial(idInterviewWeb);

        if(listDateInterviewWeb.isEmpty())
            return true;

        return listDateInterviewWeb.stream()
                .anyMatch(dateInterviewWeb ->
                        (Boolean.TRUE.equals(uniqueOfficial)
                                ? !validateDateOfficial(dateInterviewWeb, requestSchedule)
                                : !validateDateOfficialNotAuthorized(dateInterviewWeb, requestSchedule)));

    }

    private boolean validateDateOfficialNotAuthorized(DateInterviewWeb list, DateInterviewWebDTO request){

        return  (request.getHourStart().toNanoOfDay() == list.getHourEnd().toNanoOfDay()) ||
                (list.getHourStart().toNanoOfDay() == request.getHourEnd().toNanoOfDay());

    }

    private boolean validateDateOfficial(DateInterviewWeb list, DateInterviewWebDTO request){
        return list.getHourStart().equals(request.getHourStart());
    }

    private boolean validateMeshWithRequest(List<Map<String, LocalDateTime>> listDate, LocalDateTime dateRequest){

        for(Map<String, LocalDateTime> map : listDate){
            if(map.get("date").equals(dateRequest)){
                return false;
            }
        }

        return true;
    }

    private boolean validationGeneral(DateInterviewWebDTO requestSchedule){

        long hourOne = properties.getInterviewWebHourStartLunch() * 3600000000000L;
        long hourTwo = properties.getInterviewWebHourEndLunch() * 3600000000000L;

        return  (requestSchedule.getHourStart().toNanoOfDay() >= hourOne && requestSchedule.getHourStart().toNanoOfDay() < hourTwo);

    }

    private boolean validateTimeMeet (DateInterviewWebDTO requestSchedule){

        long timeMeet = MINUTE * properties.getInterviewWebTimeDuration() * 1000000;
        return  ((requestSchedule.getHourEnd().toNanoOfDay() - requestSchedule.getHourStart().toNanoOfDay()) != timeMeet);
    }

    private boolean calculateSchedule(LocalTime startRequest, LocalDate day){

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByHourStart(startRequest, day);
        return dateInterviewWebRepository.findAll(spec).stream().count() >= properties.getMaxConcurrentMeetings();

    }

    private List<Map<String, LocalDateTime>> calculateMeshTimetable(LocalDate date){

        List<DateInterviewWeb> listInterviewWed = findAllByDay(date);
        List<Map<String, LocalDateTime>> listTime = new ArrayList<>();

        LocalTime now = LocalTime.of(LocalTime.now().getHour(), validMinute(LocalTime.now().getMinute()), 0);

        long start = (date.equals(LocalDate.now()) ? now.toSecondOfDay() * 1000 : properties.getInterviewWebHourStart() * MINUTE * 60);
        long end = properties.getInterviewWebHourEnd() * MINUTE * 60;
        long timeInterview = properties.getInterviewWebTimeDuration() * MINUTE;
        long dateNow = LocalDateTime.of(date.getYear(),date.getMonth(),date.getDayOfMonth(),0,0,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + start;
        long startLunch = (properties.getInterviewWebHourStartLunch() * MINUTE * 60) - timeInterview;
        long endLunch = properties.getInterviewWebHourEndLunch() * MINUTE * 60;

        for(long i =  start ; i < end ; i += timeInterview){

            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateNow), ZoneId.systemDefault());

            boolean flag = listInterviewWed
                    .stream()
                    .map(DateInterviewWeb::getHourStart)
                    .filter(t -> t.equals(time.toLocalTime()))
                    .count() >= properties.getMaxConcurrentMeetings();

            if((i <= startLunch  || i >= endLunch) && !flag){
                listTime.add(Map.of("date", time));
            }

            dateNow += timeInterview ;
        }

        return listTime;
    }

    private boolean validateDay(LocalDate day, LocalTime time){

        time =  time != null ? time : LocalTime.now().plusMinutes(1);

        LocalDateTime date = LocalDateTime.of(day, time);

        if (!date.isBefore(LocalDateTime.now())) {

            int i = 0;
            LocalDate dateCount = LocalDate.now();

            while( i <= properties.getInterviewWebDaysLimit()){

                if(dateCount.equals(day))
                    return false;

                if(!validDay(dateCount))
                    i++;

                dateCount = dateCount.plusDays(1);

            }

            return true;
        }

        throw new AffiliationError("Error!!, La fecha de la reunion es menor a la actual");
    }

    private boolean validDay(LocalDate day){

        DayOfWeek dayOfWeek = day.getDayOfWeek();
        return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

    }

    private void sendEmail(String idAffiliation, LocalDateTime dateInterview, Long idOfficial){

        AffiliateMercantile affiliateMercantile = getAffiliateMercantileByFieldNumber(idAffiliation);
        Affiliate affiliate = findByFiledNumberAffiliate(affiliateMercantile.getFiledNumber());

        TemplateSendEmailsDTO templateSendEmailsDTO = new TemplateSendEmailsDTO();
        UserMain userMain = iUserPreRegisterRepository.findById(affiliateMercantile.getIdUserPreRegister())
                .orElseThrow( () -> new AffiliationError("Error, usuario no encontrado"));

        affiliateMercantile.setDateInterview(dateInterview);
        affiliateMercantile.setStageManagement(Constant.INTERVIEW_WEB);
        affiliateMercantile = affiliateMercantileRepository.save(affiliateMercantile);

        if(affiliate != null && idAffiliation !=null){

            affiliate.setIdOfficial(idOfficial);
            affiliateRepository.save(affiliate);
        }

        BeanUtils.copyProperties(userMain, templateSendEmailsDTO);
        templateSendEmailsDTO.setDateInterview(dateInterview);
        templateSendEmailsDTO.setBusinessName(affiliateMercantile.getBusinessName());
        templateSendEmailsDTO.setFieldNumber(affiliateMercantile.getFiledNumber());

        sendEmails.confirmationInterviewWeb(templateSendEmailsDTO);
        sendEmailOfficial(idAffiliation, dateInterview);

    }

    private AffiliateMercantile getAffiliateMercantileByFieldNumber(String fieldNumber){

        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByFieldNumber(fieldNumber);
        return affiliateMercantileRepository.findOne(spc)
                .orElseThrow( () -> new UserNotFoundInDataBase(Constant.AFFILIATE_NOT_FOUND));
    }

    private List<UserMain> findAllOfficial(){

        List<Long> idsRoles = Optional.ofNullable(getAllRoles())
                .map(ActiveRolesResponseDTO::getData)
                .map(data -> data.stream()
                        .filter(d -> d.getRoleName().contains(NAME_ROLE))
                        .map(ActiveRoleDTO::getId)
                        .toList())
                .orElse(Collections.emptyList());

        List<String> codes = idsRoles.stream()
                .map(this::getProfileRole)
                .filter(roleResponse -> roleResponse != null && roleResponse.getData() != null)
                .flatMap(roleResponse -> roleResponse.getData().getProfileAndPermission().stream()
                        .flatMap(profile -> profile.getPermissions().stream()
                                .filter(permission -> permission.getPermissionName() != null && permission.getPermissionName().contains(NAME_PROFILE))
                                .map(permission -> roleResponse.getData().getCode())))
                .distinct()
                .toList();


        List<UserMain> listUserMain = new ArrayList<>();
        List<Long> ids = converter(iUserPreRegisterRepository.findAllOfficial(codes));
        ids.forEach(id -> {
            Optional<UserMain> optionalUser = iUserPreRegisterRepository.findById(id);
            optionalUser.ifPresent(listUserMain::add);
        });
        return listUserMain;
    }

    private List<DateInterviewWeb> findAllByDay(LocalDate day){

        try {
            Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByDay(day);
            return dateInterviewWebRepository.findAll(spec);
        }catch (Exception e){
            throw new AffiliationError(e.getMessage());
        }
    }

    private List<DateInterviewWeb> findAllInterview (){

        try {
            return dateInterviewWebRepository.findAll();
        }catch (Exception e){
            throw new AffiliationError(e.getMessage());
        }
    }

    private Optional<DateInterviewWeb> findOne(String idAffiliate){

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByAffiliation(idAffiliate);
        return dateInterviewWebRepository.findOne(spec);
    }

    private DateInterviewWebDTO saveInterview(DateInterviewWebDTO dateInterviewWebDTO){

        DateInterviewWeb dateInterviewWeb =  new DateInterviewWeb();
        BeanUtils.copyProperties(dateInterviewWebDTO, dateInterviewWeb);
        dateInterviewWeb.setTokenInterview(converterToken(generateTokenDaily(dateInterviewWebDTO)));
        dateInterviewWebRepository.save(dateInterviewWeb);
        return dateInterviewWebDTO;

    }

    private String generateTokenDaily(DateInterviewWebDTO dateInterviewWebDTO){

        TokenDailyDTO tokenDailyDTO = new TokenDailyDTO();
        tokenDailyDTO.setIdOfficial(dateInterviewWebDTO.getIdOfficial());
        tokenDailyDTO.setStart(LocalDateTime.of(dateInterviewWebDTO.getDay(), dateInterviewWebDTO.getHourStart()));
        tokenDailyDTO.setEnd(LocalDateTime.of(dateInterviewWebDTO.getDay(), dateInterviewWebDTO.getHourEnd()));
        tokenDailyDTO.setName(dateInterviewWebDTO.getName().concat(" ").concat(dateInterviewWebDTO.getSurname()));
        tokenDailyDTO.setIdRoom(dateInterviewWebDTO.getIdRoom());
        return dailyService.createTokenUser(tokenDailyDTO);
    }

    private List<Long> converter(List<Object> listIds) {
        return listIds.stream()
                .filter(Objects::nonNull)
                .map(id -> Long.valueOf(String.valueOf(id)))
                .toList();
    }

    private String converterToken(String token){

        return  token.replaceAll("[ {:}\"]","")
                .replace("token", "");
    }

    private DataDailyDTO findDataDaily(String idAffiliate){

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByAffiliation(idAffiliate);
        Optional<DateInterviewWeb> optionalDateInterviewWeb  = dateInterviewWebRepository.findOne(spec);

        if(optionalDateInterviewWeb.isPresent()){
            DateInterviewWeb dateInterviewWeb = optionalDateInterviewWeb.get();
            DataDailyDTO dataDailyDTO = dailyService.dataDaily(dateInterviewWeb.getIdRoom());
            dataDailyDTO.setToken(dateInterviewWeb.getTokenInterview());
            return dataDailyDTO;
        }

        return null;
    }

    private void sendEmailOfficial(String idAffiliation, LocalDateTime dateInterview){

        findOne(idAffiliation).
                flatMap(interviewWeb -> iUserPreRegisterRepository.findById(interviewWeb.getIdOfficial())).
                ifPresent(official -> sendEmails.confirmationInterviewWebOfficial(dateInterview, official.getEmail(), idAffiliation));

    }

    private Affiliate findByFiledNumberAffiliate(String filedNumber){
        Specification<Affiliate> spect = AffiliateSpecification.findByField(filedNumber);
        return affiliateRepository.findOne(spect).orElse(null);
    }

    private ActiveRolesResponseDTO getAllRoles() {

        String url = properties.getUrlTransversal().concat("role/findActiveRoles");

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(ActiveRolesResponseDTO.class).block();
    }

    private RoleResponseDTO getProfileRole(Long id){

        String url = properties.getUrlTransversal().concat("role/findByid/").concat(String.valueOf(id));

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(RoleResponseDTO.class).block();
    }

    private Long validRoom(LocalTime hourStart, LocalDate day){

        List<DailyRoomsDTO> listRooms = dailyService.findAllRooms();

        if(listRooms.isEmpty())
            return null;

        Specification<DateInterviewWeb> spec = DateInterviewWebSpecification.findByDay(day);
        List<DateInterviewWeb> listInterviewWeb =  dateInterviewWebRepository.findAll(spec);

        if(listInterviewWeb.isEmpty())
            return listRooms.stream().map(DailyRoomsDTO::getId).findFirst().orElse(null);

        return listRooms.stream()
                .filter(room -> listInterviewWeb
                        .stream()
                        .filter(interview -> Objects.equals(interview.getIdRoom(),room.getId()))
                        .noneMatch(interview ->
                                Math.abs(Duration.between(hourStart, interview.getHourStart()).toMinutes()) <= 30
                        ))
                .map(DailyRoomsDTO::getId)
                .findFirst()
                .orElse(null);

    }

    private Long createRoom(){

        try {

            DailyRoomsDTO dailyRoomsDTO = dailyService.createRoom();
            return dailyRoomsDTO.getId();

        }catch (Exception e){
            return null;
        }
    }

    private int validMinute(int minute){

        if(minute <= 15)
            return 15;
        if(minute >= 15 && minute < 30)
            return 30;
        if(minute >= 30 && minute < 45)
            return 45;

        return 0;

    }

}
