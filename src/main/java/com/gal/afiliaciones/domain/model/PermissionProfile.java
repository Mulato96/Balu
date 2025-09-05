package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permiso_perfil")
public class PermissionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permiso_perfil_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "perfil_id")
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "permiso_id")
    private Permission permission;

}
