package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.domain.model.PermissionProfile;
import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.RoleProfile;
import com.gal.afiliaciones.domain.model.UserRole;
import com.gal.afiliaciones.domain.model.WorkspaceRole;
import com.gal.afiliaciones.infrastructure.dao.repository.PermissionProfileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RoleProfileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RoleRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.UserRoleRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.WorkspaceRoleRepository;
import com.gal.afiliaciones.infrastructure.dto.login.PermissionResponseLoginDTO;
import com.gal.afiliaciones.infrastructure.dto.login.ProfileResponseLoginDTO;
import com.gal.afiliaciones.infrastructure.dto.login.RoleResponseLoginDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolesUserServicesImpl implements RolesUserService {

    private final RoleRepository roleRepository;
    private final PermissionProfileRepository permissionProfileRepository;
    private final RoleProfileRepository roleProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;

    @Override
    public List<RoleResponseLoginDTO> getRolesByUser(Long userId) {
        List<RoleResponseLoginDTO> responseLoginDTOS = new ArrayList<>();
        List<UserRole> allByIdUser = userRoleRepository.findAllByUserId(userId);

        allByIdUser.forEach(userRole -> {
            Optional<Role> optionalRole = roleRepository.findById(userRole.getRoleId());

            if (optionalRole.isPresent()) {
                Role role = optionalRole.get();
                List<ProfileResponseLoginDTO> profileResponseLoginDTOS = new ArrayList<>();
                List<RoleProfile> allByRole = roleProfileRepository.findAllByRole(role);
                WorkspaceRole workspaceRole = workspaceRoleRepository.findById(role.getWorkspace().getId()).orElse(null);

                allByRole.forEach(roleProfile -> {
                    List<PermissionResponseLoginDTO> permissionResponseLoginDTOS = new ArrayList<>();
                    List<PermissionProfile> allByProfile = permissionProfileRepository.findAllByProfile(roleProfile.getProfile());

                    allByProfile.forEach(permissionProfile ->
                            permissionResponseLoginDTOS.add(PermissionResponseLoginDTO.builder()
                                    .namePermission(permissionProfile.getPermission().getPermissionName()).build()));

                    profileResponseLoginDTOS.add(ProfileResponseLoginDTO.builder()
                            .nameProfile(roleProfile.getProfile().getProfileName())
                            .permissions(permissionResponseLoginDTOS)
                            .build());
                });
                responseLoginDTOS.add(RoleResponseLoginDTO.builder()
                        .nameRole(role.getRoleName())
                        .profiles(profileResponseLoginDTOS)
                        .workspaceRole(workspaceRole)
                        .build());
            }
        });
        return responseLoginDTOS;
    }

    @Override
    public List<RoleResponseLoginDTO> getRolesByRoleName(String roleName) {
        List<RoleResponseLoginDTO> responseLoginDTOS = new ArrayList<>();
        Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            List<ProfileResponseLoginDTO> profileResponseLoginDTOS = new ArrayList<>();
            List<RoleProfile> allByRole = roleProfileRepository.findAllByRole(role);
            WorkspaceRole workspaceRole = workspaceRoleRepository.findById(role.getWorkspace().getId()).orElse(null);

            allByRole.forEach(roleProfile -> {
                List<PermissionResponseLoginDTO> permissionResponseLoginDTOS = new ArrayList<>();
                List<PermissionProfile> allByProfile = permissionProfileRepository.findAllByProfile(roleProfile.getProfile());

                allByProfile.forEach(permissionProfile ->
                        permissionResponseLoginDTOS.add(PermissionResponseLoginDTO.builder()
                                .namePermission(permissionProfile.getPermission().getPermissionName()).build()));

                profileResponseLoginDTOS.add(ProfileResponseLoginDTO.builder()
                        .nameProfile(roleProfile.getProfile().getProfileName())
                        .permissions(permissionResponseLoginDTOS)
                        .build());
            });
            responseLoginDTOS.add(RoleResponseLoginDTO.builder()
                    .nameRole(role.getRoleName())
                    .profiles(profileResponseLoginDTOS)
                    .workspaceRole(workspaceRole)
                    .build());
        }
        return responseLoginDTOS ;
    }

    @Override
    public Boolean updateRoleUser(Long userId, Long roleId) {
        if(Boolean.FALSE.equals(alreadyAssignedRole(userId, roleId))) {
            UserRole userRole = new UserRole();
            userRole.setRoleId(roleId);
            userRole.setUserId(userId);
            userRoleRepository.save(userRole);
        }
        return true;
    }

    private Boolean alreadyAssignedRole(Long userId, Long roleId){
        List<UserRole> rolesByUserList = userRoleRepository.findAllByUserId(userId);
        if(!rolesByUserList.isEmpty()){
            List<Long> roleIdList = rolesByUserList.stream().map(UserRole::getRoleId).toList();
            return !roleIdList.isEmpty() && roleIdList.contains(roleId);
        }
        return false;
    }

    @Override
    public Role findByName(String name){
        return roleRepository.findByRoleName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role cannot exists"));
    }

}
