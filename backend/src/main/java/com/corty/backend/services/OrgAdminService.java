package com.corty.backend.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.corty.backend.dto.OrgAdminCreateRequest;
import com.corty.backend.dto.OrgAdminRequest;
import com.corty.backend.dto.OrgAdminResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.OrgMapper;
import com.corty.backend.model.City;
import com.corty.backend.model.Organization;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgAdminService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final RoleRepository roleRepository;
    private final OrgMapper orgMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;

    @Transactional
    public OrgAdminResponse create(OrgAdminCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EntityInUseException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityInUseException("El email ya está registrado");
        }
        if (organizationRepository.existsByCif(request.getCif())) {
            throw new EntityInUseException("El CIF ya está registrado");
        }
        if (organizationRepository.existsByBusinessName(request.getBusinessName())) {
            throw new EntityInUseException("La razón social ya está registrada");
        }

        Role role = roleRepository.findByName("ORGANIZATION")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ORGANIZATION no encontrado"));

        boolean verified = Boolean.TRUE.equals(request.getVerified());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(verified)
                .build();
        userRepository.save(user);

        City city = request.getCityId() != null
                ? cityRepository.findById(request.getCityId()).orElse(null)
                : null;

        Organization organization = Organization.builder()
                .businessName(request.getBusinessName())
                .cif(request.getCif())
                .fiscalCity(city)
                .user(user)
                .build();
        organizationRepository.save(organization);

        if (!verified) {
            activationService.createAndSend(user);
        } else {
            activationService.sendWelcomeIfVerified(user);
        }

        return orgMapper.toAdminResponse(organization);
    }

    public List<OrgAdminResponse> createBatch(MultipartFile file) {
        Role orgRole = roleRepository.findByName("ORGANIZATION")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ORGANIZATION no encontrado"));

        List<OrgAdminResponse> createdOrgs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> dataLines = reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            for (int index = 0; index < dataLines.size(); index++) {
                String[] columns = dataLines.get(index).split(",", -1);
                String email = columns[0].trim();
                String baseUsername = columns.length > 1 && !columns[1].isBlank() ? columns[1].trim() : email.split("@")[0];
                String password = columns.length > 2 && !columns[2].isBlank() ? columns[2].trim() : UUID.randomUUID().toString();
                String businessName = columns.length > 3 ? columns[3].trim() : "";
                String cif = columns.length > 4 ? columns[4].trim() : "";
                boolean isVerified = columns.length > 5 && "true".equalsIgnoreCase(columns[5].trim());

                boolean alreadyExists = userRepository.existsByEmail(email)
                        || (!cif.isBlank() && organizationRepository.existsByCif(cif))
                        || (!businessName.isBlank() && organizationRepository.existsByBusinessName(businessName));

                if (alreadyExists) {
                    log.warn("CSV línea {}: organización '{}' ya existe, se omite", index + 2, email);
                } else {
                    try {
                        String username = resolveUniqueUsername(baseUsername);
                        OrgAdminResponse created = createSingleFromBatch(
                                orgRole, email, username, password, businessName, cif, isVerified);
                        createdOrgs.add(created);
                    } catch (DataIntegrityViolationException exception) {
                        log.warn("CSV línea {}: conflicto de datos para '{}', se omite", index + 2, email);
                    }
                }
            }
        } catch (EntityInUseException entityInUseException) {
            throw entityInUseException;
        } catch (Exception exception) {
            throw new EntityInUseException("Error procesando CSV: " + exception.getMessage());
        }
        return createdOrgs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrgAdminResponse createSingleFromBatch(Role orgRole, String email, String username,
            String password, String businessName, String cif, boolean isVerified) {
        User newUser = userRepository.save(User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .role(orgRole).enabled(isVerified)
                .build());

        Organization newOrg = organizationRepository.save(Organization.builder()
                .businessName(businessName.isBlank() ? null : businessName)
                .cif(cif.isBlank() ? null : cif)
                .user(newUser)
                .build());

        if (!isVerified) {
            activationService.createAndSend(newUser);
        } else {
            activationService.sendWelcomeIfVerified(newUser);
        }
        return orgMapper.toAdminResponse(newOrg);
    }

    public List<OrgAdminResponse> getAll() {
        return orgMapper.toAdminResponseList(organizationRepository.findAll());
    }

    public OrgAdminResponse getById(Long id) {
        return orgMapper.toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public OrgAdminResponse update(Long id, OrgAdminRequest request) {
        Organization organization = findOrThrow(id);
        organization.setBusinessName(request.getBusinessName());
        organization.setCif(request.getCif());
        if (request.getCityId() != null) {
            organization.setFiscalCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada")));
        } else {
            organization.setFiscalCity(null);
        }
        return orgMapper.toAdminResponse(organizationRepository.save(organization));
    }

    private String resolveUniqueUsername(String base) {
        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private Organization findOrThrow(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"));
    }
}
