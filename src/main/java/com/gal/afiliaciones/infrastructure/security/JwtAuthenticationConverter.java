package com.gal.afiliaciones.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principal-attribute}")
    private String principalAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {

        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter.convert(source).stream(), extractResourceRoles(source).stream()).
                toList();

        return new JwtAuthenticationToken(source, authorities, getPrincipaleName(source));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt){

        Map<String, Object> resourceAccess;
        Collection<String> resourceRoles;

        if(jwt.getClaim("realm_access") == null){
            return List.of();
        }

        resourceAccess = jwt.getClaim("realm_access");



        if(resourceAccess.get("permisos") == null){
            return List.of();
        }

        resourceRoles = (Collection<String>) resourceAccess.get("permisos");
        return resourceRoles.stream().map(SimpleGrantedAuthority::new).toList();

    }

    private String getPrincipaleName(Jwt jwt){

        return (principalAttribute != null) ? jwt.getClaim(principalAttribute) : jwt.getClaim(JwtClaimNames.SUB);
    }

}
