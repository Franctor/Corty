package com.corty.backend.controller;

import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.LoginRequest;
import com.corty.backend.dto.RegisterPlayerRequest;
import com.corty.backend.exception.UserAlreadyExistsException;
import com.corty.backend.model.enums.Gender;
import com.corty.backend.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(AuthControllerTest.MockConfig.class)
@DisplayName("AuthController — login y registro")
class AuthControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("TC-C01: POST /api/auth/login con credenciales válidas → 200 con token")
    void login_valid_returns_200_with_token() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("jugador1");
        request.setPassword("Passw0rd!");

        when(authService.login(any())).thenReturn(AuthResponse.builder().token("jwt-abc").build());

        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-abc"));
    }

    @Test
    @DisplayName("TC-C02: POST /api/auth/login con body vacío → 400 Bad Request")
    void login_empty_body_returns_400() throws Exception {
        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-C03: POST /api/auth/register con datos válidos → 200")
    void register_valid_returns_200() throws Exception {
        RegisterPlayerRequest request = RegisterPlayerRequest.builder()
                .username("jugador1")
                .email("jugador@test.com")
                .password("Passw0rd!")
                .name("Juan")
                .surname("García")
                .phone("612345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 20))
                .cityId(1L)
                .build();

        when(authService.register(any())).thenReturn(AuthResponse.builder().build());

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-C04: POST /api/auth/register con username duplicado → 409 Conflict")
    void register_duplicate_username_returns_409() throws Exception {
        RegisterPlayerRequest request = RegisterPlayerRequest.builder()
                .username("jugador1")
                .email("jugador@test.com")
                .password("Passw0rd!")
                .name("Juan")
                .surname("García")
                .phone("612345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 20))
                .cityId(1L)
                .build();

        when(authService.register(any()))
                .thenThrow(new UserAlreadyExistsException("El nombre de usuario ya está en uso"));

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-C05: POST /api/auth/register sin email → 400 con mensaje de validación")
    void register_missing_email_returns_400() throws Exception {
        RegisterPlayerRequest request = RegisterPlayerRequest.builder()
                .username("jugador1")
                .password("Passw0rd!")
                .name("Juan")
                .surname("García")
                .phone("612345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 20))
                .cityId(1L)
                .build();

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
