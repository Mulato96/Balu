package com.gal.afiliaciones.infrastructure.controller.keycloak;

import com.gal.afiliaciones.application.job.KeycloakScheduler;
import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.keycloak.GroupKeycloakRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.keycloak.RoleKeycloakRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/keycloak")
@RequiredArgsConstructor
public class KeycloakController {

    private final KeycloakService service;
    private final KeycloakScheduler keycloakScheduler;

    @GetMapping("/roles")
    public ResponseEntity<BodyResponseConfig<List<RoleRepresentation>>> getAllRoles() {
        log.info("Get All roles");
        return ResponseEntity.ok(new BodyResponseConfig<>(service.getAllRoles(), "Get All roles"));
    }

    @GetMapping("/groups")
    public ResponseEntity<BodyResponseConfig<List<GroupRepresentation>>> getAllGroups() {
        log.info("Get All groups");
        return ResponseEntity.ok(new BodyResponseConfig<>(service.getAllGroups(), "Get All groups"));
    }

    @PutMapping("/assign-roles/{groupId}")
    public ResponseEntity<BodyResponseConfig<GroupRepresentation>> assignRolesToGroup(@PathVariable("groupId") String groupId, @RequestBody List<String> roleIds) {
        log.info("Assign roles to group {}", groupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(service.assignRolesToGroup(groupId, roleIds), "Assign roles to group"));
    }

    @PostMapping("/create/role")
    public ResponseEntity<BodyResponseConfig<RoleRepresentation>> createRole(@RequestBody RoleKeycloakRequestDTO role) {
        log.info("Create role {}", role);
        return ResponseEntity.ok(new BodyResponseConfig<>(service.createRole(role), "Create role"));
    }

    @PostMapping("/synchronize-profile")
    public ResponseEntity<BodyResponseConfig<Boolean>> synchronizeProfile() {
        log.info("synchronize profiles");
        return ResponseEntity.ok(new BodyResponseConfig<>(keycloakScheduler.setProfilesAndRoles(), "synchronize profiles"));
    }

    @PostMapping("/synchronize-permission")
    public ResponseEntity<BodyResponseConfig<Boolean>> synchronizePermission() {
        log.info("synchronize permissions");
        return ResponseEntity.ok(new BodyResponseConfig<>(keycloakScheduler.setPermission(), "synchronize permissions"));
    }

    @PostMapping("/create/group")
    public ResponseEntity<BodyResponseConfig<GroupRepresentation>> createGroup(@RequestBody GroupKeycloakRequestDTO group) {
        log.info("Create group {}", group);
        return ResponseEntity.ok(new BodyResponseConfig<>(service.createGroup(group), "Create group"));
    }

    @PutMapping("/group/{groupId}")
    public ResponseEntity<BodyResponseConfig<Boolean>> updateGroupName(@PathVariable("groupId") String groupId, @RequestBody GroupKeycloakRequestDTO newName) {
        log.info("Updating group name for groupId: {}", groupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(service.updateGroupName(groupId, newName), "Group name updated successfully"));
    }

    @PostMapping("/group/{parentGroupId}/subgroup")
    public ResponseEntity<BodyResponseConfig<GroupRepresentation>> createSubGroup(@PathVariable("parentGroupId") String parentGroupId, @RequestBody GroupKeycloakRequestDTO subGroup) {
        log.info("Creating subgroup for parentGroupId: {}", parentGroupId);
        GroupRepresentation createdSubGroup = service.createSubGroup(parentGroupId, subGroup);
        return ResponseEntity.ok(new BodyResponseConfig<>(createdSubGroup, "Subgroup created successfully"));
    }

    @PutMapping("/group/{subGroupId}/move-to/{newParentGroupId}")
    public ResponseEntity<BodyResponseConfig<Void>> moveSubGroup(
            @PathVariable("subGroupId") String subGroupId,
            @PathVariable("newParentGroupId") String newParentGroupId) {
        log.info("Moving subgroup {} from to group {}", subGroupId, newParentGroupId);
        service.moveSubGroup(subGroupId, newParentGroupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Subgroup moved successfully"));
    }

    @PutMapping("/user-to-group/{username}/{groupId}")
    public ResponseEntity<BodyResponseConfig<Void>> userToGroup(@PathVariable("username") String username, @PathVariable("groupId") String groupId) {
        log.info("user assign to group {}", groupId);
        service.assignUserToGroup(username, groupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "user assign to group"));
    }

    @PutMapping("/role-to-user/{userId}/{roleId}")
    public ResponseEntity<BodyResponseConfig<Void>> roleToUser(@PathVariable("userId") String userId, @PathVariable("roleId") String roleId) {
        log.info("user assign to role {}", roleId);
        service.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "user assign to group"));
    }

    @DeleteMapping("/remove-roles/{groupId}")
    public ResponseEntity<BodyResponseConfig<Void>> removeRolesFromGroup(@PathVariable("groupId") String groupId, @RequestBody List<String> roleNames) {
        log.info("Remove roles from group {}", groupId);
        service.removeRolesFromGroup(groupId, roleNames);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Remove roles from group"));
    }

    @DeleteMapping("/remove-role-from-user/{userId}/{roleName}")
    public ResponseEntity<BodyResponseConfig<Void>> removeRoleFromUser(@PathVariable("userId") String userId, @PathVariable("roleName") String roleName) {
        log.info("Remove role {} from user {}", roleName, userId);
        service.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Remove role from user"));
    }

    @DeleteMapping("/remove-user-from-group/{userId}/{groupId}")
    public ResponseEntity<BodyResponseConfig<Void>> removeUserFromGroup(@PathVariable("userId") String userId, @PathVariable("groupId") String groupId) {
        log.info("Remove user {} from group {}", userId, groupId);
        service.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Remove user from group"));
    }

    @DeleteMapping("/delete-role/{roleName}")
    public ResponseEntity<BodyResponseConfig<Void>> deleteRole(@PathVariable("roleName") String roleName) {
        log.info("Delete role {}", roleName);
        service.deleteRole(roleName);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Delete role"));
    }

    @DeleteMapping("/delete-group/{groupId}")
    public ResponseEntity<BodyResponseConfig<Void>> deleteGroup(@PathVariable("groupId") String groupId) {
        log.info("Delete group {}", groupId);
        service.deleteGroup(groupId);
        return ResponseEntity.ok(new BodyResponseConfig<>(null, "Delete group"));
    }
}
