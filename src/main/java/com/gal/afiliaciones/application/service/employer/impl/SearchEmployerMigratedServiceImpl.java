package com.gal.afiliaciones.application.service.employer.impl;

import com.gal.afiliaciones.application.service.employer.SearchEmployerMigratedService;
import com.gal.afiliaciones.config.ex.Error.Type;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchEmployerMigratedServiceImpl implements SearchEmployerMigratedService {

    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private final AffiliateMercantileRepository mercantileRepository;

    @Override
    public List<DataBasicEmployerMigratedDTO> searchEmployerDataBasic(String documentType, String documentNumber){
        return affiliateRepository.findEmployerDataByDocument(documentType, documentNumber);
    }

}
