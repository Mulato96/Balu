package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.MainOfficeSpecification;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainOfficeServiceImpl implements MainOfficeService {

    private final SendEmails sendEmails;
    private final MainOfficeRepository repository;
    private final WorkCenterService workCenterService;
    private final AffiliateRepository affiliateRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IEconomicActivityRepository economicActivityRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    private final ArlInformationDao arlInformationDao;

    private static final String ERROR_DELETE = "No puede eliminar este centro de trabajo ya que tienes trabajadores asociados";

    @Override
    public List<MainOfficeGrillaDTO> getAllMainOffices(Long idAffiliateEmployer) {
        return repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(idAffiliateEmployer))
                .stream()
                .map(main -> {

                    MainOfficeGrillaDTO mainOfficeDTO = new MainOfficeGrillaDTO();
                    BeanUtils.copyProperties(main, mainOfficeDTO);
                    return mainOfficeDTO;

                    }).sorted(Comparator.comparing(MainOfficeGrillaDTO::getMain).reversed())
                .toList();
    }

    @Override
    public MainOffice getMainOfficeByCode(String officeCode) {
        return repository.findByCode(officeCode);
    }

    @Override
    public MainOffice saveMainOffice(MainOffice mainOffice){

        return repository.save(mainOffice);
    }

    @Override
    public MainOffice saveMainOffice(MainOfficeDTO mainOfficeDTO){

        if(!findByIdUserAndDepartmentAndCityAndAddress(mainOfficeDTO.getIdAffiliateEmployer(), mainOfficeDTO.getAddressDTO()).isEmpty())
            throw new AffiliationError("La sede ya se encuentra creada, Valida la información y vuelve a intentar");

        if(!repository.findAll(MainOfficeSpecification.findByIdUserAndName(mainOfficeDTO.getIdAffiliateEmployer(), mainOfficeDTO.getMainOfficeName())).isEmpty())
            throw new AffiliationError("La sede ya se encuentra creada, Valida la información y vuelve a intentar");

        mainOfficeDTO.setEconomicActivity(mainOfficeDTO.getEconomicActivity().stream().filter(Objects::nonNull).toList());

        mainOfficeDTO.setMainOfficeZone(mainOfficeDTO.getMainOfficeZone().toUpperCase());

        validMainOffice(mainOfficeDTO);

        MainOffice mainOffice = new MainOffice();

        UserMain userMain = findUserMain(mainOfficeDTO.getOfficeManager());
        Affiliate affiliate =  affiliateRepository.findByIdAffiliate(mainOfficeDTO.getIdAffiliateEmployer())
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        BeanUtils.copyProperties(mainOfficeDTO, mainOffice);
        BeanUtils.copyProperties(mainOfficeDTO.getAddressDTO(), mainOffice);

        mainOffice.setOfficeManager(userMain);
        mainOffice.setIdAffiliate(affiliate.getIdAffiliate());

        mainOffice.setCode(findCode());

        if(Boolean.TRUE.equals(mainOfficeDTO.getMain()))
            changeMain(mainOffice.getIdAffiliate(), mainOffice);

        if(repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(mainOfficeDTO.getIdAffiliateEmployer())).isEmpty())
            mainOffice.setMain(true);

        MainOffice newMainOffice = repository.save(mainOffice);
        createWorkCentersByMainOffice(mainOfficeDTO, newMainOffice, userMain);

        sendEmail(affiliate);

        return newMainOffice;
    }

    private void createWorkCentersByMainOffice(MainOfficeDTO mainOfficeDTO, MainOffice newMainOffice, UserMain userMain){
        List<EconomicActivity> economicActivityList =  findAllActivityEconomicById(mainOfficeDTO.getEconomicActivity());
        economicActivityList.forEach(economic -> workCenter(economic, userMain, newMainOffice, Boolean.TRUE));

        List<EconomicActivity> economicActivityEmployer = findEconomicActivitiesByEmployer(mainOfficeDTO.getIdAffiliateEmployer());

        List<EconomicActivity> diferentList = new ArrayList<>(economicActivityEmployer);
        diferentList.removeAll(economicActivityList);

        diferentList.forEach(economic -> workCenter(economic, userMain, newMainOffice, Boolean.FALSE));
    }

    private List<EconomicActivity> findEconomicActivitiesByEmployer(Long idAffiliate){
        List<EconomicActivity> economicActivityList = new ArrayList<>();
        Object affiliate = findAffiliateMercantile(idAffiliate);

        if(affiliate instanceof AffiliateMercantile affiliateMercantile){
            economicActivityList = affiliateMercantile.getEconomicActivity()
                    .stream()
                    .map(AffiliateActivityEconomic::getActivityEconomic).toList();
        }

        if(affiliate instanceof Affiliation affiliation) {
            economicActivityList = affiliation.getEconomicActivity()
                    .stream()
                    .map(AffiliateActivityEconomic::getActivityEconomic).toList();
        }

        return economicActivityList;
    }

    @Override
    public MainOffice findById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Sede no encontrada"));
    }

    @Override
    public MainOfficeDTO findId(Long id) {
        MainOffice mainOffice = findById(id);
        MainOfficeDTO mainOfficeDTO = new MainOfficeDTO();
        AddressDTO addressDTO = new AddressDTO();

        Object affiliate = findAffiliateMercantile(mainOffice.getIdAffiliate());

        if(affiliate instanceof AffiliateMercantile affiliateMercantile){
            List<WorkCenter> workCenterList = workCenterService.getWorkCenterByMainOffice(mainOffice);
            List<Long> activityEconomic = new ArrayList<>();
            workCenterList.forEach(workCenter -> {
                if(workCenter.getIsEnable().booleanValue()) {
                    List<EconomicActivity> economicActivity = economicActivityRepository.findByEconomicActivityCode(workCenter.getEconomicActivityCode());
                    activityEconomic.add(economicActivity.get(0).getId());
                }
            });

            BeanUtils.copyProperties(mainOffice, mainOfficeDTO);
            BeanUtils.copyProperties(mainOffice,addressDTO);

            mainOfficeDTO.setAddressDTO(addressDTO);
            mainOfficeDTO.setOfficeManager(mainOffice.getOfficeManager().getId());
            mainOfficeDTO.setEconomicActivity(activityEconomic);


            return mainOfficeDTO;
        }

        if(affiliate instanceof Affiliation affiliation) {

            List<WorkCenter> workCenterList = workCenterService.getWorkCenterByMainOffice(mainOffice);
            List<Long> activityEconomic = new ArrayList<>();
            workCenterList.forEach(workCenter -> {
                if(workCenter.getIsEnable().booleanValue()) {
                    List<EconomicActivity> economicActivity = economicActivityRepository.findByEconomicActivityCode(workCenter.getEconomicActivityCode());
                    activityEconomic.add(economicActivity.get(0).getId());
                }
            });

            BeanUtils.copyProperties(mainOffice, mainOfficeDTO);
            BeanUtils.copyProperties(mainOffice, addressDTO);

        mainOfficeDTO.setAddressDTO(addressDTO);
        mainOfficeDTO.setOfficeManager(mainOffice.getOfficeManager().getId());
        mainOfficeDTO.setEconomicActivity(activityEconomic);

            return mainOfficeDTO;
        }

        throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
    }

    @Override
    public List<MainOfficeGrillaDTO> findByIdUserAndDepartmentAndCity(Long idUser, Long department, Long city) {

        return repository.findAll(MainOfficeSpecification.findByIdUserAndDepartmentAndCity(idUser, department, city))
                .stream()
                .map(main -> {

                    MainOfficeGrillaDTO mainOfficeDTO = new MainOfficeGrillaDTO();
                    BeanUtils.copyProperties(main, mainOfficeDTO);
                    return mainOfficeDTO;

                })
                .toList();
    }

    @Override
    public MainOffice update(MainOfficeDTO mainOfficeDTO, Long id) {

        MainOffice mainOffice = findById(id);

        mainOfficeDTO.setEconomicActivity(mainOfficeDTO.getEconomicActivity().stream().filter(Objects::nonNull).toList());

        if(!mainOffice.getMainOfficeName().equals(mainOfficeDTO.getMainOfficeName())){

            List<MainOffice> list = repository.findAll(MainOfficeSpecification.findByIdUserAndName(mainOfficeDTO.getOfficeManager(), mainOfficeDTO.getMainOfficeName()))
                    .stream()
                    .filter(main -> main.getMainOfficeName().equals(mainOfficeDTO.getMainOfficeName()))
                    .toList();

            list.forEach(main ->{
                if(!Objects.equals(main.getId(), id)){
                    throw new AffiliationError("La sede ya se encuentra registrada");
                }
            });
        }

        validMainOffice(mainOfficeDTO);

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(mainOfficeDTO.getIdAffiliateEmployer())
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        String code = mainOffice.getCode();

        BeanUtils.copyProperties(mainOfficeDTO, mainOffice);
        BeanUtils.copyProperties(mainOfficeDTO.getAddressDTO(), mainOffice);

        List<EconomicActivity> economicActivityList =  findAllActivityEconomicById(mainOfficeDTO.getEconomicActivity());

        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findOne(AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber()))
                .orElse(null);

        if(affiliateMercantile!=null){
            if(changedEconomicActivitiesList(economicActivityList, affiliateMercantile) && affiliateMercantile.getEconomicActivity() != null) {
                List<EconomicActivity> economicActivitiesEmployer = affiliateMercantile.getEconomicActivity()
                        .stream()
                        .map(AffiliateActivityEconomic::getActivityEconomic).toList();

                List<EconomicActivity> diferentList = new ArrayList<>(economicActivitiesEmployer);
                diferentList.removeAll(economicActivityList);

                diferentList.forEach(economic -> {
                        if (!economicActivityList.isEmpty() && economicActivityList.get(0) != null)
                            validWorkedAssociatedToWorkCenter(mainOffice.getId(), economic, affiliate);
                    });
            }
        }else{
            Affiliation affiliationDomestic = domesticServiceIndependentRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElse(null);

            if (affiliationDomestic != null &&
                    changedEconomicActivitiesDomestic(economicActivityList, affiliationDomestic) &&
                    affiliationDomestic.getEconomicActivity() != null) {
                List<EconomicActivity> economicActivitiesEmployer = affiliationDomestic.getEconomicActivity()
                        .stream()
                        .map(AffiliateActivityEconomic::getActivityEconomic).toList();

                List<EconomicActivity> diferentList = new ArrayList<>(economicActivitiesEmployer);
                diferentList.removeAll(economicActivityList);

                diferentList.forEach(economic -> {
                    if (!economicActivityList.isEmpty() && economicActivityList.get(0) != null)
                        validWorkedAssociatedToWorkCenter(mainOffice.getId(), economic, affiliate);
                });
            }
        }

        UserMain userMain = findUserMain(mainOfficeDTO.getOfficeManager());
        updateWorkCenter(economicActivityList, mainOffice, userMain);

        if(Boolean.TRUE.equals(mainOfficeDTO.getMain()))
            changeMain(mainOfficeDTO.getIdAffiliateEmployer(), mainOffice);

        mainOffice.setCode(code);

        if(repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(mainOfficeDTO.getIdAffiliateEmployer())).size() == 1)
            mainOffice.setMain(true);

        MainOffice newMainOffice = repository.save(mainOffice);

        sendEmail(affiliate);

        return newMainOffice;

    }

    private boolean changedEconomicActivitiesList(List<EconomicActivity> economicActivityList, AffiliateMercantile affiliateMercantile) {
        if (affiliateMercantile != null && affiliateMercantile.getEconomicActivity() != null &&
                economicActivityList.size() != affiliateMercantile.getEconomicActivity().size())
            return true;

            Set<Long> idsEconomicActivityEmployer = affiliateMercantile.getEconomicActivity().stream()
                    .map(AffiliateActivityEconomic::getId).collect(Collectors.toSet());

            return economicActivityList.stream().map(EconomicActivity::getId)
                    .anyMatch(idsEconomicActivityEmployer::contains);

    }

    private boolean changedEconomicActivitiesDomestic(List<EconomicActivity> economicActivityList, Affiliation affiliation) {
        if (affiliation != null && affiliation.getEconomicActivity() != null &&
                economicActivityList.size() != affiliation.getEconomicActivity().size())
            return true;

        Set<Long> idsEconomicActivityEmployer = affiliation.getEconomicActivity().stream()
                .map(AffiliateActivityEconomic::getId).collect(Collectors.toSet());

        return economicActivityList.stream().map(EconomicActivity::getId)
                .anyMatch(idsEconomicActivityEmployer::contains);

    }

    @Override
    public String delete(Long id, Long idAffiliateEmployer) {

        MainOffice mainOffice = findById(id);

        if(Boolean.TRUE.equals(mainOffice.getMain()))
            throw new AffiliationError("No se puede eliminar la sede, porque es la sede principal");

        Affiliate affiliate = affiliateRepository.findByIdAffiliate(idAffiliateEmployer)
                .orElseThrow(() -> new AffiliateNotFound("Affiliate employer not found"));

        List<AffiliateActivityEconomic> economicActivityList = new ArrayList<>();
        if(affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
            AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findOne(AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber()))
                    .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

            economicActivityList = affiliateMercantile.getEconomicActivity();
        }

        if(affiliate.getAffiliationSubType().equals(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES)) {
            Affiliation affiliationDomestic = domesticServiceIndependentRepository.findByFiledNumber(affiliate.getFiledNumber())
                    .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

            economicActivityList = affiliationDomestic.getEconomicActivity();
        }

        economicActivityList
                .forEach(main -> validWorkedAssociated(mainOffice.getId(), main.getActivityEconomic().getId(), affiliate));

        if(repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(idAffiliateEmployer)).size() <= 1)
            throw new AffiliationError("No se puede eliminar la sede, debe quedar minimo una sede.");

        repository.delete(mainOffice);

        sendEmail(affiliate);

        return "OK";

    }

    @Override
    public String findCode(){
        long nextNumber = repository.nextConsecutiveCodeMainOffice();
        return String.format("%s%010d", "S", nextNumber);
    }

    private UserMain findUserMain(Long id){
        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private List<EconomicActivity> findAllActivityEconomicById(List<Long> ids){
        return ids.stream()
                .map(id -> economicActivityRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro la actividad economica con id: " + id)))
                .toList();
    }

    private void validMainOffice(MainOfficeDTO mainOfficeDTO){

        validEmpty(String.valueOf(mainOfficeDTO.getAddressDTO().getIdCity()), "El campo ciudad no puede ser vacio");
        validEmpty(String.valueOf(mainOfficeDTO.getAddressDTO().getIdDepartment()), "El campo departamento no puede ser vacio");
        validEmpty(mainOfficeDTO.getMainOfficeEmail(),"El campo email no puede ser vacio");
        validNumberPhone(mainOfficeDTO.getMainOfficePhoneNumber(), true);
        validNumberPhone(mainOfficeDTO.getMainPhoneNumberTwo(), false);
        validNumberPhone(mainOfficeDTO.getPhoneOneResponsibleHeadquarters(), true);
        validNumberPhone(mainOfficeDTO.getPhoneTwoResponsibleHeadquarters(), false);
        validNames(mainOfficeDTO.getFirstNameResponsibleHeadquarters(), true, 50, "Nombre incorrecto");
        validNames(mainOfficeDTO.getSecondNameResponsibleHeadquarters(), false, 100, "Segundo nombre incorrecto");
        validNames(mainOfficeDTO.getSurnameResponsibleHeadquarters(), true, 50, "Apellido incorrecto");
        validNames(mainOfficeDTO.getSecondSurnameResponsibleHeadquarters(), false, 100, "Segundo apellido incorrecto");

        if(validEmpty(mainOfficeDTO.getMainOfficeName()) && mainOfficeDTO.getMainOfficeName().length() >= 101)
            throw new AffiliationError("El nombre de la sede excede el tamaño permitido, el tamaño debe ser menor a 100 caracteres");

        if(validEmail(mainOfficeDTO.getMainOfficeEmail()) && validEmail(mainOfficeDTO.getEmailResponsibleHeadquarters()))
            throw new AffiliationError("El correo tiene un formato incorrecto");

        if(!List.of(Constant.URBAN_ZONE, Constant.RURAL_ZONE).contains(mainOfficeDTO.getMainOfficeZone()))
            throw new AffiliationError("La zona no corresponde a ninguna de las indicadas");

        if(mainOfficeDTO.getEconomicActivity() == null || mainOfficeDTO.getEconomicActivity().isEmpty() || mainOfficeDTO.getEconomicActivity().size() >= 6)
            throw new AffiliationError("Error, se debe contar con minimo 1 y maximo 5 actividades economicas");

        if(!validTypeNumberIdentification(mainOfficeDTO.getTypeDocumentResponsibleHeadquarters()) &&
                !validNumberIdentification(mainOfficeDTO.getNumberDocumentResponsibleHeadquarters(), mainOfficeDTO.getTypeDocumentResponsibleHeadquarters()))
            throw new AffiliationError("Tipo o numero de documento incorrectos");

    }

    private boolean validEmpty(String data){
        return data == null || data.isEmpty();
    }

    private void validEmpty(String data, String message){
        if (data == null || data.isEmpty())
            throw new AffiliationError(message);
    }

    private void validNumberPhone(String number, boolean requested){

        boolean isEmpty = validEmpty(number);

        List<String> codesPhone = List.of("6", "3");

        if(requested && isEmpty)
            throw new AffiliationError(Constant.PHONE);

        if(!isEmpty && !codesPhone.contains(number.substring(0, 1)))
            throw new AffiliationError(Constant.PHONE);

    }

    private boolean validEmail(String email){

        if(!validEmpty(email))
            return !(email.length() <= 100 && email.matches("^[^\\s@]+@(?=[a-zA-Z\\-]+\\.[a-zA-Z]{2,})(?!-)[a-zA-Z\\-]+(?<!-)\\.[a-zA-Z]{2,}$"));
        return true;
    }

    private void changeMain(Long idAffiliateEmployer, MainOffice mainOffice){
        repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(idAffiliateEmployer)).forEach(main -> {
            main.setMain(false);
            repository.save(main);
        });
        mainOffice.setMain(true);
    }

    private void workCenter(EconomicActivity economicActivity, UserMain user, MainOffice mainOffice, Boolean isEnable){

        String codeActivityEconomic = codeActivityEconomic(economicActivity);

        if(workCenterService.getWorkCenterByCodeAndIdUser(codeActivityEconomic, user) == null){

            Long code = workCenterService.getNumberCode(user) + 1;

            WorkCenter workCenter = new WorkCenter();

            workCenter.setCode(String.valueOf(code));
            workCenter.setEconomicActivityCode(codeActivityEconomic);
            workCenter.setTotalWorkers(0);
            workCenter.setRiskClass(economicActivity.getClassRisk());
            workCenter.setWorkCenterDepartment(mainOffice.getIdDepartment());
            workCenter.setWorkCenterCity(mainOffice.getIdCity());
            workCenter.setWorkCenterZone(mainOffice.getMainOfficeZone());
            workCenter.setWorkCenterManager(user);
            workCenter.setMainOffice(mainOffice);
            workCenter.setIdAffiliate(mainOffice.getIdAffiliate());
            workCenter.setIsEnable(isEnable);
            workCenterService.saveWorkCenter(workCenter);

        }

    }

    private void updateWorkCenter(List<EconomicActivity> economicActivityList, MainOffice mainOffice, UserMain userMain){
        List<WorkCenter> workcenterList = workCenterService.getWorkCenterByMainOffice(mainOffice);
        //Deshabilitar todos los centros de trabajo
        workcenterList.forEach(workCenter -> {
            workCenter.setIsEnable(Boolean.FALSE);
            workCenterService.saveWorkCenter(workCenter);
        });

        List<String> newActivities = economicActivityList.stream()
                .map(EconomicActivity::getEconomicActivityCode).toList();

        if(!newActivities.isEmpty()){
            //habilitar los centros de trabajo seleccionados
            newActivities.forEach(economicActivityCode -> {
                WorkCenter workCenter = workCenterService.
                        getWorkCenterByEconomicActivityAndMainOffice(economicActivityCode, mainOffice.getId());
                if(workCenter!=null) {
                    workCenter.setIsEnable(Boolean.TRUE);
                    workCenterService.saveWorkCenter(workCenter);
                }else{
                    EconomicActivity economic = economicActivityRepository.findByEconomicActivityCode(economicActivityCode).get(0);
                    workCenter(economic, userMain, mainOffice, Boolean.TRUE);
                }
            });
        }
    }

    private String codeActivityEconomic(EconomicActivity economicActivity){

        return String.join("", economicActivity.getClassRisk(), economicActivity.getCodeCIIU(), economicActivity.getAdditionalCode());
    }

    private boolean validTypeNumberIdentification(String typeNumber){
        if(!validEmpty(typeNumber))
            return List.of("CC", "NI",  "CE", "TI", "RC", "PA", "CD", "PE", "SC", "PT").contains(typeNumber);
        return false;
    }

    private boolean validNumberIdentification(String number, String type){

        if(!validEmpty(number))
            return switch (type) {
                case "CC", "CE" -> (number.length() >= 3 && number.length() <= 10);
                case "NI" -> (number.length() >= 9 && number.length() <= 12);
                case "TI", "RC" -> (number.length() >= 10 && number.length() <= 11);
                case "PA" -> (number.length() >= 3 && number.length() <= 16);
                case "CD" -> (number.length() >= 3 && number.length() <= 11);
                case "PE" -> (number.length() == 15);
                case "SC" -> (number.length() == 9);
                case "PT" -> (!number.isEmpty() && number.length() <= 8);
                default -> false;
            };
        return false;

    }

    private void validNames(String name, boolean requested, long length, String message){

        boolean isEmpty = validEmpty(name);

        if(requested && isEmpty)
            throw new AffiliationError(message);

        if(!isEmpty && !(name.length() <= length && name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$")))
            throw new AffiliationError(message);

    }

    private List<MainOffice> findByIdUserAndDepartmentAndCityAndAddress(Long idUser, AddressDTO addressDTO){
        return repository.findAll(MainOfficeSpecification.findByIdUserAndDepartmentAndCityAndAddress(idUser, addressDTO));
    }

    private void validWorkedAssociated(Long idHeadquarter, Long idEconomicActivity, Affiliate affiliate){

        try {

            if(idEconomicActivity != null){

                List<String> filedNumbers = affiliationDependentRepository.findAll(AffiliationDependentSpecification.findDependentsByIdAffiliateEmployer(affiliate.getIdAffiliate()))
                        .stream()
                        .map(AffiliationDependent::getFiledNumber)
                        .toList();

                if(!filedNumbers.isEmpty() &&
                        (!affiliationDependentRepository.findAll(AffiliationDependentSpecification.findByFiledNumberList(filedNumbers))
                                .stream()
                                .filter(dependent -> Objects.equals(dependent.getIdHeadquarter(), idHeadquarter))
                                .toList()
                                .isEmpty()))
                    throw new AffiliationError(ERROR_DELETE);

                Object affiliation = findAffiliation(affiliate.getFiledNumber());

                if(affiliation instanceof AffiliateMercantile affiliateMercantile && Objects.equals(affiliateMercantile.getIdMainHeadquarter(), idHeadquarter))
                    throw new AffiliationError(ERROR_DELETE);

                if(affiliation instanceof Affiliation affiliationDomestic && Objects.equals(affiliationDomestic.getIdMainHeadquarter(), idHeadquarter))
                    throw new AffiliationError(ERROR_DELETE);


            }

        }catch (AffiliationError e){
            throw e;
        }catch (Exception e){
            throw new AffiliationError("Ocurrio un error");
        }

    }

    private void validWorkedAssociatedToWorkCenter(Long idHeadquarter, EconomicActivity economicActivity, Affiliate affiliate){

        try {

            if(economicActivity != null){
                WorkCenter workCenter = workCenterService.getWorkCenterByEconomicActivityAndMainOffice(economicActivity.getEconomicActivityCode(), idHeadquarter);

                if(workCenter!=null) {
                    List<String> filedNumbers = affiliationDependentRepository.findAll(AffiliationDependentSpecification.findDependentsByIdAffiliateEmployer(affiliate.getIdAffiliate()))
                            .stream()
                            .map(AffiliationDependent::getFiledNumber)
                            .toList();

                    if (!filedNumbers.isEmpty() &&
                            (!affiliationDependentRepository.findAll(AffiliationDependentSpecification.findByEconomicActivityAndEmployer(filedNumbers, economicActivity.getEconomicActivityCode()))
                                    .stream()
                                    .filter(dependent -> Objects.equals(dependent.getIdWorkCenter(), workCenter.getId()))
                                    .toList()
                                    .isEmpty()))
                        throw new AffiliationError(ERROR_DELETE);
                }

            }

        }catch (AffiliationError e){
            throw e;
        }catch (Exception e){
            throw new AffiliationError("Ocurrio un error");
        }

    }

    private void sendEmail(Affiliate affiliate){

        Object affiliation = findAffiliation(affiliate.getFiledNumber());
        String nameCompany = affiliate.getCompany();
        String email = null;
        String emailArl = Constant.EMAIL_ARL;

        List<ArlInformation> arlInformation = arlInformationDao.findAllArlInformation();
        if(!arlInformation.isEmpty())
            emailArl = arlInformation.get(0).getEmail();

        if(affiliation instanceof AffiliateMercantile affiliateMercantile)
            email = affiliateMercantile.getEmail();

        if(affiliation instanceof Affiliation affiliationDomestic)
            email = affiliationDomestic.getEmail();

        if(email == null)
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);

        Map<String, Object> data = new HashMap<>();

            data.put("nameCompany", nameCompany);
            data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, h:mm a")));
            data.put("mail", emailArl);
            data.put("sectionName", "Datos basicos empresa");

        sendEmails.sendEmailHeadquarters(data, email);

    }

    private Object findAffiliation(String filedNumber){

        Specification<AffiliateMercantile> specMercantile = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFiledNumber(filedNumber);

        Optional<AffiliateMercantile> optionalAffiliationMercantile = affiliateMercantileRepository.findOne(specMercantile);
        Optional<Affiliation> optionalAffiliationDomestic =  domesticServiceIndependentRepository.findOne(specAffiliation);

        if(optionalAffiliationMercantile.isEmpty() && optionalAffiliationDomestic.isEmpty())
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);

        return optionalAffiliationDomestic.isPresent() ? optionalAffiliationDomestic.get() : optionalAffiliationMercantile.get();

    }

    @Override
    public Object findAffiliateMercantile(Long idAffiliate){
        if(idAffiliate == null)
            return null;

        Affiliate affiliate = affiliateRepository.findById(idAffiliate)
                .orElse(null);
        if(affiliate != null)
            return affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE) ?
                    affiliateMercantileRepository.findOne(AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber()))
                            .orElse(null) :
                    domesticServiceIndependentRepository.findOne(AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(affiliate.getFiledNumber()))
                            .orElse(null);

        return null;

    }

    @Override
    public List<MainOfficeGrillaDTO> getAllMainOfficesByIdAffiliate(Long idAffiliate) {
        return repository.findAll(MainOfficeSpecification.findAllByIdAffiliate(idAffiliate))
                .stream()
                .map(main -> {
                    MainOfficeGrillaDTO mainOfficeDTO = new MainOfficeGrillaDTO();
                    BeanUtils.copyProperties(main, mainOfficeDTO);
                    return mainOfficeDTO;
                })
                .toList();
    }

    @Override
    public List<MainOfficeGrillaDTO> findByNumberAndTypeDocument(String number, String type){

        validOfficial(number);
        Affiliate affiliate = findAffiliate(number);

        return getAllMainOffices(affiliate.getUserId());
    }

    @Override
    public MainOffice saveMainOfficeOfficial(MainOfficeOfficialDTO mainOfficeOfficialDTO){

        validOfficial(mainOfficeOfficialDTO.getNumberDocument());
        Affiliate affiliate = findAffiliate(mainOfficeOfficialDTO.getNumberDocument());
        mainOfficeOfficialDTO.getMainOfficeDTO().setOfficeManager(affiliate.getUserId());
        return saveMainOffice(mainOfficeOfficialDTO.getMainOfficeDTO());
    }

    @Override
    public MainOffice updateOfficial(MainOfficeDTO mainOfficeDTO, Long id, String number) {
        validOfficial(number);
        return update(mainOfficeDTO, id);
    }

    @Override
    public String deleteOfficial(Long id, String number) {
        validOfficial(number);
        Affiliate affiliate = findAffiliate(number);
        return delete(id, affiliate.getIdAffiliate());
    }

    @Override
    public List<MainOffice> findAll() {
        return repository.findAll();
    }

    private Affiliate findAffiliate(String number){
        return affiliateRepository.findOne(AffiliateSpecification.findMercantileByLegalRepresentative(number))
                .orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));
    }

    private void validOfficial(String number){
        try {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserMain userMain = iUserPreRegisterRepository.findByEmail(jwt.getClaim("email")).orElseThrow(() -> new AffiliationError(Constant.USER_NOT_FOUND));

            if (number.equals(userMain.getIdentification()))
                throw new AffiliationError("El usuario logueado no puede ser el mismo usuario al cual se le esta gestionando las sedes");

        }catch (AffiliationError a){
            throw a;
        }catch (Exception e){
            log.error("Error method getEmailUserPreRegister : {}", e.getMessage());
            throw new AffiliationError(Constant.USER_NOT_FOUND_IN_DATA_BASE);
        }
    }
}
