package com.gal.afiliaciones.application.service.retirementreason;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.CompanyInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RetirementEmployerDTO;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;

public interface RetirementReasonService {

    List<RetirementReason> findAll();

    CompanyInfoDTO getCompanyInfo(String identificacitonType, String identification);

    String retirementEmployer(RetirementEmployerDTO retirementEmployerDTO) throws MessagingException, IOException;

    List<RetirementReasonWorker> findAllRetirementReasonWorker();

}
