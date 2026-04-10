package com.corty.backend.controller;

import com.corty.backend.dto.UserAdminResponse;
import com.corty.backend.dto.UserRoleRequest;
import com.corty.backend.dto.UserStatusRequest;
import com.corty.backend.mapper.UserMapper;
import com.corty.backend.model.Authority;
import com.corty.backend.model.Role;
import com.corty.backend.repository.AuthorityRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.corty.backend.model.User;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles(@AuthenticationPrincipal User principal) {
        boolean isSuperadmin = principal.getRole() != null && "SUPERADMIN".equals(principal.getRole().getName());
        return ResponseEntity.ok(roleRepository.findAll().stream()
                .map(Role::getName)
                .filter(name -> isSuperadmin || !"SUPERADMIN".equals(name))
                .sorted()
                .toList());
    }

    @GetMapping("/authorities")
    public ResponseEntity<List<String>> getAuthorities() {
        return ResponseEntity.ok(authorityRepository.findAll().stream().map(Authority::getName).sorted().toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<UserAdminResponse> updateRoleAndAuthorities(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request,
            @AuthenticationPrincipal User principal) {
        boolean isSuperadmin = principal.getRole() != null && "SUPERADMIN".equals(principal.getRole().getName());
        if (!isSuperadmin && "SUPERADMIN".equals(request.getRole())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.updateRoleAndAuthorities(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<UserAdminResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
