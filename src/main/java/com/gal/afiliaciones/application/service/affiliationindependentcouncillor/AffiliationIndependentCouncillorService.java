package com.gal.afiliaciones.application.service.affiliationindependentcouncillor;

import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor.AffiliationCouncillorStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.mayoraltydependence.MayoraltyDependenceDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AffiliationIndependentCouncillorService {

    AffiliationCouncillorStep1DTO createAffiliationStep1(AffiliationCouncillorStep1DTO dto);
    AffiliationCouncillorStep2DTO createAffiliationStep2(AffiliationCouncillorStep2DTO dto);
    AffiliationCouncillorStep3DTO createAffiliationStep3(AffiliationCouncillorStep3DTO dto, List<MultipartFile> documents);
    List<MayoraltyDependenceDTO> findAllMayoraltyDependence(String nit);
    AffiliationCouncillorStep3DTO createAffiliationStep3FromPila(AffiliationCouncillorStep3DTO dto);

}
