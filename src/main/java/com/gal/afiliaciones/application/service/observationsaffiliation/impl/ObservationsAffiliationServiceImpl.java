package com.gal.afiliaciones.application.service.observationsaffiliation.impl;

import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation.ObservationsAffiliationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.ObservationsAffiliationSpecifications;
import com.gal.afiliaciones.infrastructure.dto.observationsaffiliation.ObservationAffiliationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObservationsAffiliationServiceImpl implements ObservationsAffiliationService {

    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final ObservationsAffiliationRepository observationsAffiliationRepository;

    @Override
    public ObservationAffiliationDTO create(String observation, String filedNumber, String reasonReject, Long idOfficial) {

        if(observation.length() >= 300){
            throw new AffiliationError("La observacion excede el tamaño maximo permitido");
        }

        ObservationsAffiliation observationsAffiliation =  new ObservationsAffiliation();
        ObservationAffiliationDTO observationAffiliationDTO = new ObservationAffiliationDTO();
        observationsAffiliation.setObservations(observation);
        observationsAffiliation.setFiledNumber(filedNumber);
        observationsAffiliation.setDate(LocalDateTime.now());
        observationsAffiliation.setReasonReject(reasonReject);
        observationsAffiliation.setIdOfficial(idOfficial);
        observationsAffiliation = observationsAffiliationRepository.save(observationsAffiliation);

        BeanUtils.copyProperties(observationsAffiliation, observationAffiliationDTO);
        observationAffiliationDTO.setNameOfficial(nameOfficial(observationsAffiliation.getIdOfficial()));
        observationAffiliationDTO.setDate(date(observationsAffiliation.getDate()));
        observationAffiliationDTO.setState("Rechazo");

        return observationAffiliationDTO;
    }

    @Override
    public List<ObservationAffiliationDTO> findByFiledNumber(String filedNumber) {

        List<ObservationAffiliationDTO> listObservationAffiliationDTO = new ArrayList<>();
        Specification<ObservationsAffiliation> spec = ObservationsAffiliationSpecifications.findByNumberFiled(filedNumber);
        List<ObservationsAffiliation> listObservationAffiliation = observationsAffiliationRepository.findAll(spec);

        listObservationAffiliation.forEach(observation -> {
            ObservationAffiliationDTO dto = new ObservationAffiliationDTO();
            BeanUtils.copyProperties(observation, dto);
            dto.setNameOfficial(nameOfficial(observation.getIdOfficial()));
            dto.setDate(date(observation.getDate()));
            dto.setState("Rechazo");

            listObservationAffiliationDTO.add(dto);
        });

        return listObservationAffiliationDTO;
    }

    private String nameOfficial(Long id){
        UserMain userMain = iUserPreRegisterRepository.findById(id).orElseThrow( () -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND));
        return userMain.getFirstName().concat(" ").concat(userMain.getSurname());
    }

    private String date(LocalDateTime fullDate){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fullDate.format(formatter);

    }
}
