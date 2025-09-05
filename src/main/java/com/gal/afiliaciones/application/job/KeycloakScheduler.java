package com.gal.afiliaciones.application.job;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.domain.model.*;
import com.gal.afiliaciones.infrastructure.dao.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakScheduler {
    public static final String TEMPORAL = "Temporal";
    public static final String TIME_ZONE = "America/Bogota";
    private final KeycloakService keycloakService;
    private final PermissionRepository permissionRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PermissionProfileRepository permissionProfileRepository;
    private final RoleProfileRepository roleProfileRepository;
    private final StateRepository stateRepository;

    @Transactional
    public Boolean setPermission() {
        List<RoleRepresentation> allRoles = keycloakService.getAllRoles();
        allRoles.forEach(roleRepresentation -> {
            Optional<Permission> byKeycloakId = permissionRepository.findByKeycloakId(roleRepresentation.getId());

            if (byKeycloakId.isEmpty()) {
                savedPermission(roleRepresentation);
            }
        });

        return true;
    }

    private Permission savedPermission(RoleRepresentation roleRepresentation) {
        Permission temporal = permissionRepository.save(Permission.builder()
                .permissionName(roleRepresentation.getName())
                .code(TEMPORAL)
                .active(true)
                .deleted(false)
                .keycloakId(roleRepresentation.getId())
                .build());

        temporal.setCode("P".concat(temporal.getPermissionId().toString()));
        return permissionRepository.save(temporal);
    }

    @Transactional
    public Boolean setProfilesAndRoles() {
        List<GroupRepresentation> allGroups = keycloakService.getAllGroups();
        State active = stateRepository.findById(1L).orElse(State.builder().id(1L).stateName("Activo").build());
        LocalDateTime now = LocalDateTime.now(ZoneId.of(TIME_ZONE));
        LocalDate nowDate = LocalDate.now(ZoneId.of(TIME_ZONE));

        allGroups.forEach(groupRepresentation -> {
            Optional<Profile> profile = profileRepository.findByKeycloakId(groupRepresentation.getId());
            Optional<Role> role = roleRepository.findByKeycloakId(groupRepresentation.getId());

            if (profile.isEmpty() && role.isEmpty()) {
                Role temporalRole = roleRepository.save(Role.builder()
                        .code(TEMPORAL)
                        .roleName(groupRepresentation.getName())
                        .status(active)
                        .createDate(now)
                        .employeeName("predefined role")
                        .keycloakId(groupRepresentation.getId())
                        .build());

                temporalRole.setCode("P".concat(temporalRole.getId().toString()));

                Role savedRole = roleRepository.save(temporalRole);

                groupRepresentation.getSubGroups().forEach(groupRepresentation1 -> {
                    Optional<Profile> profile1 = profileRepository.findByKeycloakId(groupRepresentation1.getId());
                    Optional<Role> role1 = roleRepository.findByKeycloakId(groupRepresentation1.getId());

                    if (profile1.isEmpty() && role1.isEmpty()) {
                        Profile temporalProfile = profileRepository.save(Profile.builder()
                                .profileCode(TEMPORAL)
                                .profileName(groupRepresentation1.getName())
                                .status(Profile.Status.ACTIVO)
                                .registrationDate(nowDate)
                                .detail("predefined profile")
                                .keycloakId(groupRepresentation1.getId())
                                .build());

                        temporalProfile.setProfileCode("P".concat(temporalProfile.getId().toString()));

                        Profile savedProfile = profileRepository.save(temporalProfile);

                        roleProfileRepository.save(RoleProfile.builder()
                                .role(savedRole)
                                .profile(savedProfile).build());

                        List<RoleRepresentation> permissionByProfile = keycloakService.getRolesByGroup(groupRepresentation1.getId());

                        permissionByProfile.forEach(roleRepresentation -> {
                            Permission permission = permissionRepository.findAllByKeycloakId(roleRepresentation.getId()).stream().findFirst().orElse(savedPermission(roleRepresentation));

                            permissionProfileRepository.save(PermissionProfile.builder()
                                    .profile(savedProfile)
                                    .permission(permission)
                                    .build());
                        });
                    }
                });
            }
        });

        return true;
    }
}
