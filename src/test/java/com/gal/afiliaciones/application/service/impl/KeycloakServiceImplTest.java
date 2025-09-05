package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gal.afiliaciones.config.ex.validationpreregister.ErrorAssignmentResourceKeycloak;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCreateResourceKeycloak;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCreateUserKeycloak;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorGetResourceKeycloak;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorUpdateUserKeycloak;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.keycloak.GroupKeycloakRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.keycloak.RoleKeycloakRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

import jakarta.ws.rs.core.Response;


@ExtendWith(MockitoExtension.class)
class KeycloakServiceImplTest {

    @Mock
    private KeyCloakProvider keyCloakProvider;

    @InjectMocks
    private KeycloakServiceImpl keycloakService;

    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private GroupsResource groupsResource;
    @Mock
    private GroupResource groupResource;
    @Mock
    private Response response;
    @Mock
    private Response.StatusType statusType;

    @BeforeEach
    void setUp() {
        // Common mocking for resources
        lenient().when(keyCloakProvider.getRealmResource()).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(realmResource.roles()).thenReturn(rolesResource);
        lenient().when(realmResource.groups()).thenReturn(groupsResource);
    }

    @Test
    @DisplayName("Search User By Username - Success")
    void searchUserByUsername_Success() {
        String username = "testuser";
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        when(usersResource.searchByEmail(username, true)).thenReturn(Collections.singletonList(user));

        List<UserRepresentation> result = keycloakService.searchUserByUsername(username);

        assertFalse(result.isEmpty());
        assertEquals(username, result.get(0).getUsername());
        verify(usersResource).searchByEmail(username, true);
    }

    @Test
    @DisplayName("Search User By Username - Not Found")
    void searchUserByUsername_NotFound() {
        String username = "nonexistentuser";
        when(usersResource.searchByEmail(username, true)).thenReturn(Collections.emptyList());

        List<UserRepresentation> result = keycloakService.searchUserByUsername(username);

        assertTrue(result.isEmpty());
        verify(usersResource).searchByEmail(username, true);
    }

    @Test
    @DisplayName("Update User - Success")
    void updateUser_Success() {
        String userId = "test@example.com";
        String password = "newPassword";
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId("user-id");
        userKeycloak.setEmail(userId);

        when(usersResource.searchByEmail(userId, true)).thenReturn(List.of(userKeycloak));
        when(keyCloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.get("user-id")).thenReturn(userResource);

        Map<String, Object> result = keycloakService.updateUser(userId, password);

        assertEquals("200", result.get("status"));
        assertEquals(Constant.PASSWORD_CHANGE_SUCCESSFUL, result.get("message"));
        verify(userResource).update(any(UserRepresentation.class));
    }

    @Test
    @DisplayName("Update User - User Not Found")
    void updateUser_UserNotFound() {
        String userId = "notfound@example.com";
        String password = "newPassword";
        when(usersResource.searchByEmail(userId, true)).thenReturn(Collections.emptyList());

        assertThrows(ErrorUpdateUserKeycloak.class,
                () -> keycloakService.updateUser(userId, password));

    }

