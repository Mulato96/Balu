package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "permiso")
@Entity
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permiso_id")
    private Long permissionId;

    @Column(name = "nombre_permiso", nullable = false)
    private String permissionName;

    @Column(name = "codigo",nullable = false)
    private String code;

    @Column(name = "activo",nullable = false)
    private Boolean active = true;

    @Column(name = "eliminado", nullable = false)
    private Boolean deleted = false;

    @Column(name = "keycloak_id")
    private String keycloakId;
}
