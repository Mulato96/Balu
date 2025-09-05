package com.gal.afiliaciones.domain.model.noveltyruaf;

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

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "novedad_ruaf")
public class NoveltyRuaf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo_arl")
    private String arlCode;
    @Column(name = "tipo_documento")
    private String identificationType;
    @Column(name = "numero_identificacion")
    private String identificationNumber;
    @Column(name = "primer_nombre")
    private String firstName;
    @Column(name = "segundo_nombre")
    private String secondName;
    @Column(name = "primer_apellido")
    private String surname;
    @Column(name = "segundo_apellido")
    private String secondSurname;
    @Column(name = "codigo_novedad")
    private String noveltyCode;
    @Column(name = "tipo_documento_aportante")
    private String identificationTypeContributor;
    @Column(name = "numero_identificacion_aportante")
    private String identificationNumberContributor;
    @Column(name = "dv_aportante")
    private Integer dvContributor;
    @Column(name = "fecha_desvinculacion_con_aportante")
    private LocalDate disassociationDateWithContributor;
    @Column(name = "fecha_novedad")
    private LocalDate noveltyDate;
    @Column(name = "causal_retiro")
    private Integer retirmentCausal;
    @Column(name = "fecha_reconocimiento_pension")
    private LocalDate pensionRecognitionDate;
    @Column(name = "fecha_fallecimiento")
    private LocalDate deathDate;
    @Column(name = "id_affiliate")
    private Long idAffiliate;

}
