package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.keycloak.GroupKeycloakRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.keycloak.RoleKeycloakRequestDTO;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

public interface KeycloakService {
    Map<String, Object> updateUser(String userId, String password);

    List<UserRepresentation> searchUserByUsername(String username);

    List<UserRepresentation> searchUserByUsernameComplete(String username);

    void createUser(UserPreRegisterDto userPreRegisterDto);

    List<RoleRepresentation> getAllRoles();

    List<GroupRepresentation> getAllGroups();

    GroupRepresentation assignRolesToGroup(String groupId, List<String> roleIds);

    RoleRepresentation createRole(RoleKeycloakRequestDTO roleName);

    GroupRepresentation createGroup(GroupKeycloakRequestDTO groupName);

    void assignUserToGroup(String username, String groupId);

    void assignRoleToUser(String userId, String roleName);

    void removeRolesFromGroup(String groupId, List<String> roleNames);

    void removeRoleFromUser(String userId, String roleName);

    void removeUserFromGroup(String userId, String groupId);

    void deleteRole(String roleName);

    void deleteGroup(String groupId);

    List<RoleRepresentation> getRolesByGroup(String groupId);

    Boolean updateGroupName(String groupId, GroupKeycloakRequestDTO newName);

    GroupRepresentation createSubGroup(String parentGroupId, GroupKeycloakRequestDTO subGroup);

    void moveSubGroup(String subGroupId, String newParentGroupId);

    void updateEmailUser(UserPreRegisterDto currentUser, String newEmail);

}
