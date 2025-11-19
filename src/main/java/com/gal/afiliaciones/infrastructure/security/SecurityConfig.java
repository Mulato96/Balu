package com.gal.afiliaciones.infrastructure.security;

import java.util.List;

import javax.ws.rs.HttpMethod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String REGISTRADURIA_CONSULT_ENDPOINT = "api/registraduria/consult";

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String defaultIssuerUri;

    @Value("${siarp.issuer-uri}")
    private String siarpIssuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf // Ignorar CSRF en estas rutas
                                .ignoringRequestMatchers(HttpMethod.POST,"/api/login")
                                .ignoringRequestMatchers(HttpMethod.POST,"/api/v1/integration-test/siarp/consultaEstadoAfiliado/raw")
                                .ignoringRequestMatchers(HttpMethod.GET,"/WSAlissta2/consultaEstadoAfiliado/**")
                                .ignoringRequestMatchers(HttpMethod.GET,"/WSAlissta2/consultaAfiliado2/**")
                                .ignoringRequestMatchers(HttpMethod.POST,"/api/refreshToken")
                                .ignoringRequestMatchers(HttpMethod.POST,"/api/users/register")
                                .ignoringRequestMatchers(HttpMethod.PUT,"/api/users/registerpassword")
                                .ignoringRequestMatchers(HttpMethod.POST,"/certificates/generate-create")
                                .ignoringRequestMatchers(HttpMethod.POST,"/consultcartificated/consultcertificated")
                                .ignoringRequestMatchers(HttpMethod.GET,"/certificates/findByTypeAndNumber")
                                .ignoringRequestMatchers(HttpMethod.GET,"/certificates/validate")
                                .ignoringRequestMatchers(HttpMethod.GET,"/consultcartificated/consultuser/**")
                                .ignoringRequestMatchers(HttpMethod.GET,"/usernotification/findAllAffiliatedUser")
                                .ignoringRequestMatchers(HttpMethod.GET,"/api/users/consulting/{identificationType}/{identification}")
                                .ignoringRequestMatchers(HttpMethod.POST,"/otp/**")
                                .ignoringRequestMatchers(HttpMethod.GET,"/api/users/consulting/{identification}")
                                .ignoringRequestMatchers(HttpMethod.GET,"/api/users/consultDvNit/{nit}")
                                .ignoringRequestMatchers(HttpMethod.POST,"/certificates/generate-certificate")
                                .ignoringRequestMatchers(HttpMethod.GET, "consultcard/cosultuserCard/**")
                                .ignoringRequestMatchers(HttpMethod.GET, "consultcard/generatedCard")
                                .ignoringRequestMatchers(HttpMethod.GET, "consultcard/consultCard/{id}")
                                .ignoringRequestMatchers(HttpMethod.GET, "affiliationdependent/search/{identificationType}/{identification}")
                                .ignoringRequestMatchers(HttpMethod.PUT,HttpMethod.POST,HttpMethod.DELETE,HttpMethod.GET, "keycloak/**")
                                .ignoringRequestMatchers(HttpMethod.GET, "/api/affiliates/by-type-and-number/**")
                                .ignoringRequestMatchers(HttpMethod.POST,"/certificates/create-non-affiliate-certificate/**")
                                .ignoringRequestMatchers(HttpMethod.GET, "/certificates/qr/{id}")
                                .ignoringRequestMatchers(HttpMethod.GET, "/api/activity/economic/**")
                                .ignoringRequestMatchers(HttpMethod.GET, "/preemploymentexamsite/findEntitiesByNameCity/**")
                                .ignoringRequestMatchers(HttpMethod.GET, "/actuator/prometheus/**")
                                .ignoringRequestMatchers(HttpMethod.POST, "novelty/createNovelty") // Se deja de manera temporal porque debe ser invocado desde recaudo
                                .ignoringRequestMatchers(HttpMethod.POST, "/inactive-pre-registred-users/active-account")
                                .ignoringRequestMatchers(HttpMethod.GET, "/api/affiliates/latest")
                                .ignoringRequestMatchers(HttpMethod.POST, "/affiliationemployeractivitiesmercantile/affiliate-bus")
                                .ignoringRequestMatchers(HttpMethod.POST, "/affiliationdependent/createaffiliation")
                                .ignoringRequestMatchers(HttpMethod.POST, "/api/person/insert")
                                .ignoringRequestMatchers(HttpMethod.POST, "/api/dependent-relationship/insert")
                                .ignoringRequestMatchers(HttpMethod.POST, "/api/independent-relationship/contract/insert")
                                .ignoringRequestMatchers(HttpMethod.POST, "/poliza/crear")
                                .ignoringRequestMatchers(HttpMethod.POST, REGISTRADURIA_CONSULT_ENDPOINT)
                                .ignoringRequestMatchers(HttpMethod.GET, REGISTRADURIA_CONSULT_ENDPOINT + "/**")
                                .ignoringRequestMatchers(HttpMethod.POST, "/api/webhook/employer/excel/async")
                                .ignoringRequestMatchers(HttpMethod.GET, "/afiliado-postiva/**")
                                .ignoringRequestMatchers(HttpMethod.POST, "/consultcartificated/generatecertificatebalu")
                                .ignoringRequestMatchers(HttpMethod.GET, "/employermigrated/searchEmployer/**")
                                .ignoringRequestMatchers(HttpMethod.GET, HttpMethod.POST, "/value-contract/**")