    @Test
    @DisplayName("Create User - Success")
    void createUser_Success() {
        UserPreRegisterDto userDto = new UserPreRegisterDto();
        userDto.setUserName("newuser");
        userDto.setEmail("new@example.com");

        when(keyCloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);

        assertDoesNotThrow(() -> keycloakService.createUser(userDto));
        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    @DisplayName("Create User - Keycloak Error")
    void createUser_KeycloakError() {
        UserPreRegisterDto userDto = new UserPreRegisterDto();
        userDto.setUserName("newuser");

        when(keyCloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.getStatusInfo()).thenReturn(statusType);
        when(statusType.getStatusCode()).thenReturn(400);
        when(statusType.getReasonPhrase()).thenReturn("Bad Request");

        assertThrows(ErrorCreateUserKeycloak.class,
                () -> keycloakService.createUser(userDto));

    }

    @Test
    @DisplayName("Get All Roles - Success")
    void getAllRoles_Success() {
        when(rolesResource.list()).thenReturn(List.of(new RoleRepresentation()));

        List<RoleRepresentation> result = keycloakService.getAllRoles();

        assertFalse(result.isEmpty());
        verify(rolesResource).list();
    }

    @Test
    @DisplayName("Get All Roles - Error")
    void getAllRoles_Error() {
        when(rolesResource.list()).thenThrow(new RuntimeException("Keycloak down"));

        assertThrows(ErrorGetResourceKeycloak.class,
                () -> keycloakService.getAllRoles());

    }

    @Test
    @DisplayName("Get All Groups - Success")
    void getAllGroups_Success() {
        GroupRepresentation group = new GroupRepresentation();
        group.setId("group-id");
        group.setName("ParentGroup");
        group.setSubGroupCount(0L);
        when(groupsResource.groups()).thenReturn(new ArrayList<>(List.of(group)));

        List<GroupRepresentation> result = keycloakService.getAllGroups();

        assertFalse(result.isEmpty());
        assertEquals("ParentGroup", result.get(0).getName());
        verify(groupsResource).groups();
    }

    @Test
    @DisplayName("Assign Roles To Group - Success")
    void assignRolesToGroup_Success() {
        String groupId = "group-id";
        List<String> roleIds = List.of("role-id-1");
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
        RoleRepresentation role1 = new RoleRepresentation("role1", "desc", false);
        role1.setId("role-id-1");

        when(groupsResource.group(groupId)).thenReturn(groupResource);
        when(groupResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(rolesResource.list()).thenReturn(List.of(role1));
        when(rolesResource.get("role1")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role1);
        when(groupResource.toRepresentation()).thenReturn(new GroupRepresentation());

        keycloakService.assignRolesToGroup(groupId, roleIds);

        verify(roleScopeResource).add(anyList());
    }

    @Test
    @DisplayName("Create Role - Success")
    void createRole_Success() {
        RoleKeycloakRequestDTO roleDto = new RoleKeycloakRequestDTO();
        roleDto.setName("newRole");
        roleDto.setDescription("description");
        RoleRepresentation newRole = new RoleRepresentation();
        newRole.setName("newRole");

        when(rolesResource.list()).thenReturn(List.of(newRole));

        RoleRepresentation result = keycloakService.createRole(roleDto);

        assertNotNull(result);
        assertEquals("newRole", result.getName());
        verify(rolesResource).create(any(RoleRepresentation.class));
    }

    @Test
    @DisplayName("Create Group - Success")
    void createGroup_Success() {
        GroupKeycloakRequestDTO groupDto = new GroupKeycloakRequestDTO();
        groupDto.setName("newGroup");
        groupDto.setPath("/newGroup");
        GroupRepresentation newGroup = new GroupRepresentation();
        newGroup.setName("newGroup");

        when(groupsResource.groups()).thenReturn(Collections.emptyList()).thenReturn(List.of(newGroup));

        GroupRepresentation result = keycloakService.createGroup(groupDto);

        assertNotNull(result);
        assertEquals("newGroup", result.getName());
        verify(groupsResource).add(any(GroupRepresentation.class));
    }

    @Test
    @DisplayName("Create Group - Already Exists")
    void createGroup_AlreadyExists() {
        GroupKeycloakRequestDTO groupDto = new GroupKeycloakRequestDTO();
        groupDto.setName("existingGroup");
        groupDto.setPath("/existingGroup");
        GroupRepresentation existingGroup = new GroupRepresentation();
        existingGroup.setName("existingGroup");

        when(groupsResource.groups()).thenReturn(List.of(existingGroup));

        assertThrows(ErrorCreateResourceKeycloak.class,
                () -> keycloakService.createGroup(groupDto));

    }

    @Test
    @DisplayName("Assign User To Group - Success")
    void assignUserToGroup_Success() {
        String username = "testuser";
        String groupId = "group-id";
        UserRepresentation user = new UserRepresentation();
        user.setId("user-id");
        GroupRepresentation group = new GroupRepresentation();
        group.setId(groupId);
        group.setSubGroupCount(0L);

        when(usersResource.search(username)).thenReturn(List.of(user));
        when(usersResource.get("user-id")).thenReturn(userResource);
        when(groupsResource.group(groupId)).thenReturn(groupResource);
        when(groupResource.toRepresentation()).thenReturn(group);

        assertDoesNotThrow(() -> keycloakService.assignUserToGroup(username, groupId));

        verify(userResource).joinGroup(groupId);
    }

    @Test
    @DisplayName("Assign User To Group - User Not Found")
    void assignUserToGroup_UserNotFound() {
        String username = "notfound";
        String groupId = "group-id";

        when(usersResource.search(username)).thenReturn(Collections.emptyList());

        ErrorAssignmentResourceKeycloak exception = assertThrows(ErrorAssignmentResourceKeycloak.class,
                () -> keycloakService.assignUserToGroup(username, groupId));

        assertTrue(true);
    }

    @Test
    @DisplayName("Assign Role To User - Success")
    void assignRoleToUser_Success() {
        String userId = "user-id";
        String roleName = "ROLE_TEST";
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

        when(usersResource.get(userId)).thenReturn(userResource);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        assertDoesNotThrow(() -> keycloakService.assignRoleToUser(userId, roleName));

        verify(roleScopeResource).add(anyList());
    }

    @Test
    @DisplayName("Remove Roles From Group - Success")
    void removeRolesFromGroup_Success() {
        String groupId = "group-id";
        String roleName = "ROLE_TO_REMOVE";
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);

        when(groupsResource.group(groupId)).thenReturn(groupResource);
        when(groupResource.roles()).thenReturn(roleMappingResource);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(rolesResource.list()).thenReturn(List.of(role));

        assertDoesNotThrow(() -> keycloakService.removeRolesFromGroup(groupId, List.of(roleName)));

        assertTrue(true);
    }

    @Test
    @DisplayName("Remove Role From User - Success")
    void removeRoleFromUser_Success() {
        String userId = "user-id";
        String roleName = "ROLE_TO_REMOVE";
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);

        when(usersResource.get(userId)).thenReturn(userResource);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        assertDoesNotThrow(() -> keycloakService.removeRoleFromUser(userId, roleName));

        verify(roleScopeResource).remove(anyList());
    }

