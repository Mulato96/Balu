package com.gal.afiliaciones.application.service.validatecontributorelationship;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidRelationShipResponse;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;

import java.util.List;
import java.util.Optional;

public interface ValidateContributorRelationShipService {

    ValidRelationShipResponse validateRelationShip(ValidateContributorRequest request);

    Optional<Affiliate> findEmployee(List<Affiliate> employees, ValidateContributorRequest request);

    List<Affiliate> filterNonEmployerAffiliates(List<Affiliate> affiliates);
}