//                        .ignoringRequestMatchers(HttpMethod.GET, "/api/confecamaras/consult/**")
                ).authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v2/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/websocket"
                        ).permitAll() // Permitir acceso a Swagger UI y API Docs
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/refreshToken").permitAll()
                        .requestMatchers(HttpMethod.GET,"/WSAlissta2/consultaEstadoAfiliado/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/WSAlissta2/consultaAfiliado2/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/registerpassword").permitAll()
                        .requestMatchers(HttpMethod.POST, "/certificates/generate-create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usernotification/findAllAffiliatedUser").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/consulting/{identificationType}/{identification}").permitAll()
                        .requestMatchers(HttpMethod.POST,"/consultcartificated/consultcertificated").permitAll()
                        .requestMatchers(HttpMethod.GET,"/certificates/findByTypeAndNumber").permitAll()
                        .requestMatchers(HttpMethod.GET,"/certificates/validate").permitAll()
                        .requestMatchers(HttpMethod.GET,"/consultcartificated/consultuser/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/otp/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/consulting/{identification}").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/consultDvNit/{nit}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/certificates/generate-certificate").permitAll()
                        .requestMatchers(HttpMethod.GET, "consultcard/cosultuserCard/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "consultcard/generatedCard").permitAll()
                        .requestMatchers(HttpMethod.GET, "consultcard/consultCard/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "affiliationdependent/search/{identificationType}/{identification}").permitAll()
                        .requestMatchers(HttpMethod.PUT,HttpMethod.POST,HttpMethod.DELETE,HttpMethod.GET, "keycloak/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/affiliates/by-type-and-number/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/certificates/create-non-affiliate-certificate/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/certificates/qr/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/activity/economic/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/preemploymentexamsite/findEntitiesByNameCity/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "novelty/createNovelty").permitAll() // Se deja de manera temporal porque debe ser invocado desde recaudo
                        .requestMatchers(HttpMethod.POST, "/inactive-pre-registred-users/active-account").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/affiliates/latest").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/consulting/username").permitAll()
                        .requestMatchers(HttpMethod.GET, "/business-group").permitAll()
                        .requestMatchers(HttpMethod.GET, "/legal-representative").permitAll()
                        .requestMatchers(HttpMethod.GET, "/employer/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/affiliationemployeractivitiesmercantile/affiliate-bus").permitAll()
                        .requestMatchers(HttpMethod.POST,"/affiliationdependent/createaffiliation").permitAll()
                        .requestMatchers(HttpMethod.GET,"/user-portal").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/person/insert").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/dependent-relationship/insert").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/independent-relationship/contract/insert").permitAll()
                        .requestMatchers(HttpMethod.POST, REGISTRADURIA_CONSULT_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.GET, REGISTRADURIA_CONSULT_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.POST, "/consultcartificated/generatecertificatebalu").permitAll()
                        .requestMatchers(HttpMethod.GET, "/employermigrated/searchEmployer/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/webhook/employer/excel/async").permitAll()
                        .requestMatchers(HttpMethod.POST,"/poliza/crear").permitAll()
                        .requestMatchers(HttpMethod.GET,"/user-portal").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/confecamaras/consult/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/afiliado-postiva/**").permitAll()
                        .requestMatchers(HttpMethod.GET, HttpMethod.POST, "/value-contract/**").permitAll()
                        .anyRequest().authenticated() // Proteger endpoint con permisos
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(bearerTokenResolver())
                        .authenticationManagerResolver(authenticationManagerResolver())
                );

        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return delegate::resolve;
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        return JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(
                defaultIssuerUri,
                siarpIssuerUri
        );
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "https://operacionesarl.positiva.gov.co",
                "https://positiva-arl.vercel.app",
                "https://prod-grafana-gal.linktic.com",
                "https://gal-back.linktic.com",
                "https://gal-back-qa.linktic.com",
                "https://gal-back-dev.linktic.com"
        ));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        cfg.setAllowedHeaders(List.of(
                "Origin", "Content-Type", "Accept", "Authorization",
                "X-Requested-With", "Cache-Control",

                "Content-Disposition", "Content-Length", "Content-Transfer-Encoding",
                "X-File-Name", "X-File-Size", "X-File-Type",

                "Accept-Encoding", "Accept-Language", "User-Agent",

                "X-Application-Version", "X-Request-ID", "X-Client-ID",

                "SOAPAction",

                "X-Forwarded-For", "X-Real-IP", "X-Forwarded-Host",

                "X-Upload-File-Name", "X-Upload-File-Size", "X-Upload-File-Type",
                "X-Download-File-Name", "X-Download-File-Size",

                "X-Transaction-ID", "X-Session-ID", "X-Request-Timestamp",

                "Access-Control-Allow-Credentials", "Access-Control-Allow-Origin",
                "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
                "X-User-Role","role"
        ));

        cfg.setExposedHeaders(List.of(
                "Authorization", "Content-Type", "Content-Disposition",
                "Content-Length", "Content-Transfer-Encoding",

                "X-File-Name", "X-File-Size", "X-File-Type",
                "X-Upload-File-Name", "X-Upload-File-Size", "X-Upload-File-Type",
                "X-Download-File-Name", "X-Download-File-Size",

                "X-Application-Version", "X-Request-ID", "X-Client-ID",
                "X-Transaction-ID", "X-Session-ID", "X-Request-Timestamp",

                "X-Response-Time", "X-Response-Size", "X-Response-Status"
        ));

        cfg.setMaxAge(3600L);

        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}