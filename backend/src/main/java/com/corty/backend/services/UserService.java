package com.corty.backend.services;

import com.corty.backend.dto.UserAdminResponse;
import com.corty.backend.dto.UserRoleRequest;
import com.corty.backend.dto.UserStatusRequest;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.UserMapper;
import com.corty.backend.model.Authority;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.repository.AuthorityRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    public List<UserAdminResponse> getAll() {
        return userMapper.toAdminResponseList(userRepository.findAll());
    }

    public UserAdminResponse getById(Long id) {
        return userMapper.toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public UserAdminResponse updateStatus(Long id, UserStatusRequest request) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) throw new EntityInUseException("No se puede modificar este usuario");
        user.setEnabled(request.getEnabled());
        user.setLocked(request.getLocked());
        return userMapper.toAdminResponse(userRepository.save(user));
    }

    @Transactional
    public UserAdminResponse updateRoleAndAuthorities(Long id, UserRoleRequest request) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) throw new EntityInUseException("No se puede modificar este usuario");

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.getRole()));
        user.setRole(role);

        Set<Authority> authorities = new HashSet<>();
        if (request.getAuthorities() != null) {
            for (String name : request.getAuthorities()) {
                Authority a = authorityRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Authority no encontrada: " + name));
                authorities.add(a);
            }
        }
        user.setExtraAuthorities(authorities);

        return userMapper.toAdminResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) throw new EntityInUseException("No se puede eliminar este usuario");
        if (!user.getBookings().isEmpty()) {
            throw new EntityInUseException("No se puede eliminar el usuario porque tiene reservas asociadas");
        }
        userRepository.delete(user);
    }

    private boolean isSuperadmin(User user) {
        return user.getRole() != null && "SUPERADMIN".equals(user.getRole().getName());
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
