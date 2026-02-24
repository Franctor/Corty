package com.corty.backend.config;

import com.corty.backend.model.*;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.RegionRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityRepository cityRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (regionRepository.count() == 0) {
            loadLocationData();
        }
        if (userRepository.count() == 0) {
            loadUserPruebas();
        }
    }

    private void loadUserPruebas() {
        Role playerRole = roleRepository.findByName("PLAYER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("PLAYER").build()
                ));

        // 2. Buscar una ciudad de tu tabla 'cities' (Cargada desde el JSON)
        // Buscamos, por ejemplo, Burgos (código 09059)
        City burgos = cityRepository.findByCode("09059")
                .orElse(cityRepository.findAll().get(0)); // Si no, la primera que haya

        // 3. Crear el Usuario de prueba
        User userPrueba1 = User.builder()
                .username("1")
                .email("1")
                .password(passwordEncoder.encode("1"))
                .role(playerRole)
                .enabled(true)
                .creationDate(LocalDateTime.now())
                .build();

        User userPrueba2 = User.builder()
                .username("2")
                .email("2")
                .password(passwordEncoder.encode("2"))
                .role(playerRole)
                .enabled(true)
                .creationDate(LocalDateTime.now())
                .build();
        userRepository.save(userPrueba1);
        userRepository.save(userPrueba2);

        System.out.println("✅ Usuario de prueba 'corty' creado con contraseña '1234'");
    }

    private void loadLocationData() throws IOException {
        InputStream inputStream = new ClassPathResource("data/regions.json").getInputStream();
        List<Map<String, Object>> data = objectMapper.readValue(inputStream, new TypeReference<>() {
        });
        for (Map<String, Object> cMap : data) {
            Region region = Region.builder()
                    .code(String.valueOf(cMap.get("code")))
                    .label((String) cMap.get("label"))
                    .provinces(new ArrayList<>())
                    .build();

            List<Map<String, Object>> pList = (List<Map<String, Object>>) cMap.get("provinces");
            for (Map<String, Object> pMap : pList) {
                Province province = Province.builder()
                        .code(String.valueOf(pMap.get("code")))
                        .label((String) pMap.get("label"))
                        .region(region)
                        .cities(new ArrayList<>())
                        .build();

                List<Map<String, Object>> tList = (List<Map<String, Object>>) pMap.get("towns");
                for (Map<String, Object> tMap : tList) {
                    City city = City.builder()
                            .code(String.valueOf(tMap.get("code")))
                            .label((String) tMap.get("label"))
                            .province(province)
                            .build();
                    province.getCities().add(city);
                }
                region.getProvinces().add(province);
            }
            regionRepository.save(region);
        }
    }
}
