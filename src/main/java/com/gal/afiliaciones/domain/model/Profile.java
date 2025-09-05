package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "perfil")
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_perfil", unique = true, nullable = false)
    private String profileCode;

    @Column(name = "perfil", nullable = false)
    private String profileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Status status;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "fecha_actualizacion")
    private LocalDate updateDate;

    @Column(name = "detalle", length = 255)
    private String detail;

    @Column(name = "usuario_creador_id", nullable = false)
    private Long userCreatorId;

    @Column(name = "usuario_editor_id")
    private Long userEditorId;

    @Column(name = "keycloak_id")
    private String keycloakId;

    @OneToMany(mappedBy = "profile")
    private List<PermissionProfile> permissions;

    public enum Status {
        ACTIVO,
        INACTIVO
    }

}
