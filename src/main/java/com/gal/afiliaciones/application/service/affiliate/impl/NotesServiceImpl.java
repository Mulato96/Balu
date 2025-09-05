package com.gal.afiliaciones.application.service.affiliate.impl;

import com.gal.afiliaciones.application.service.affiliate.NotesService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.Notes;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.NotesRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationEmployerDomesticServiceIndependentSpecifications;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.NotesSpecification;
import com.gal.afiliaciones.infrastructure.dto.NotesDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class NotesServiceImpl implements NotesService {

    private final NotesRepository notesRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    @Override
    public NotesDTO create(Notes note) {

        NotesDTO notesDTO =  new NotesDTO();
        Optional<AffiliateMercantile> optionalAffiliateMercantile = findAffiliation(note.getFiledNumberAffiliation());
        Optional<Affiliation> optionalAffiliation = findAffiliateByFieldNumber(note.getFiledNumberAffiliation());
        UserMain userMain = findOfficial(note.getIdOfficial());

        if(optionalAffiliation.isEmpty() && optionalAffiliateMercantile.isEmpty()){
            throw new AffiliationError(Constant.AFFILIATE_NOT_FOUND);
        }



        if(note.getNote().length() >= 501){
            throw new AffiliationError("La nota excedio el tamaño permitido, el tamaño permitido es de 500 caracteres");
        }

        if(note.getNote().isEmpty()){
            throw new AffiliationError("La nota no puede ser vacia");
        }

        if(optionalAffiliateMercantile.isPresent()){

            AffiliateMercantile affiliateMercantile = optionalAffiliateMercantile.get();

            if (!(affiliateMercantile.getStageManagement().equals(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW) || affiliateMercantile.getStageManagement().equals(Constant.INTERVIEW_WEB))) {
                throw new AffiliationError(Constant.ERROR_AFFILIATION);
            }

            note.setDateInterviewWed(LocalDateTime.now());
            note.setStageManagement(affiliateMercantile.getStageManagement());

            BeanUtils.copyProperties(notesRepository.save(note),notesDTO);
            notesDTO.setTitle(generateName(numberNotes(notesDTO.getFiledNumberAffiliation())));
            notesDTO.setNameOfficial(userMain.getFirstName().concat(" ").concat(userMain.getSurname()));

            return notesDTO;
        }

        Affiliation affiliation = optionalAffiliation.get();

        if (!(affiliation.getStageManagement().equals(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW) || affiliation.getStageManagement().equals(Constant.INTERVIEW_WEB))) {
            throw new AffiliationError(Constant.ERROR_AFFILIATION);
        }

        note.setDateInterviewWed(LocalDateTime.now());
        note.setStageManagement(affiliation.getStageManagement());

        BeanUtils.copyProperties(notesRepository.save(note),notesDTO);
        notesDTO.setTitle(generateName(numberNotes(notesDTO.getFiledNumberAffiliation())));
        notesDTO.setNameOfficial(userMain.getFirstName().concat(" ").concat(userMain.getSurname()));

        return notesDTO;

    }

    @Override
    public List<NotesDTO> findByAffiliation(String filedNumber) {

        List<NotesDTO> listNotesDTO =  new ArrayList<>();
        Specification<Notes> spec = NotesSpecification.findByAffiliation(filedNumber);
        List<Notes> listNotes = notesRepository.findAll(spec);
        Long numNote = 1L;

        for(Notes note : listNotes){

            NotesDTO notesDTO = new NotesDTO();
            UserMain userMain = findOfficial(note.getIdOfficial());

            BeanUtils.copyProperties(note, notesDTO);
            notesDTO.setTitle(generateName(numNote));
            notesDTO.setNameOfficial(userMain.getFirstName().concat(" ").concat(userMain.getSurname()));
            listNotesDTO.add(notesDTO);
            numNote++;

        }

        return listNotesDTO;
    }

    private Long numberNotes(String filedNumber){

        Specification<Notes> spec = NotesSpecification.findByAffiliation(filedNumber);
        return (long) notesRepository.findAll(spec).size();
    }

    private String generateName(Long number){
        return "Nota ".concat(String.valueOf(number));
    }

    private UserMain findOfficial(Long id){
        return iUserPreRegisterRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el funcionario"));
    }

    private Optional<AffiliateMercantile> findAffiliation(String filedNumber){

        Specification<AffiliateMercantile> spc = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        return affiliateMercantileRepository.findOne(spc);
    }

    private Optional<Affiliation> findAffiliateByFieldNumber(String fieldNumber){

        Specification<Affiliation> specAffiliation = AffiliationEmployerDomesticServiceIndependentSpecifications.hasFieldNumber(fieldNumber);
        return repositoryAffiliation.findOne(specAffiliation);
    }
}
