package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.domain.model.Permission;
import com.gal.afiliaciones.domain.model.PermissionProfile;
import com.gal.afiliaciones.domain.model.Profile;
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

class RolesUserServicesImplTest {

    private RoleRepository roleRepository;
    private PermissionProfileRepository permissionProfileRepository;
    private RoleProfileRepository roleProfileRepository;
    private UserRoleRepository userRoleRepository;
    private WorkspaceRoleRepository workspaceRoleRepository;

    private RolesUserServicesImpl service;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        permissionProfileRepository = mock(PermissionProfileRepository.class);
        roleProfileRepository = mock(RoleProfileRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        workspaceRoleRepository = mock(WorkspaceRoleRepository.class);

        service = new RolesUserServicesImpl(roleRepository, permissionProfileRepository, roleProfileRepository,
                userRoleRepository, workspaceRoleRepository);
    }

    @Test
    void getRolesByUser_withRoles_returnsRoleResponseLoginDTOs() {
        Long userId = 1L;
        UserRole userRole = new UserRole();
        userRole.setRoleId(10L);
        userRole.setUserId(userId);

        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(userRole));

        Role role = new Role();
        role.setId(10L);
        role.setRoleName("Admin");
        WorkspaceRole workspaceRole = new WorkspaceRole();
        workspaceRole.setId(100L);
        role.setWorkspace(workspaceRole);

        when(roleRepository.findById(10L)).thenReturn(Optional.of(role));

        RoleProfile roleProfile = new RoleProfile();
        Profile profile = new Profile();
        profile.setProfileName("Profile1");
        roleProfile.setProfile(profile);

        when(roleProfileRepository.findAllByRole(role)).thenReturn(List.of(roleProfile));

        Permission permission = new Permission();
        permission.setPermissionName("PERM_READ");
        PermissionProfile permissionProfile = new PermissionProfile();
        permissionProfile.setPermission(permission);

        when(permissionProfileRepository.findAllByProfile(profile)).thenReturn(List.of(permissionProfile));

        when(workspaceRoleRepository.findById(100L)).thenReturn(Optional.of(workspaceRole));

        List<RoleResponseLoginDTO> result = service.getRolesByUser(userId);

        assertNotNull(result);
        assertEquals(1, result.size());

        RoleResponseLoginDTO roleDTO = result.get(0);
        assertEquals("Admin", roleDTO.getNameRole());
        assertEquals(workspaceRole, roleDTO.getWorkspaceRole());

        List<ProfileResponseLoginDTO> profiles = roleDTO.getProfiles();
        assertEquals(1, profiles.size());
        ProfileResponseLoginDTO profileDTO = profiles.get(0);
        assertEquals("Profile1", profileDTO.getNameProfile());

        List<PermissionResponseLoginDTO> permissions = profileDTO.getPermissions();
        assertEquals(1, permissions.size());
        assertEquals("PERM_READ", permissions.get(0).getNamePermission());
    }

    @Test
    void getRolesByUser_withNoRoles_returnsEmptyList() {
        Long userId = 1L;
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<RoleResponseLoginDTO> result = service.getRolesByUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRolesByRoleName_withRole_returnsRoleResponseLoginDTOs() {
        String roleName = "Admin";

        Role role = new Role();
        role.setId(10L);
        role.setRoleName(roleName);
        WorkspaceRole workspaceRole = new WorkspaceRole();
        workspaceRole.setId(100L);
        role.setWorkspace(workspaceRole);

        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.of(role));

        RoleProfile roleProfile = new RoleProfile();
        Profile profile = new Profile();
        profile.setProfileName("Profile1");
        roleProfile.setProfile(profile);

        when(roleProfileRepository.findAllByRole(role)).thenReturn(List.of(roleProfile));

        Permission permission = new Permission();
        permission.setPermissionName("PERM_READ");
        PermissionProfile permissionProfile = new PermissionProfile();
        permissionProfile.setPermission(permission);

        when(permissionProfileRepository.findAllByProfile(profile)).thenReturn(List.of(permissionProfile));

        when(workspaceRoleRepository.findById(100L)).thenReturn(Optional.of(workspaceRole));

        List<RoleResponseLoginDTO> result = service.getRolesByRoleName(roleName);

        assertNotNull(result);
        assertEquals(1, result.size());

        RoleResponseLoginDTO roleDTO = result.get(0);
        assertEquals(roleName, roleDTO.getNameRole());
        assertEquals(workspaceRole, roleDTO.getWorkspaceRole());

        List<ProfileResponseLoginDTO> profiles = roleDTO.getProfiles();
        assertEquals(1, profiles.size());
        ProfileResponseLoginDTO profileDTO = profiles.get(0);
        assertEquals("Profile1", profileDTO.getNameProfile());

        List<PermissionResponseLoginDTO> permissions = profileDTO.getPermissions();
        assertEquals(1, permissions.size());
        assertEquals("PERM_READ", permissions.get(0).getNamePermission());
    }

    @Test
    void getRolesByRoleName_withNoRole_returnsEmptyList() {
        String roleName = "NonExistingRole";
        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.empty());

        List<RoleResponseLoginDTO> result = service.getRolesByRoleName(roleName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateRoleUser_roleNotAssigned_savesUserRole() {
        Long userId = 1L;
        Long roleId = 10L;

        UserRole existingUserRole = new UserRole();
        existingUserRole.setUserId(userId);
        existingUserRole.setRoleId(20L);

        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(existingUserRole));
        // roleId 10L not assigned yet

        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Boolean result = service.updateRoleUser(userId, roleId);

        assertTrue(result);
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void updateRoleUser_roleAlreadyAssigned_doesNotSaveUserRole() {
        Long userId = 1L;
        Long roleId = 10L;

        UserRole existingUserRole = new UserRole();
        existingUserRole.setUserId(userId);
        existingUserRole.setRoleId(roleId);

        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(existingUserRole));

        Boolean result = service.updateRoleUser(userId, roleId);

        assertTrue(result);
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void findByName_existingRole_returnsRole() {
        String roleName = "Admin";
        Role role = new Role();
        role.setRoleName(roleName);

        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.of(role));

        Role result = service.findByName(roleName);

        assertNotNull(result);
        assertEquals(roleName, result.getRoleName());
    }

    @Test
    void findByName_nonExistingRole_throwsResourceNotFoundException() {
        String roleName = "NonExistingRole";

        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByName(roleName));
    }
}