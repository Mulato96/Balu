package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.MainOfficeSpecification;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String ERROR_DELETE = "no puedes eliminar este centro de trabajo ya que tienes trabajadores asociados";

    @Override
    public List<MainOfficeGrillaDTO> getAllMainOffices(Long idUser) {
        return repository.findAll(MainOfficeSpecification.findAllByIdUser(idUser))
                .stream()
                .map(main -> {

                    MainOfficeGrillaDTO mainOfficeDTO = new MainOfficeGrillaDTO();
                    BeanUtils.copyProperties(main, mainOfficeDTO);
                    return mainOfficeDTO;

                    })
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

        if(!findByIdUserAndDepartmentAndCityAndAddress(mainOfficeDTO.getOfficeManager(), mainOfficeDTO.getAddressDTO()).isEmpty())
            throw new AffiliationError("La sede ya se encuentra creada, Valida la información y vuelve a intentar");

        if(!repository.findAll(MainOfficeSpecification.findByIdUserAndName(mainOfficeDTO.getOfficeManager(), mainOfficeDTO.getMainOfficeName())).isEmpty())
            throw new AffiliationError("La sede ya se encuentra creada, Valida la información y vuelve a intentar");

        mainOfficeDTO.setEconomicActivity(mainOfficeDTO.getEconomicActivity().stream().filter(Objects::nonNull).toList());

        mainOfficeDTO.setMainOfficeZone(mainOfficeDTO.getMainOfficeZone().toUpperCase());

        validMainOffice(mainOfficeDTO);

        MainOffice mainOffice = new MainOffice();

        UserMain userMain = findUserMain(mainOfficeDTO.getOfficeManager());
        Affiliate affiliate =  affiliateRepository.findAll(AffiliateSpecification.findByIdentificationTypeAndNumber(userMain.getIdentificationType(), userMain.getIdentification()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));


        BeanUtils.copyProperties(mainOfficeDTO, mainOffice);
        BeanUtils.copyProperties(mainOfficeDTO.getAddressDTO(), mainOffice);

        mainOffice.setOfficeManager(userMain);
        mainOffice.setIdAffiliate(affiliate.getIdAffiliate());

        List<EconomicActivity> economicActivityList =  findAllActivityEconomicById(mainOfficeDTO.getEconomicActivity());

        economicActivityList.forEach(economic -> workCenter(economic, userMain, mainOffice.getIdDepartment(), mainOffice.getIdCity(), mainOffice.getMainOfficeZone()));

        mainOffice.setCode(findCode());

        if(Boolean.TRUE.equals(mainOfficeDTO.getMain()))
            changeMain(mainOffice.getOfficeManager().getId());

        if(repository.findAll(MainOfficeSpecification.findAllByIdUser(mainOfficeDTO.getOfficeManager())).isEmpty())
            mainOffice.setMain(true);

        sendEmail(findByNumberAndTypeDocument(userMain));

        return repository.save(mainOffice);
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
            List<Long> activityEconomic = affiliateMercantile.getEconomicActivity()
                    .stream()
                    .map(economic -> economic.getActivityEconomic().getId())
                    .toList();

            BeanUtils.copyProperties(mainOffice, mainOfficeDTO);
            BeanUtils.copyProperties(mainOffice,addressDTO);

            mainOfficeDTO.setAddressDTO(addressDTO);
            mainOfficeDTO.setOfficeManager(mainOffice.getOfficeManager().getId());
            mainOfficeDTO.setEconomicActivity(activityEconomic);


            return mainOfficeDTO;
        }

        if(affiliate instanceof Affiliation affiliation) {

            List<Long> activityEconomic = affiliation.getEconomicActivity()
                    .stream()
                    .map(AffiliateActivityEconomic::getActivityEconomic)
                    .map(EconomicActivity::getId)
                    .toList();

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
    public MainOffice update(MainOfficeDTO mainOfficeDTO, Long id, String filedNumber) {

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

        Affiliate affiliate = findByFiledNumber(filedNumber);

        UserMain userMain = findUserMain(mainOfficeDTO.getOfficeManager());

        String code = mainOffice.getCode();

        BeanUtils.copyProperties(mainOfficeDTO, mainOffice);
        BeanUtils.copyProperties(mainOfficeDTO.getAddressDTO(), mainOffice);

        List<EconomicActivity> economicActivityList =  findAllActivityEconomicById(mainOfficeDTO.getEconomicActivity());
        AffiliateMercantile affiliateMercantile = affiliateMercantileRepository.findOne(AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber()))
                .orElse(null);

        if(changedEconomicActivitiesList(economicActivityList, affiliateMercantile)) {
            if (affiliateMercantile != null && affiliateMercantile.getEconomicActivity() != null) {
                affiliateMercantile.getEconomicActivity()
                        .forEach(economic -> {
                            if (!economicActivityList.isEmpty() && economicActivityList.get(0) != null)
                                validWorkedAssociated(mainOffice.getId(), economic.getActivityEconomic().getId(), affiliate);
                        });
            }

            economicActivityList.forEach(economic -> workCenter(economic, userMain, mainOffice.getIdDepartment(), mainOffice.getIdCity(), mainOffice.getMainOfficeZone()));
        }

        if(Boolean.TRUE.equals(mainOffice.getMain()))
            changeMain(mainOffice.getOfficeManager().getId());

        mainOffice.setCode(code);

        if(repository.findAll(MainOfficeSpecification.findAllByIdUser(mainOfficeDTO.getOfficeManager())).size() == 1)
            mainOffice.setMain(true);

        sendEmail(affiliate);

        return repository.save(mainOffice);

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

    @Override
    public String delete(Long id, String filedNumber) {

        MainOffice mainOffice = findById(id);
        Affiliate affiliate = findByFiledNumber(filedNumber);
        AffiliateMercantile affiliateMercantile =  affiliateMercantileRepository.findOne(AffiliateMercantileSpecification.findByFieldNumber(affiliate.getFiledNumber()))
                .orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

        if(Boolean.TRUE.equals(mainOffice.getMain()))
            throw new AffiliationError("No se puede eliminar la sede, porque es la sede principal");

        affiliateMercantile.getEconomicActivity()
                        .forEach(main -> validWorkedAssociated(mainOffice.getId(), main.getActivityEconomic().getId(), affiliate));

        if(repository.findAll(MainOfficeSpecification.findAllByIdUser(mainOffice.getOfficeManager().getId())).size() <= 1)
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

    private Affiliate findByFiledNumber(String filedNumber){

        return affiliateRepository.findOne(AffiliateSpecification.findByField(filedNumber)).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));
    }

    private List<EconomicActivity> findAllActivityEconomicById(List<Long> ids){
        return ids.stream()
                .map(id -> economicActivityRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro la actividad economica con id: " + id)))
                .toList();
    }


    private void validMainOffice(MainOfficeDTO mainOfficeDTO){

        if(validEmpty(mainOfficeDTO.getMainOfficeName()) && mainOfficeDTO.getMainOfficeName().length() >= 101)
            throw new AffiliationError("El nombre de la sede excede el tamaño permitido, el tamaño debe ser menor a 100 caracteres");

        if(validEmpty(String.valueOf(mainOfficeDTO.getAddressDTO().getIdCity())))
            throw new AffiliationError("El campo ciudad no puede ser vacio");

        if(validEmpty(String.valueOf(mainOfficeDTO.getAddressDTO().getIdDepartment())))
            throw new AffiliationError("El campo departamento no puede ser vacio");

        if(validEmpty(mainOfficeDTO.getMainOfficeEmail()))
            throw new AffiliationError("El campo email no puede ser vacio");

        if(validNumberPhone(mainOfficeDTO.getMainOfficePhoneNumber(), true))
            throw new AffiliationError(Constant.PHONE);

        if(validNumberPhone(mainOfficeDTO.getMainPhoneNumberTwo(), false))
            throw new AffiliationError(Constant.PHONE);

        if(validNumberPhone(mainOfficeDTO.getPhoneOneResponsibleHeadquarters(), true))
            throw new AffiliationError(Constant.PHONE);

        if(validNumberPhone(mainOfficeDTO.getPhoneTwoResponsibleHeadquarters(), false))
            throw new AffiliationError(Constant.PHONE);

        if(validEmail(mainOfficeDTO.getMainOfficeEmail()) && validEmail(mainOfficeDTO.getEmailResponsibleHeadquarters()))
            throw new AffiliationError("El correo tiene un formato incorrecto");

        if(!List.of(Constant.URBAN_ZONE, Constant.RURAL_ZONE).contains(mainOfficeDTO.getMainOfficeZone()))
            throw new AffiliationError("La zona no corresponde a ninguna de las indicadas");

        if(mainOfficeDTO.getEconomicActivity() == null || mainOfficeDTO.getEconomicActivity().isEmpty() || mainOfficeDTO.getEconomicActivity().size() >= 6)
            throw new AffiliationError("Error, se debe contar con minimo 1 y maximo 5 actividades economicas");

        if(!validTypeNumberIdentification(mainOfficeDTO.getTypeDocumentResponsibleHeadquarters()) &&
                !validNumberIdentification(mainOfficeDTO.getNumberDocumentResponsibleHeadquarters(), mainOfficeDTO.getTypeDocumentResponsibleHeadquarters()))
            throw new AffiliationError("Tipo o numero de documento incorrectos");

        if(validNames(mainOfficeDTO.getFirstNameResponsibleHeadquarters(), true, 50))
            throw new AffiliationError("Nombre incorrecto");

        if(validNames(mainOfficeDTO.getSecondNameResponsibleHeadquarters(), false, 100))
            throw new AffiliationError("Segundo nombre incorrecto");

        if(validNames(mainOfficeDTO.getSurnameResponsibleHeadquarters(), true, 50))
            throw new AffiliationError("Apellido incorrecto");

        if(validNames(mainOfficeDTO.getSecondSurnameResponsibleHeadquarters(), false, 100))
            throw new AffiliationError("Segundo apellido incorrecto");

    }

    private boolean validEmpty(String data){
        return !(data != null && !data.isEmpty());
    }

    private boolean validNumberPhone(String number, boolean requested){

        if(requested){
            if(!validEmpty(number))
                return !List.of(
                        "601", "602", "604", "605", "606", "607", "608",
                        "300", "301", "302", "303", "304", "305", "310", "311", "312",
                        "313", "314", "315", "316", "317", "318", "319", "320", "321",
                        "322", "323", "324", "333", "350", "351")
                        .contains(number.substring(0, 3));
            return true;
        }

        if(!validEmpty(number))
            return !List.of(
                    "601", "602", "604", "605", "606", "607", "608",
                    "300", "301", "302", "303", "304", "305", "310", "311", "312",
                    "313", "314", "315", "316", "317", "318", "319", "320", "321",
                    "322", "323", "324", "333", "350", "351")
                    .contains(number.substring(0, 3));
        return false;

    }

    private boolean validEmail(String email){

        if(!validEmpty(email))
            return !(email.length() <= 100 && email.matches("^[^\\s@]+@(?=[a-zA-Z\\-]+\\.[a-zA-Z]{2,})(?!-)[a-zA-Z\\-]+(?<!-)\\.[a-zA-Z]{2,}$"));
        return true;
    }

    private void changeMain(Long idUser){

        repository.findAll(MainOfficeSpecification.findByMainTrue(idUser)).forEach(main -> {
            main.setMain(false);
            repository.save(main);
        });

    }

    private void workCenter(EconomicActivity economicActivity , UserMain user, Long department, Long city, String zone){

        String codeActivityEconomic = codeActivityEconomic(economicActivity);

        if(workCenterService.getWorkCenterByCodeAndIdUser(codeActivityEconomic, user) == null){

            Long code = workCenterService.getNumberCode(user) + 1;

            WorkCenter workCenter = new WorkCenter();

            workCenter.setCode(String.valueOf(code));
            workCenter.setEconomicActivityCode(codeActivityEconomic);
            workCenter.setTotalWorkers(0);
            workCenter.setRiskClass(economicActivity.getClassRisk());
            workCenter.setWorkCenterDepartment(department);
            workCenter.setWorkCenterCity(city);
            workCenter.setWorkCenterZone(zone);
            workCenter.setWorkCenterManager(user);
            workCenterService.saveWorkCenter(workCenter);

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

    private boolean validNames(String name, boolean requested, long length){

        if(requested){

            if(!validEmpty(name))
                return !(name.length() <= length && name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$"));
            else
                return true;
        }

        if(!validEmpty(name))
            return !(name.length() <= length && name.matches("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$"));
        return false;

    }

    private List<MainOffice> findByIdUserAndDepartmentAndCityAndAddress(Long idUser, AddressDTO addressDTO){

        return repository.findAll(MainOfficeSpecification.findByIdUserAndDepartmentAndCityAndAddress(idUser, addressDTO));
    }

    private void validWorkedAssociated(Long idHeadquarter, Long idEconomicActivity, Affiliate affiliate){

       try {

           if(idEconomicActivity != null){

               List<String> filedNumbers = affiliateRepository.findAll(AffiliateSpecification.findDependentsByEmployer(affiliate.getNitCompany()))
                       .stream()
                       .map(Affiliate::getFiledNumber)
                       .toList();

               if(!filedNumbers.isEmpty() &&
                       (!affiliationDependentRepository.findAll(AffiliationDependentSpecification.findByEconomicActivityAndEmployer(filedNumbers))
                               .stream()
                               .filter(dependent -> Objects.equals(dependent.getIdHeadquarter(), idHeadquarter))
                               .toList()
                               .isEmpty()
                       )
               )
                   throw new AffiliationError(ERROR_DELETE);

               Object affiliation = findAffiliation(affiliate.getFiledNumber());

               if(affiliation instanceof AffiliateMercantile affiliateMercantile && Objects.equals(affiliateMercantile.getIdMainHeadquarter(), idHeadquarter)
               )
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

    private void sendEmail(Affiliate affiliate){

        Object affiliation = findAffiliation(affiliate.getFiledNumber());
        String nameCompany = affiliate.getCompany();
        String email = null;

        if(affiliation instanceof AffiliateMercantile affiliateMercantile)
            email = affiliateMercantile.getEmail();

        if(affiliation instanceof Affiliation affiliationDomestic)
            email = affiliationDomestic.getEmail();

        if(email == null)
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);

        Map<String, Object> data = new HashMap<>();

            data.put("nameCompany", nameCompany);
            data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, h:mm a")));
            data.put("mail", Constant.EMAIL_ARL);
            data.put("sectionName", "Datos basicos empresa");

        sendEmails.sendEmailHeadquarters(data, email);

    }

    private Object findAffiliation(String filedNumber){

        Specification<AffiliateMercantile> specMercantile = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFiledNumber(filedNumber);

        Optional<AffiliateMercantile> optionalAffiliation = affiliateMercantileRepository.findOne(specMercantile);
        Optional<Affiliation> optionalAffiliate =  domesticServiceIndependentRepository.findOne(specAffiliation);

        if(optionalAffiliation.isEmpty() && optionalAffiliate.isEmpty())
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);

        return optionalAffiliate.isPresent() ? optionalAffiliate.get() : optionalAffiliation.get();

    }

    private Affiliate findByNumberAndTypeDocument(UserMain userMain){
        Specification<Affiliate> spc = AffiliateSpecification.findByEmployerAndIdentification(userMain.getIdentificationType(), userMain.getIdentification());
        List<Affiliate> affiliateList = affiliateRepository.findAll(spc);
        return affiliateList.get(0);
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



}
