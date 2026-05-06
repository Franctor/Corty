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
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.corty.backend.model.enums.BookingStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final EmailService emailService;
    private final PlayerRepository playerRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final OrganizationRepository organizationRepository;

    public Page<UserAdminResponse> getAll(int page, int size, String search) {
        return userRepository.findAllFiltered(search, PageRequest.of(page, size))
                .map(userMapper::toAdminResponse);
    }

    public UserAdminResponse getById(Long id) {
        return userMapper.toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public UserAdminResponse updateStatus(Long id, UserStatusRequest request) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) throw new EntityInUseException("No se puede modificar este usuario");

        boolean wasEnabled = user.isEnabled();
        boolean wasLocked  = user.isLocked();

        user.setEnabled(request.getEnabled());
        user.setLocked(request.getLocked());
        UserAdminResponse saved = userMapper.toAdminResponse(userRepository.save(user));

        boolean nowDisabled  = wasEnabled  && !request.getEnabled();
        boolean nowEnabled   = !wasEnabled && request.getEnabled();
        boolean nowLocked    = !wasLocked  && request.getLocked();
        boolean nowUnlocked  = wasLocked   && !request.getLocked();

        if (nowDisabled) {
            emailService.sendAccountDisabled(user.getEmail(), user.getUsername(), request.getReason());
        } else if (nowLocked) {
            emailService.sendAccountLocked(user.getEmail(), user.getUsername(), request.getReason());
        } else if (nowEnabled) {
            emailService.sendAccountEnabled(user.getEmail(), user.getUsername());
        } else if (nowUnlocked) {
            emailService.sendAccountUnlocked(user.getEmail(), user.getUsername());
        }

        return saved;
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
                Authority authority = authorityRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Authority no encontrada: " + name));
                authorities.add(authority);
            }
        }
        user.setExtraAuthorities(authorities);

        return userMapper.toAdminResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) throw new EntityInUseException("No se puede eliminar al superadministrador");

        boolean hasActiveBookings = user.getBookings().stream().anyMatch(b ->
                (b.getBookingStatus() == BookingStatus.CONFIRMED ||
                 b.getBookingStatus() == BookingStatus.PENDING_PAYMENT) &&
                !b.getDate().isBefore(LocalDate.now())
        );
        if (hasActiveBookings) {
            throw new EntityInUseException("No se puede eliminar el usuario porque tiene reservas activas o futuras");
        }

        playerRepository.findByUser_IdUser(id).ifPresent(player -> {
            playerBookingRepository.deleteAllByPlayerId(player.getIdPlayer());
            user.setPlayer(null);
            playerRepository.delete(player);
        });
        organizationRepository.findByUser_IdUser(id).ifPresent(org -> {
            user.setOrganization(null);
            organizationRepository.delete(org);
        });

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
