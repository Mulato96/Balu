package com.gal.afiliaciones.infrastructure.utils;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyCloakProvider {

    @Value("${keycloak.SERVER.URL}")
    private String serverUrl;
    @Value("${keycloak.REALM.NAME}")
    private String realmName;
    @Value("${keycloak.REALM.MASTER}")
    private String realmMaster;
    @Value("${keycloak.ADMIN.CLI}")
    private String adminCli;
    @Value("${keycloak.USER.CONSOLE}")
    private String userConsole;
    @Value("${keycloak.PASSWORD.CONSOLE}")
    private String passwordConsole;
    @Value("${keycloak.CLIENT.SECRET}")
    private String clientSecret;


    public RealmResource getRealmResource() {

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmMaster)
                .clientId(adminCli)
                .username(userConsole)
                .password(passwordConsole)
                .clientSecret(clientSecret)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();

        return keycloak.realm(realmName);
    }

    public UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }
}
