package com.gal.afiliaciones.infrastructure.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.ws.rs.HttpMethod;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf // Ignorar CSRF en estas rutas
                        .ignoringRequestMatchers(HttpMethod.POST,"/api/login")
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
                        .anyRequest().authenticated() // Proteger endpoint con permisos
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
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
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 🔑 Acepta cualquier origen
        cfg.setAllowedOriginPatterns(List.of("*"));

        // 🔑 Acepta todos los métodos y cabeceras
        cfg.setAllowedMethods(List.of("*"));
        cfg.setAllowedHeaders(List.of("*"));

        // Si SÍ vas a pasar cookies o Authentication:Bearer entre dominios
        cfg.setAllowCredentials(true);   // con allowedOriginPatterns("*") no explota

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}