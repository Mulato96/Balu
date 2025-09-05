package com.gal.afiliaciones.application.service.affiliationemployerprovisionserviceindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ProvisionServiceAffiliationStep3DTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AffiliationEmployerProvisionServiceIndependentService {

    ProvisionServiceAffiliationStep1DTO createAffiliationProvisionServiceStep1(ProvisionServiceAffiliationStep1DTO dto);
    ProvisionServiceAffiliationStep2DTO createAffiliationProvisionServiceStep2(ProvisionServiceAffiliationStep2DTO dto);
    ProvisionServiceAffiliationStep3DTO createAffiliationProvisionServiceStep3(ProvisionServiceAffiliationStep3DTO dto,
                                                                               List<MultipartFile> documents);
    ProvisionServiceAffiliationStep3DTO createProvisionServiceStep3FromPila(ProvisionServiceAffiliationStep3DTO dto);

}
