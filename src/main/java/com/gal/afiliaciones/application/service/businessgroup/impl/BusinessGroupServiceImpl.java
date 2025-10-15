package com.gal.afiliaciones.application.service.businessgroup.impl;

import com.gal.afiliaciones.application.service.businessgroup.BusinessGroupService;
import com.gal.afiliaciones.domain.model.BusinessGroup;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.client.generic.businessgroup.BusinessGroupResponse;
import com.gal.afiliaciones.infrastructure.client.generic.businessgroup.ConsultBusinessGroupClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.businessgroup.BusinessGroupRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessGroupServiceImpl implements BusinessGroupService {

    private final ConsultBusinessGroupClient consultBusinessGroupClient;
    private final AffiliateRepository affiliateRepository;
    private final BusinessGroupRepository businessGroupRepository;

    @Override
    public boolean insertBusinessGroupFromClient(){
        try {
            List<BusinessGroupResponse> businessGroupList = consultBusinessGroupClient.getBusinessGroups().block();
            businessGroupList.forEach(businessGroup -> {

                Specification<Affiliate> spc = AffiliateSpecification.findByEmployerActive(
                        businessGroup.getEmployerDocType(), businessGroup.getEmployerDocNumber());
                Affiliate affiliate = affiliateRepository.findOne(spc).orElse(null);

                if (!existCompanyByBusinessGroup(businessGroup, affiliate)) {
                    BusinessGroup newBusinessGroup = new BusinessGroup();
                    newBusinessGroup.setIdBusinessGroup(businessGroup.getId());
                    newBusinessGroup.setNameBusinessGroup(businessGroup.getName());
                    newBusinessGroup.setIdAffiliate(affiliate != null ? affiliate.getIdAffiliate() : null);
                    newBusinessGroup.setIsMainCompany(false);
                    businessGroupRepository.save(newBusinessGroup);
                }

            });

            return true;

        } catch (Exception ex) {
            log.error("Error insert business group: " + ex.getMessage());
        }

        return false;
    }

    private boolean existCompanyByBusinessGroup(BusinessGroupResponse businessGroup, Affiliate affiliate){
        if(affiliate!=null){
            BusinessGroup existBusinessGroup = businessGroupRepository.
                    findByIdBusinessGroupAndIdAffiliate(businessGroup.getId(), affiliate.getIdAffiliate())
                    .orElse(null);
            if(existBusinessGroup==null)
                return false;
        }else{
            log.error("Company " + businessGroup.getCompanyName() + " is not affiliate");
        }
        return true;
    }

}
