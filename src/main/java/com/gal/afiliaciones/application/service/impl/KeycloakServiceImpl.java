package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.KeycloakService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {
    private final KeyCloakProvider keyCloakProvider;

    @Override
    public List<UserRepresentation> searchUserByUsername(String username) {
        return Optional.ofNullable(keyCloakProvider.getRealmResource().users().searchByEmail(username, true))
                .orElse(Collections.emptyList());
    }

    @Override
    public Map<String, Object> updateUser(String userId, String password) {
        try {
            UserRepresentation userRepresentation = new UserRepresentation();

            userRepresentation.setCredentials(getCredentialRepresentations(password));

            UserRepresentation userKeycloak = searchUserByUsername(userId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(Constant.USER_NOT_FOUND));

            userKeycloak.setRequiredActions(List.of());

            UserResource usersResource = keyCloakProvider.getUserResource().get(userKeycloak.getId());

            usersResource.update(userRepresentation);
            return Map.of("status", "200", "message", Constant.PASSWORD_CHANGE_SUCCESSFUL);
        } catch (Exception e) {
            String message = e.getMessage().equals(Constant.USER_NOT_FOUND) ? Constant.USER_NOT_FOUND : Constant.ERROR_UPDATE_USER_KEYCLOAK;

            throw new ErrorUpdateUserKeycloak(message);
        }
    }

    @Override
    public void createUser(UserPreRegisterDto userPreRegisterDto) {
        UsersResource usersResource = keyCloakProvider.getUserResource();
        UserRepresentation userRepresentation = getUserRepresentation(userPreRegisterDto);

        try (Response responseKeycloak = usersResource.create(userRepresentation)) {

            if (responseKeycloak.getStatus() != 201) {
                String message = "code: " + responseKeycloak.getStatusInfo().getStatusCode() + ", message: " + responseKeycloak.getStatusInfo().getReasonPhrase();
                log.error("Create User: {}", message);
                throw new ErrorCreateUserKeycloak(Constant.ERROR_CREATE_USER_KEYCLOAK);
            }

        } catch (Exception e) {
            throw new ErrorCreateUserKeycloak(Constant.ERROR_CREATE_USER_KEYCLOAK);
        }
    }

    @Override
    public List<RoleRepresentation> getAllRoles() {
        try {
            RolesResource rolesResource = keyCloakProvider.getRealmResource().roles();
            return rolesResource.list();
        } catch (Exception e) {
            log.error("Error getting all roles", e);
            throw new ErrorGetResourceKeycloak("Error getting all roles");
        }
    }

    @Override
    public List<GroupRepresentation> getAllGroups() {
        try {
            GroupsResource groupsResource = keyCloakProvider.getRealmResource().groups();
            List<GroupRepresentation> allGroups = groupsResource.groups();

            allGroups.forEach(this::populateSubGroups);
            return allGroups;
        } catch (Exception e) {
            log.error("Error getting all groups", e);
            throw new ErrorGetResourceKeycloak("Error getting all groups");
        }
    }

    private void populateSubGroups(GroupRepresentation groupRepresentation) {
        try {
            GroupResource groupResource = keyCloakProvider.getRealmResource().groups().group(groupRepresentation.getId());
            List<GroupRepresentation> subGroups = groupResource.getSubGroups(0, groupRepresentation.getSubGroupCount().intValue(), false);

            if (subGroups != null && !subGroups.isEmpty()) {
                groupRepresentation.setSubGroups(subGroups);
                subGroups.forEach(this::populateSubGroups);
            }
        } catch (Exception e) {
            log.error("Error populating subgroups for group: {}", groupRepresentation.getName(), e);
        }
    }

    @Override
    public GroupRepresentation assignRolesToGroup(String groupId, List<String> roleIds) {
        try {
            GroupsResource groupsResource = keyCloakProvider.getRealmResource().groups();
            GroupResource groupResource = groupsResource.group(groupId);
            RolesResource rolesResource = keyCloakProvider.getRealmResource().roles();
            List<RoleRepresentation> currentRoles = groupResource.roles().realmLevel().listAll();

            if (!currentRoles.isEmpty()) {
                groupResource.roles().realmLevel().remove(currentRoles);
            }

            List<RoleRepresentation> list = rolesResource.list();
            List<String> rolNames = new ArrayList<>();

            list.forEach(roleRepresentation -> roleIds.forEach(id -> {
                if (roleRepresentation.getId().equals(id))
                    rolNames.add(roleRepresentation.getName());
            }));

            List<RoleRepresentation> rolesToAdd = rolNames.stream()
                    .map(rolesResource::get)
                    .map(RoleResource::toRepresentation)
                    .toList();

            groupResource.roles().realmLevel().add(rolesToAdd);
            return groupResource.toRepresentation();
        } catch (Exception e) {
            log.error("Error assigning roles to group", e);
            throw new ErrorAssignmentResourceKeycloak("Error assigning roles to group");
        }
    }

    @Override
    public RoleRepresentation createRole(RoleKeycloakRequestDTO role) {
        try {
            RolesResource rolesResource = keyCloakProvider.getRealmResource().roles();
            RoleRepresentation roleRepresentation = new RoleRepresentation();

            roleRepresentation.setName(role.getName());
            roleRepresentation.setDescription(role.getDescription());
            rolesResource.create(roleRepresentation);
            return keyCloakProvider.getRealmResource().roles().list().stream().
                    filter(r -> r.getName().equals(role.getName())).findFirst().orElse(roleRepresentation);
        } catch (ErrorCreateResourceKeycloak e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating role", e);
            throw new ErrorCreateResourceKeycloak("Error creating role");
        }
    }

    @Override
    public GroupRepresentation createGroup(GroupKeycloakRequestDTO group) {
        try {
            GroupsResource groupsResource = keyCloakProvider.getRealmResource().groups();
            List<GroupRepresentation> existingGroups = groupsResource.groups();
            boolean groupExists = existingGroups.stream().anyMatch(g -> g.getName().equals(group.getName()));

            if (groupExists) {
                String message = "Grupo con el nombre '" + group.getName() + "' ya existe en el sistema.";
                log.warn(message);
                throw new ErrorCreateResourceKeycloak(message, HttpStatus.CONFLICT);
            }

            GroupRepresentation groupRepresentation = new GroupRepresentation();

            groupRepresentation.setName(group.getName());
            groupRepresentation.setPath(group.getPath());
            groupsResource.add(groupRepresentation);
            return keyCloakProvider.getRealmResource().groups().groups().stream().
                    filter(g -> g.getName().equals(group.getName())).findFirst().orElse(groupRepresentation);
        } catch (ErrorCreateResourceKeycloak e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating group", e);
            throw new ErrorCreateResourceKeycloak("Error creating group");
        }
    }

    @Override
    public void assignUserToGroup(String username, String groupId) {
        try {
            UsersResource usersResource = keyCloakProvider.getRealmResource().users();

            List<UserRepresentation> users = usersResource.search(username);
            if (users.isEmpty()) {
                throw new ErrorAssignmentResourceKeycloak("User not found");
            }

            UserRepresentation userRepresentation = users.get(0);
            String userId = userRepresentation.getId();
            UserResource userResource = usersResource.get(userId);

            userResource.joinGroup(groupId);

            GroupResource groupResource = keyCloakProvider.getRealmResource().groups().group(groupId);
            GroupRepresentation representation = groupResource.toRepresentation();

            populateSubGroups(representation);
            List<GroupRepresentation> subGroups = representation.getSubGroups();

            for (GroupRepresentation subGroup : subGroups) {
                userResource.joinGroup(subGroup.getId());
            }

            log.info("User {} assigned to group {} and its subgroups", username, groupId);
        } catch (Exception e) {
            log.error("Error assigning user to group and subgroups", e);
            throw new ErrorAssignmentResourceKeycloak("Error assigning user to group and subgroups");
        }
    }

    @Override
    public void assignRoleToUser(String userId, String roleName) {
        try {
            UsersResource usersResource = keyCloakProvider.getRealmResource().users();
            UserResource userResource = usersResource.get(userId);
            RolesResource rolesResource = keyCloakProvider.getRealmResource().roles();
            RoleResource roleResource = rolesResource.get(roleName);
            RoleRepresentation roleRepresentation = roleResource.toRepresentation();

            userResource.roles().realmLevel().add(List.of(roleRepresentation));
        } catch (Exception e) {
            log.error("Error assigning role to user", e);
            throw new ErrorAssignmentResourceKeycloak("Error assigning role to user");
        }
    }

    @Override
    public void removeRolesFromGroup(String groupId, List<String> roleNames) {
        try {
            GroupResource groupResource = keyCloakProvider.getRealmResource().groups().group(groupId);
            List<RoleRepresentation> rolesToRemove = keyCloakProvider.getRealmResource().roles()
                    .list().stream()
                    .filter(role -> roleNames.contains(role.getName()))
                    .toList();

            groupResource.roles().realmLevel().remove(rolesToRemove);
        } catch (Exception e) {
            log.error("Error removing roles from group", e);
            throw new ErrorAssignmentResourceKeycloak("Error removing roles from group");
        }
    }

    @Override
    public void removeRoleFromUser(String userId, String roleName) {
        try {
            UserResource userResource = keyCloakProvider.getRealmResource().users().get(userId);
            RoleRepresentation roleRepresentation = keyCloakProvider.getRealmResource().roles().get(roleName).toRepresentation();

            userResource.roles().realmLevel().remove(List.of(roleRepresentation));
        } catch (Exception e) {
            log.error("Error removing role from user", e);
            throw new ErrorAssignmentResourceKeycloak("Error removing role from user");
        }
    }

    @Override
    public void removeUserFromGroup(String userId, String groupId) {
        try {
            UserResource userResource = keyCloakProvider.getRealmResource().users().get(userId);

            userResource.leaveGroup(groupId);
        } catch (Exception e) {
            log.error("Error removing user from group", e);
            throw new ErrorAssignmentResourceKeycloak("Error removing user from group");
        }
    }

    @Override
    public void deleteRole(String roleName) {
        try {
            // Protege los roles por defecto
            if ("ROLE_ADMIN".equals(roleName) || "ROLE_USER".equals(roleName)) {
                throw new ErrorCreateResourceKeycloak("Cannot delete default role: " + roleName);
            }

            RolesResource rolesResource = keyCloakProvider.getRealmResource().roles();
            rolesResource.deleteRole(roleName);
        } catch (Exception e) {
            log.error("Error deleting role", e);
            throw new ErrorCreateResourceKeycloak("Error deleting role");
        }
    }

    @Override
    public void deleteGroup(String groupId) {
        try {
            // Protege los grupos por defecto
            if ("default-group-id".equals(groupId)) {
                throw new ErrorCreateResourceKeycloak("Cannot delete default group");
            }

            GroupsResource groupsResource = keyCloakProvider.getRealmResource().groups();
            groupsResource.group(groupId).remove();
        } catch (Exception e) {
            log.error("Error deleting group", e);
            throw new ErrorCreateResourceKeycloak("Error deleting group");
        }
    }

    @Override
    public List<RoleRepresentation> getRolesByGroup(String groupId) {
        try {
            GroupResource groupResource = keyCloakProvider.getRealmResource().groups().group(groupId);
            return groupResource.roles().realmLevel().listAll();
        } catch (Exception e) {
            log.error("Error getting roles for group: {}", groupId, e);
            throw new ErrorGetResourceKeycloak("Error getting roles for group: " + groupId);
        }
    }

    @Override
    public Boolean updateGroupName(String groupId, GroupKeycloakRequestDTO newName) {
        try {
            GroupResource groupResource = keyCloakProvider.getRealmResource().groups().group(groupId);
            GroupRepresentation groupRepresentation = groupResource.toRepresentation();

            groupRepresentation.setName(newName.getName());
            groupRepresentation.setPath("/".concat(newName.getName()));
            groupResource.update(groupRepresentation);
            log.info("Group name updated successfully: {}", newName.getName());

            return true;
        } catch (Exception e) {
            log.error("Error updating group name for groupId: {}", groupId, e);
            throw new ErrorCreateResourceKeycloak("Error updating group name for groupId: " + groupId);
        }
    }

    @Override
    public GroupRepresentation createSubGroup(String parentGroupId, GroupKeycloakRequestDTO subGroup) {
        try {
            GroupResource parentGroupResource = keyCloakProvider.getRealmResource().groups().group(parentGroupId);
            GroupRepresentation subGroupRepresentation = new GroupRepresentation();

            subGroupRepresentation.setName(subGroup.getName());
            subGroupRepresentation.setPath(subGroup.getPath());
            parentGroupResource.subGroup(subGroupRepresentation);

            GroupRepresentation updatedGroup = parentGroupResource.toRepresentation();

            populateSubGroups(updatedGroup);
            return updatedGroup.getSubGroups().stream().filter(groupRepresentation -> groupRepresentation.getName().equals(subGroup.getName())).toList().stream().findFirst().get();

        } catch (Exception e) {
            log.error("Error creating subgroup for parentGroupId: {}", parentGroupId, e);
            throw new ErrorCreateResourceKeycloak("Error creating subgroup for parentGroupId: " + parentGroupId);
        }
    }

    @Override
    public void moveSubGroup(String subGroupId, String newParentGroupId) {
        try {
            GroupResource subGroup = keyCloakProvider.getRealmResource().groups().group(subGroupId);
            GroupRepresentation subGroupRepresentation = subGroup.toRepresentation();
            GroupResource newParenGroup = keyCloakProvider.getRealmResource().groups().group(newParentGroupId);

            newParenGroup.subGroup(subGroupRepresentation);
            log.info("Subgroup {} moved from to new parent group {}", subGroupId, newParentGroupId);
        } catch (Exception e) {
            log.error("Error moving subgroup {} from to new parent group {}", subGroupId, newParentGroupId, e);
            throw new ErrorCreateResourceKeycloak("Error moving subgroup");
        }
    }

    private UserRepresentation getUserRepresentation(UserPreRegisterDto userPreRegisterDto) {
        UserRepresentation userRepresentation = new UserRepresentation();

        userRepresentation.setFirstName(userPreRegisterDto.getFirstName());
        userRepresentation.setLastName(userPreRegisterDto.getSurname());
        userRepresentation.setUsername(userPreRegisterDto.getUserName());
        userRepresentation.setEmail(userPreRegisterDto.getEmail());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);
        userRepresentation.setRequiredActions(List.of());
        return userRepresentation;
    }

    private List<CredentialRepresentation> getCredentialRepresentations(String password) {
        CredentialRepresentation credentials = new CredentialRepresentation();

        credentials.setTemporary(false);
        credentials.setType(OAuth2Constants.PASSWORD);
        credentials.setValue(password);
        return Collections.singletonList(credentials);
    }

    @Override
    public void updateEmailUser(UserPreRegisterDto currentUser, String newEmail) {
        try {
            UsersResource usersResource = keyCloakProvider.getUserResource();
            UserRepresentation userRepresentationDeprecated = getUserRepresentation(currentUser);

            UserRepresentation userKeycloak = usersResource.searchByEmail(userRepresentationDeprecated.getEmail(),true ).stream().findFirst().get();
            userKeycloak.setEmail(newEmail);
            userKeycloak.setEmailVerified(true);
            userKeycloak.setEnabled(true);
            UserResource userResource = usersResource.get(userKeycloak.getId());
            userResource.update(userKeycloak);
        } catch (Exception e) {
            String message = e.getMessage().equals(Constant.USER_NOT_FOUND) ? Constant.USER_NOT_FOUND : Constant.ERROR_UPDATE_USER_KEYCLOAK;
            throw new ErrorUpdateUserKeycloak(message);
        }
    }
}
