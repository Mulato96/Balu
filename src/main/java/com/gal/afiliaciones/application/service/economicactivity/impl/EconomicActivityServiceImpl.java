package com.gal.afiliaciones.application.service.economicactivity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUNotFound;
import com.gal.afiliaciones.config.ex.economicactivity.CodeCIIUShorterLength;
import com.gal.afiliaciones.config.ex.economicactivity.DescriptionNotFound;
import com.gal.afiliaciones.config.ex.economicactivity.IdEconomicSectorEmpty;
import com.gal.afiliaciones.config.mapper.EconomicActivityAdapter;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.EconomicActivitySpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.response.EconomicActivityResponseDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EconomicActivityServiceImpl implements IEconomicActivityService {

    private final IEconomicActivityRepository iEconomicActivityRepository;
    private final AffiliateRepository affiliateRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;

    private static final String INDEPENDENT_AFFILIATION = "Trabajador Independiente";
    private static final String DOMESTIC_AFFILIATION = "Empleador Servicio Doméstico";
    private static final String AFFILIATION_STATUS = "Activa";
    private static final String AFFILIATION_MERCANTILE_STAGE = "Afiliación completa";

    @Override
    public List<EconomicActivityDTO> getEconomicActivityByCodeCIIU(String codeCIIU, String description) {
        if (codeCIIU == null && description == null) {
            List<EconomicActivity> listAll = iEconomicActivityRepository.findAll();
            return getEconomicActivityDTOS(listAll);
        }

        if (codeCIIU != null){
            Specification<EconomicActivity> spec = UserSpecifications.findActivityEconomicByCodeCIIU(codeCIIU);
            List<EconomicActivity> activityList = iEconomicActivityRepository.findAll(spec);
            int length = codeCIIU.length();
            if (length < Constant.LENGTH_CODE_CIIU || length > Constant.LENGTH_ECONOMIC_ACTIVITY_CODE){
                throw new CodeCIIUShorterLength(Constant.CODE_CIIU_SHORTER_LENGTH);
            }
            if (activityList.isEmpty()){
                throw new DescriptionNotFound(Constant.CODE_CIIU_NOT_FOUND_MESSAGE);
            }
            return getEconomicActivityDTOS(activityList);
        }

        if(description.length() < Constant.LENGTH_CODE_CIIU){
            throw new DescriptionNotFound(Constant.CODE_DESCRIPTION_NULL);
        }

        Specification<EconomicActivity> spec = UserSpecifications.findActivityEconomicByDescription(description);
        List<EconomicActivity> activityList = iEconomicActivityRepository.findAll(spec);
        if (activityList.isEmpty()){
            throw new CodeCIIUNotFound(Constant.DESCRIPTION_NOT_FOUND_MESSAGE);
        }

        return getEconomicActivityDTOS(activityList);
    }

    @NotNull
    private List<EconomicActivityDTO> getEconomicActivityDTOS(List<EconomicActivity> activityList) {
        List<EconomicActivityDTO> economicActivityDTOList = new ArrayList<>();
        for (EconomicActivity activity : activityList) {
            EconomicActivityDTO dto = new EconomicActivityDTO();
            BeanUtils.copyProperties(activity, dto);
            dto.setEconomicActivityCode(dto.getClassRisk().concat(dto.getCodeCIIU()).concat(dto.getAdditionalCode()));
            economicActivityDTOList.add(dto);
        }
        return economicActivityDTOList;
    }

    @Override
    public EconomicActivity getEconomicActivityByRiskCodeCIIUCodeAdditional
            (String risk, String codeCIIU, String codeAdditional){
        Specification<EconomicActivity> spec = UserSpecifications.findActivityEconomicByRiskCodeCIIUCodeAdditional
                (risk, codeCIIU, codeAdditional);
        List<EconomicActivity> allActivities = iEconomicActivityRepository.findAll(spec);
        if(!allActivities.isEmpty()){
            return allActivities.get(0);
        }
        return null;
    }

    @Override
    public EconomicActivityDTO getEconomicActivityByCode(String code){
        if(code!=null && code.length() == 7){
            String risk = code.substring(0,1);
            String codeCIIU = code.substring(1,5);
            String additionalCode = code.substring(5);
            EconomicActivity activity = getEconomicActivityByRiskCodeCIIUCodeAdditional(risk, codeCIIU, additionalCode);
            EconomicActivityDTO dto = new EconomicActivityDTO();
            BeanUtils.copyProperties(activity, dto);
            return dto;
        }else{
            throw new CodeCIIUShorterLength(Constant.ACTIVITY_ECONOMIC_CODE_LENGTH);
        }

    }

    @Override
    public List<EconomicActivityDTO> getEconomicActivitiesByEconomicSectorId(Long economicSectorId) {
        if (Objects.isNull(economicSectorId)) {
            throw new IdEconomicSectorEmpty(Constant.ID_ECONOMIC_SECTOR_EMPTY);
        }
        List<EconomicActivity> economicActivities = iEconomicActivityRepository.findByIdEconomicSector(economicSectorId);
        return getEconomicActivityDTOS(economicActivities);
    }

    @Override
    public List<EconomicActivity> listEconomicActivity(List<Long> ids) {
        return (ids != null && !ids.isEmpty()) ? iEconomicActivityRepository.findAll(EconomicActivitySpecification.findByIds(ids)) : Collections.emptyList();
    }

    @Override
    public List<EconomicActivityResponseDTO> findUserEconomicActivity(String documentType, String documentNumber) {
        List<Affiliate> affiliations = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .stream()
                .filter(affiliate -> (affiliate.getAffiliationType().equals(INDEPENDENT_AFFILIATION) || affiliate.getAffiliationType().equals(DOMESTIC_AFFILIATION)) && affiliate.getAffiliationStatus().equals(AFFILIATION_STATUS))
                .toList();

        if (affiliations.isEmpty())
            return affiliateMercantileRepository.findByTypeDocumentIdentificationAndNumberIdentification(documentType, documentNumber)
                    .stream()
                    .filter(affiliateMercantile -> affiliateMercantile.getFiledNumber() != null && affiliateMercantile.getStageManagement().equals(AFFILIATION_MERCANTILE_STAGE))
                    .map(affiliateMercantile -> affiliateMercantile.getEconomicActivity()
                                .stream()
                                .filter(AffiliateActivityEconomic::getIsPrimary)
                                .map(AffiliateActivityEconomic::getActivityEconomic)
                                .findFirst()
                                .orElse(null))
                    .map(EconomicActivityAdapter.entityToDto)
                    .toList();

        return affiliationDetailRepository.findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber(documentType, documentNumber)
                .stream()
                .flatMap(affiliation ->
                        affiliation.getEconomicActivity()
                                .stream()
                                .map(AffiliateActivityEconomic::getActivityEconomic)
                                .map(EconomicActivityAdapter.entityToDto)
                ).toList();
    }


    @Override
    public List<EconomicActivityDTO> getEconomyActivityExcludeCurrent(String documentType, String documentNumber) {

        Specification<EconomicActivity> spec = UserSpecifications.findActivityEconomicByDescription(null);
        List<EconomicActivity> allActivityList = iEconomicActivityRepository.findAll(spec);

        Set<String> userActivityCodes = affiliationDetailRepository
                .findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber(documentType, documentNumber)
                .stream()
                .flatMap(affiliation ->
                        affiliation.getEconomicActivity()
                                .stream()
                                .map(AffiliateActivityEconomic::getActivityEconomic)
                                .map(EconomicActivity::getEconomicActivityCode)
                ).collect(Collectors.toSet());


        return allActivityList.stream()
                .filter(activity -> activity.getCodeCIIU() != null)
                .filter(activity -> !userActivityCodes.contains(activity.getCodeCIIU()))
                .filter(activity -> "4".equals(activity.getClassRisk()) || "5".equals(activity.getClassRisk()))
                .map(EconomicActivityAdapter.entityToEconmyActivityDto)
                .toList();
    }


}
