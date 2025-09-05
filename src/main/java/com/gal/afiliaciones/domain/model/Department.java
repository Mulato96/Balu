package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tmp_departamentos")
public class Department {

    @Id
    @Column(name = "id_departamento")
    private Integer idDepartment;

    @Column(name = "nombre_departamento")
    private String departmentName;

    @Column(name = "codigo")
    private String departmentCode;

}
