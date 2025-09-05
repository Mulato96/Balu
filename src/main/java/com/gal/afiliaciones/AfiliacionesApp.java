package com.gal.afiliaciones;

import com.gal.afiliaciones.config.interceptor.TransactionLogFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@OpenAPIDefinition
@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.gal.afiliaciones.infrastructure.dao.repository")
public class AfiliacionesApp {
    @PostConstruct
    public void init(){
        // Establecer la zona horaria global
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Bogota")));
    }
    static{
        System.setProperty("jdk.tls.maxHandshakeMessageSize", "50000");
    }
    public static void main(String[] args) {
        SpringApplication.run(AfiliacionesApp.class, args);
    }

    @Bean
    public FilterRegistrationBean<TransactionLogFilter> transactionLogFilter(){
        FilterRegistrationBean<TransactionLogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TransactionLogFilter());
        registrationBean.addUrlPatterns("/*"); // Para todas las URL
        registrationBean.setOrder(1); // Asegúrate de que se ejecute en el orden deseado
        return registrationBean;
    }

}