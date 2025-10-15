package com.gal.afiliaciones.config.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class AffiliationProperties {

    @Value("${keycloak.auth-server-integration}")
    private String keycloakUrl;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.CLIENT.SECRET}")
    private String clientSecret;
    @Value("${billing.scheduler.cron}")
    private String cronExpression;
    @Value("${spring.security.oauth2.resourceserver.jwt.logout-uri}")
    private String logoutUrl;
    @Value("${business-group-url}")
    private String businessGroupUrl;
    @Value("${legal-representative.url}")
    private String legalRepresentativeUrl;
    
    @Value("${employer.url}")
    private String busUrlEmployer;

    @Value("${user.portal.url}")
    private String userPortalUrl;

    @Value("${bus.url.affiliate}")
    private String busUrlAffiliate;
    @Value("${bus.url.affiliate.v1:}")
    private String busUrlAffiliateV1;
    @Value("${bus.url.person}")
    private String busUrlPerson;
    @Value("${person.url}")
    private String insertPersonUrl;
    @Value("${dependent.relation.url}")
    private String dependentRelationshipUrl;
    @Value("${independent.relation.url}")
    private String independentContractRelationshipUrl;
    @Value("${policy.url}")
    private String insertPolicyUrl;
    @Value("${policy.consult.url}")
    private String policyConsultUrl;
    @Value("${branch.consult.url}")
    private String branchConsultUrl;
    @Value("${headquarters.consult.url}")
    private String headquartersConsultUrl;
    @Value("${headquarters.insert.url}")
    private String insertHeadquartersUrl;
    @Value("${headquarters.update.url}")
    private String headquartersUpdateUrl;
    @Value("${work.center.url}")
    private String insertWorkCenterUrl;
    @Value("${work.center.consult.url}")
    private String workCenterConsultUrl;
    @Value("${work.center.update.url}")
    private String workCenterUpdateUrl;
    @Value("${volunteer.relation.url}")
    private String insertVolunteerUrl;
    @Value("${sat.consult.transferable.employer.url}")
    private String satConsultTransferableEmployerUrl;

    @Value("${worker.displacement.notification.url}")
    private String workerDisplacementNotificationUrl;

    @Value("${worker.retirement.novelty.url:}")
    private String workerRetirementNoveltyUrl;

    @Value("${worker.retirement.novelty.enabled:false}")
    private boolean workerRetirementNoveltyEnabled;


    @Value("${siarp.consultaAfiliado2.url:}")
    private String siarpConsultaAfiliado2Url;

    @Value("${employer.activities.consult.url}")
    private String employerActivitiesConsultUrl;

    @Value("${employer.activation.consult.url}")
    private String employerActivationConsultUrl;

    @Value("${worker.position.update.url}")
    private String workerPositionUpdateUrl;

    @Value("${independent.economic.activity.update.url}")
    private String independentEconomicActivityUpdateUrl;

    @Value("${independent.contract.date.update.url}")
    private String independentContractDateUpdateUrl;

    @Value("${independent.occupation.update.url}")
    private String independentOccupationUpdateUrl;
}
