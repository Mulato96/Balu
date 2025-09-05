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
@Table(name = "entidad_examen_preocupacional")
public class PreEmploymentExamSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre")
    private String nameSite;
    @Column(name = "telefono")
    private Long phoneNumber;
    @Column(name = "sitio_web")
    private String webSite;
    @Column(name = "id_departamento")
    private Long idDepartment;
    @Column(name = "id_municipio")
    private Long idMunicipality;
    @Column(name = "latitud")
    private String latitude;
    @Column(name = "longitud")
    private String longitude;
    @Column(name = "direccion")
    private String address;
    @Column(name = "id_calle_principal")
    private Long idMainStreet;
    @Column(name = "id_numero_calle_principal")
    private Long idNumberMainStreet;
    @Column(name = "id_letra1_calle_principal")
    private Long idLetter1MainStreet;
    @Column(name = "bis")
    private Boolean isBis;
    @Column(name = "id_letra2_calle_principal")
    private Long idLetter2MainStreet;
    @Column(name = "id_punto_cardinal_calle_principal")
    private Long idCardinalPointMainStreet;
    @Column(name = "id_numero1_segunda_calle")
    private Long idNum1SecondStreet;
    @Column(name = "id_letra_segunda_calle")
    private Long idLetterSecondStreet;
    @Column(name = "id_numero2_segunda_calle")
    private Long idNum2SecondStreet;
    @Column(name = "id_punto_cardinal2")
    private Long idCardinalPoint2;
    @Column(name = "id_propiedad_horizontal1")
    private Long idHorizontalProperty1;
    @Column(name = "id_numero_propiedad_horizontal1")
    private Long idNumHorizontalProperty1;
    @Column(name = "id_propiedad_horizontal2")
    private Long idHorizontalProperty2;
    @Column(name = "id_numero_propiedad_horizontal2")
    private Long idNumHorizontalProperty2;
    @Column(name = "id_propiedad_horizontal3")
    private Long idHorizontalProperty3;
    @Column(name = "id_numero_propiedad_horizontal3")
    private Long idNumHorizontalProperty3;
    @Column(name = "id_propiedad_horizontal4")
    private Long idHorizontalProperty4;
    @Column(name = "id_numero_propiedad_horizontal4")
    private Long idNumHorizontalProperty4;

}
