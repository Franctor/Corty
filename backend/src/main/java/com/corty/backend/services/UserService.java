package com.corty.backend.services;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.corty.backend.dto.UserAdminResponse;
import com.corty.backend.dto.UserRoleRequest;
import com.corty.backend.dto.UserStatusRequest;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.UserMapper;
import com.corty.backend.model.Authority;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.repository.AuthorityRepository;
import com.corty.backend.repository.ConversationRepository;
import com.corty.backend.repository.MessageRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final EmailService emailService;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    private static final java.util.Map<String, String> USER_SORT_MAP = java.util.Map.of(
        "username", "username",
        "email", "email",
        "role", "role.name",
        "creationDate", "creationDate"
    );

    public Page<UserAdminResponse> getAll(int page, int size, String search, String sort, String dir) {
        String sortField = USER_SORT_MAP.getOrDefault(sort, "username");
        var direction = "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        var pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortField));
        return userRepository.findAllFiltered(search, pageable)
                .map(userMapper::toAdminResponse);
    }

    public UserAdminResponse getById(Long id) {
        return userMapper.toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public UserAdminResponse updateStatus(Long id, UserStatusRequest request) {
        User user = findOrThrow(id);
        if (isSuperadmin(user)) {
            throw new EntityInUseException("No se puede modificar este usuario");
        }

        boolean wasEnabled = user.isEnabled();
        boolean wasLocked = user.isLocked();

        user.setEnabled(request.getEnabled());
        user.setLocked(request.getLocked());
        UserAdminResponse saved = userMapper.toAdminResponse(userRepository.save(user));

        boolean nowDisabled = wasEnabled && !request.getEnabled();
        boolean nowEnabled = !wasEnabled && request.getEnabled();
        boolean nowLocked = !wasLocked && request.getLocked();
        boolean nowUnlocked = wasLocked && !request.getLocked();

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
        if (isSuperadmin(user)) {
            throw new EntityInUseException("No se puede modificar este usuario");
        }

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
        if (isSuperadmin(user)) {
            throw new EntityInUseException("No se puede eliminar al superadministrador");
        }

        boolean hasActiveBookings = user.getBookings().stream().anyMatch(b
                -> (b.getBookingStatus() == BookingStatus.CONFIRMED
                || b.getBookingStatus() == BookingStatus.PENDING_PAYMENT)
                && !b.getDate().isBefore(LocalDate.now())
        );
        if (hasActiveBookings) {
            throw new EntityInUseException("No se puede eliminar el usuario porque tiene reservas activas o futuras");
        }

        messageRepository.detachSender(id);

        conversationRepository.findAllByParticipantId(id).forEach(conv -> {
            conv.getParticipants().remove(user);
            if (conv.getParticipants().isEmpty()) {
                conversationRepository.delete(conv);
            } else {
                conversationRepository.save(conv);
            }
        });

        playerRepository.findByUser_IdUser(id).ifPresent(player -> {
            player.setName("Usuario");
            player.setSurname("eliminado");
            player.setPhone(null);
            player.setBiography(null);
            player.setAvatarUrl(null);
            player.setBirthDate(null);
            player.setGender(null);
            player.setPublicProfile(false);
            player.setStripeCustomerId(null);
            player.setDefaultPaymentMethodId(null);
            playerRepository.save(player);
        });

        organizationRepository.findByUser_IdUser(id).ifPresent(org -> {
            user.setOrganization(null);
            organizationRepository.delete(org);
        });

        String anon = UUID.randomUUID().toString();
        user.setUsername("deleted_" + anon);
        user.setEmail("deleted_" + anon + "@corty.invalid");
        user.setPassword("");
        user.setEnabled(false);
        user.setLocked(true);
        user.setRole(null);
        user.getExtraAuthorities().clear();
        user.getSentRequests().clear();
        user.getReceivedRequests().clear();
        if (user.getActivationToken() != null) {
            user.setActivationToken(null);
        }
        userRepository.save(user);
    }

    private boolean isSuperadmin(User user) {
        return user.getRole() != null && "SUPERADMIN".equals(user.getRole().getName());
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
