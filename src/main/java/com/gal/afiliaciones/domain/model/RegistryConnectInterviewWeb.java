package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "registry_connect_interview_web")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistryConnectInterviewWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number_filed")
    private String numberFiled;

    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "state")
    private String state;

    @Column(name = "type_user")
    private String typeUser;
}
