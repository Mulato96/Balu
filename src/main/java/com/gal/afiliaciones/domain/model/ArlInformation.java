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
@Table(name = "informacion_arl")
public class ArlInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre")
    private String name;
    @Column(name = "codigo")
    private String code;
    @Column(name = "nit")
    private String nit;
    @Column(name = "email")
    private String email;
    @Column(name = "direccion")
    private String address;
    @Column(name = "sitio_web")
    private String website;
    @Column(name = "telefono")
    private String phoneNumber;
    @Column(name = "dv")
    private String dv;
    @Column(name = "otros_telefonos")
    private String otherPhoneNumbers;

}