    @Test
    @DisplayName("Remove User From Group - Success")
    void removeUserFromGroup_Success() {
        String userId = "user-id";
        String groupId = "group-id";

        when(usersResource.get(userId)).thenReturn(userResource);

        assertDoesNotThrow(() -> keycloakService.removeUserFromGroup(userId, groupId));

        verify(userResource).leaveGroup(groupId);
    }

    @Test
    @DisplayName("Delete Role - Success")
    void deleteRole_Success() {
        String roleName = "ROLE_TO_DELETE";
        assertDoesNotThrow(() -> keycloakService.deleteRole(roleName));
        verify(rolesResource).deleteRole(roleName);
    }

    @Test
    @DisplayName("Delete Role - Cannot Delete Default Role")
    void deleteRole_CannotDeleteDefault() {
        String roleName = "ROLE_ADMIN";
        assertThrows(ErrorCreateResourceKeycloak.class,
                () -> keycloakService.deleteRole(roleName));
    }

    @Test
    @DisplayName("Delete Group - Success")
    void deleteGroup_Success() {
        String groupId = "group-to-delete";
        when(groupsResource.group(groupId)).thenReturn(groupResource);
        assertDoesNotThrow(() -> keycloakService.deleteGroup(groupId));
        verify(groupResource).remove();
    }

    @Test
    @DisplayName("Delete Group - Cannot Delete Default Group")
    void deleteGroup_CannotDeleteDefault() {
        String groupId = "default-group-id";
        assertThrows(ErrorCreateResourceKeycloak.class,
                () -> keycloakService.deleteGroup(groupId));
    }

    @Test
    @DisplayName("Get Roles By Group - Success")
    void getRolesByGroup_Success() {
        String groupId = "group-id";
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        when(groupsResource.group(groupId)).thenReturn(groupResource);
        when(groupResource.roles()).thenReturn(roleMappingResource);
        RoleScopeResource roleScopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listAll()).thenReturn(List.of(new RoleRepresentation()));

