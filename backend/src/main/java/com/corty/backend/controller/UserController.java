package com.corty.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corty.backend.dto.UserAdminResponse;
import com.corty.backend.dto.UserRoleRequest;
import com.corty.backend.dto.UserStatusRequest;
import com.corty.backend.model.Authority;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.repository.AuthorityRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users")
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @GetMapping
    public ResponseEntity<Page<UserAdminResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(userService.getAll(page, size, search));
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
        boolean callerIsSuperadmin = principal.getRole() != null && "SUPERADMIN".equals(principal.getRole().getName());
        boolean attemptingToAssignSuperadmin = "SUPERADMIN".equals(request.getRole());
        final ResponseEntity<UserAdminResponse> response;
        if (!callerIsSuperadmin && attemptingToAssignSuperadmin) {
            response = ResponseEntity.status(403).build();
        } else {
            response = ResponseEntity.ok(userService.updateRoleAndAuthorities(id, request));
        }
        return response;
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
