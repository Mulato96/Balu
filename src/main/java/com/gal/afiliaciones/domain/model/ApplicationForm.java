package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "formulario")
public class ApplicationForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "radicado_afiliacion")
    private String filedNumberAffiliation;
    @Column(name = "tipo_identificacion")
    private String identificationType;
    @Column(name = "numero_identificacion")
    private String identificationNumber;
    @Column(name = "fecha_expedicion")
    private String expeditionDate;
    @Column(name = "radicado_documento")
    private String filedNumberDocument;

}
