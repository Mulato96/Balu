package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tmp_ocupaciones")
public class Occupation {

    @Id
    @Column(name = "id_ocupacion")
    private Long idOccupation;
    @Column(name = "codigo_ocupacion")
    private String codeOccupation;
    @Column(name = "nombre_ocupacion")
    private String nameOccupation;

}
