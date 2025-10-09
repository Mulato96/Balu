package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.employer.SearchEmployerMigratedService;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchEmployerMigratedServiceImpl implements SearchEmployerMigratedService {

    private final AffiliateRepository affiliateRepository;
    private final IUserPreRegisterRepository userRepository;

    private static final String ACRONYM_DELEGATE = "DEL";

    @Override
    public List<DataBasicEmployerMigratedDTO> searchEmployerDataBasic(String documentType, String documentNumber, String userType){
        if(userType.equalsIgnoreCase(ACRONYM_DELEGATE)) {
            Specification<UserMain> spc = UserSpecifications.byUsername(documentType+"-"+documentNumber+"-"+ACRONYM_DELEGATE);
            UserMain userDelegate = userRepository.findOne(spc)
                    .orElseThrow(() -> new UserNotFoundInDataBase("User delegate not found"));
            return affiliateRepository.findEmployerDataByDelegate(userDelegate.getId());
        } else {
            return affiliateRepository.findEmployerDataByDocument(documentType, documentNumber);
        }
    }

}
