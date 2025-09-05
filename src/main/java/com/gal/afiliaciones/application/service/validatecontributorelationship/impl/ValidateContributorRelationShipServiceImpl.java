package com.gal.afiliaciones.application.service.validatecontributorelationship.impl;

import com.gal.afiliaciones.application.service.validatecontributorelationship.ValidateContributorRelationShipService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidRelationShipResponse;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ValidateContributorRelationShipServiceImpl implements ValidateContributorRelationShipService {
    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;


    @Override
    public ValidRelationShipResponse validateRelationShip(ValidateContributorRequest request) {

        ValidRelationShipResponse response = new ValidRelationShipResponse();
        List<Affiliate> affiliates;
        if (!request.getEmployerIdentificationType().equals(Constant.NI)) {
            affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber
                    (request.getEmployerIdentificationType(), request.getEmployerIdentificationNumber());
        } else {
            Specification<Affiliate> spc = AffiliateSpecification.findByNit(request.getEmployerIdentificationNumber());
            affiliates = affiliateRepository.findAll(spc);
        }

        if (affiliates.isEmpty()) {
            throw new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND);
        }

        Affiliate affiliate = affiliates.get(0);

        //Consultar los cotizantes por NIT_Company
        List<Affiliate> affiliatesByNitCompany = affiliateRepository.findByNitCompany(affiliate.getNitCompany());

        if (affiliatesByNitCompany.isEmpty()) {
            throw new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND);
        }

        List<Affiliate> employees = filterNonEmployerAffiliates(affiliatesByNitCompany);

        Optional<Affiliate> employeeOptional = findEmployee(employees, request);

        if (employeeOptional.isPresent()) {
            setResponseForEmployee(response, employeeOptional.get());
        } else {
            response.setMessageResponse("El cotizante no tiene un vínculo laboral vigente con el aportante. ¿Desea continuar de todas formas?");
        }

        return response;
    }

    @Override
    public Optional<Affiliate> findEmployee(List<Affiliate> employees, ValidateContributorRequest request) {
        return employees.stream()
                .filter(employee ->
                        employee.getDocumentNumber().equals(request.getEmployeeIdentificationNumber())
                                && employee.getDocumentType().equals(request.getEmployeeIdentificationType())
                )
                .findFirst();
    }

    @Override
    public List<Affiliate> filterNonEmployerAffiliates(List<Affiliate> affiliates) {
        return affiliates.stream()
                .filter(aff ->
                        !aff.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)
                                && !aff.getAffiliationSubType().equals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC)
                )
                .toList();
    }

    private void setResponseForEmployee(ValidRelationShipResponse response, Affiliate employee) {
        if (employee.getAffiliationType().equals(Constant.TYPE_AFFILLATE_INDEPENDENT)) {
            Affiliation affiliationIndependent = affiliationRepository.findByFiledNumber(employee.getFiledNumber())
                    .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

            response.setMessageResponse("Ok");
            response.setFirstNameContributor(affiliationIndependent.getFirstName());
            response.setSecondNameContributor(affiliationIndependent.getSecondName());
            response.setFirstSurNameContributor(affiliationIndependent.getSurname());
            response.setSecondSurNameContributor(affiliationIndependent.getSecondSurname());
        } else {
            AffiliationDependent affiliationDependent = affiliationDependentRepository.findByFiledNumber(employee.getFiledNumber())
                    .orElseThrow(() -> new AffiliateNotFound(Constant.AFFILIATE_NOT_FOUND));

            response.setMessageResponse("Ok");
            response.setFirstNameContributor(affiliationDependent.getFirstName());
            response.setSecondNameContributor(affiliationDependent.getSecondName());
            response.setFirstSurNameContributor(affiliationDependent.getSurname());
            response.setSecondSurNameContributor(affiliationDependent.getSecondSurname());
        }
    }

}
