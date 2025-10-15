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
@Table(name = "grupo_empresarial")
public class BusinessGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_grupo_empresarial")
    private Long idBusinessGroup;
    @Column(name = "nombre_grupo_empresarial")
    private String nameBusinessGroup;
    @Column(name = "id_affiliate")
    private Long idAffiliate;
    @Column(name = "es_empresa_principal")
    private Boolean isMainCompany;

}

