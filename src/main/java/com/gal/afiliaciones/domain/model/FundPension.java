package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name= "tmp_fondos_pensiones")
public class FundPension {

    @Id
    @Column(name = "id_afp")
    private Integer idAfp;

    @Column(name = "nombre_afp")
    private String nameAfp;

    @Column(name = "codigo_afp")
    private Long codeAfp;

}