        List<RoleRepresentation> result = keycloakService.getRolesByGroup(groupId);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Update Group Name - Success")
    void updateGroupName_Success() {
        String groupId = "group-id";
        GroupKeycloakRequestDTO newNameDto = new GroupKeycloakRequestDTO();
        newNameDto.setName("newName");
        newNameDto.setPath("/newName");
        GroupRepresentation group = new GroupRepresentation();

        when(groupsResource.group(groupId)).thenReturn(groupResource);
        when(groupResource.toRepresentation()).thenReturn(group);

        Boolean result = keycloakService.updateGroupName(groupId, newNameDto);

        assertTrue(result);
        verify(groupResource).update(any(GroupRepresentation.class));
    }

    @Test
    @DisplayName("Create SubGroup - Success")
    void createSubGroup_Success() {
        String parentGroupId = "parent-id";
        GroupKeycloakRequestDTO subGroupDto = new GroupKeycloakRequestDTO();
        subGroupDto.setName("subGroup");
        subGroupDto.setPath("/subGroup");
        GroupRepresentation parentGroup = new GroupRepresentation();
        parentGroup.setId(parentGroupId);
        parentGroup.setSubGroupCount(1L);
        GroupRepresentation subGroup = new GroupRepresentation();
        subGroup.setName("subGroup");
        subGroup.setId("sub-id");
        subGroup.setSubGroupCount(0L);

        when(groupsResource.group(parentGroupId)).thenReturn(groupResource);
        when(groupResource.toRepresentation()).thenReturn(parentGroup);
        when(groupResource.getSubGroups(anyInt(), anyInt(), anyBoolean())).thenReturn(List.of(subGroup));

        GroupRepresentation result = keycloakService.createSubGroup(parentGroupId, subGroupDto);

        assertNotNull(result);
        assertEquals("subGroup", result.getName());
        verify(groupResource).subGroup(any(GroupRepresentation.class));
    }

    @Test
    @DisplayName("Move SubGroup - Success")
    void moveSubGroup_Success() {
        String subGroupId = "sub-id";
        String newParentGroupId = "new-parent-id";
        GroupResource subGroupResource = mock(GroupResource.class);
        GroupResource newParentGroupResource = mock(GroupResource.class);

        when(groupsResource.group(subGroupId)).thenReturn(subGroupResource);
        when(subGroupResource.toRepresentation()).thenReturn(new GroupRepresentation());
        when(groupsResource.group(newParentGroupId)).thenReturn(newParentGroupResource);

        assertDoesNotThrow(() -> keycloakService.moveSubGroup(subGroupId, newParentGroupId));

        verify(newParentGroupResource).subGroup(any(GroupRepresentation.class));
    }

    @Test
    @DisplayName("Update Email User - Success")
    void updateEmailUser_Success() {
        String newEmail = "new.email@example.com";
        UserPreRegisterDto currentUserDto = new UserPreRegisterDto();
        currentUserDto.setEmail("old.email@example.com");
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId("user-id");
        userKeycloak.setEmail("old.email@example.com");

        when(keyCloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.searchByEmail("old.email@example.com", true)).thenReturn(List.of(userKeycloak));
        when(usersResource.get("user-id")).thenReturn(userResource);

        assertDoesNotThrow(() -> keycloakService.updateEmailUser(currentUserDto, newEmail));

        verify(userResource).update(any(UserRepresentation.class));
    }

    @Test
    @DisplayName("Update Email User - User Not Found")
    void updateEmailUser_UserNotFound() {
        String newEmail = "new.email@example.com";
        UserPreRegisterDto currentUserDto = new UserPreRegisterDto();
        currentUserDto.setEmail("notfound@example.com");

        when(keyCloakProvider.getUserResource()).thenReturn(usersResource);
        when(usersResource.searchByEmail("notfound@example.com", true)).thenThrow(new NoSuchElementException(Constant.USER_NOT_FOUND));

        assertThrows(ErrorUpdateUserKeycloak.class,
                () -> keycloakService.updateEmailUser(currentUserDto, newEmail));

    }
}