package com.gal.afiliaciones.config.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    private String UserPortalUrl;
    @Value("${bus.url.affiliate}")
    private String busUrlAffiliate;
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
    @Value("${work.center.url}")
    private String insertWorkCenterUrl;
    @Value("${volunteer.relation.url}")
    private String insertVolunteerUrl;

}
