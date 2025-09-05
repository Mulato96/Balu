package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tmp_municipality")
public class Municipality {

    @Id
    @Column(name = "id_municipio")
    private Long idMunicipality;
    @Column(name = "id_departamento")
    private Long idDepartment;
    @Column(name = "nombre_municipio")
    private String municipalityName;
    @Column(name = "divipola")
    private String divipolaCode;
    @Column(name = "codigo_departamento")
    private String departmentCode;
    @Column(name = "codigo_municipio")
    private String municipalityCode;

}
