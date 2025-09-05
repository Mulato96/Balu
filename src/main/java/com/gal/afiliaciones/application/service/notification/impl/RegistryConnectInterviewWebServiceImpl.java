package com.gal.afiliaciones.application.service.notification.impl;

import com.gal.afiliaciones.application.service.notification.RegistryConnectInterviewWebService;
import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.notification.RegistryConnectInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.RegistryConnectInterviewWebSpecification;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = AffiliationsExceptionBase.class)
public class RegistryConnectInterviewWebServiceImpl implements RegistryConnectInterviewWebService {

    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final RegistryConnectInterviewWebRepository registryConnectInterviewWebRepository;

    @Override
    public void save(RegistryConnectInterviewWeb registryConnectInterviewWeb) {

        AffiliateMercantile affiliateMercantile = findByNumberFiledMercantile(registryConnectInterviewWeb.getNumberFiled());

        if(findUser(affiliateMercantile.getIdUserPreRegister())){
            registryConnectInterviewWebRepository.save(registryConnectInterviewWeb);
        }else{
            throw new AffiliationError(Constant.ERROR_CONSULT_USER);
        }

    }

    @Override
    public RegistryConnectInterviewWeb findById(Long id) {
        return registryConnectInterviewWebRepository.findById(id).orElseThrow(() -> new AffiliationError("No se encontro el registro"));
    }

    @Override
    public List<RegistryConnectInterviewWeb> findByFiledNumber(String filedNumber) {

        Specification<RegistryConnectInterviewWeb> spec = RegistryConnectInterviewWebSpecification.findByFiledNumber(filedNumber);
        return registryConnectInterviewWebRepository.findAll(spec);
    }

    @Override
    public List<RegistryConnectInterviewWeb> findAll() {
        return registryConnectInterviewWebRepository.findAll();
    }

    @Override
    public void deleteByFiledNumber(String filedNumber){

        findByFiledNumber(filedNumber).forEach(registryConnectInterviewWebRepository::delete);
    }

    private boolean findUser(Long id){

         return iUserPreRegisterRepository.findById(id).isPresent();
    }

    private AffiliateMercantile findByNumberFiledMercantile(String filedNumber){

        Specification<AffiliateMercantile> spec = AffiliateMercantileSpecification.findByFieldNumber(filedNumber);
        return affiliateMercantileRepository.findOne(spec).orElseThrow(() -> new AffiliationError(Constant.AFFILIATE_NOT_FOUND));

    }

}
