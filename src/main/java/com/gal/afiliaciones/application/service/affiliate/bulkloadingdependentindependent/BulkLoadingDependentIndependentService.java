package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent;

import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import org.springframework.web.multipart.MultipartFile;

public interface BulkLoadingDependentIndependentService {

    ResponseServiceDTO dataFile(MultipartFile file, String type, Long idUser);
    ResponseServiceDTO dataFileWithNumber(MultipartFile file, String type, String documentNumber, String typeDocument, Long idOfficial);
    String getTemplateByBondingType(String bondingType);
    String downloadTemplateGuide();
    String consultAffiliation(String type, String number);

}
