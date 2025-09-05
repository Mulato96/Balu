package com.gal.afiliaciones.application.service.affiliationindependentvolunteer;

import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliate.MandatoryDanger;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep3DTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AffiliationIndependentVolunteerService {

    Affiliation createAffiliationStep1(AffiliationIndependentVolunteerStep1DTO dto);

    Danger createAffiliationStep2(AffiliationIndependentVolunteerStep2DTO dto);

    Affiliation createAffiliationStep3(AffiliationIndependentVolunteerStep3DTO dto, List<MultipartFile> documents);

    Boolean isTransferableBySAT(String identificationType, String identificationNumber);

    Affiliation createAffiliationStep3FromPila(AffiliationIndependentVolunteerStep3DTO dto);

    MandatoryDanger getMandatoryDangerByFkOccupationId(Long fkOccupationId);

}
