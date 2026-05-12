package com.corty.backend.controller;

import com.corty.backend.dto.*;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.services.BookingResultService;
import com.corty.backend.services.BookingService;
import com.corty.backend.services.CourtAvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.corty.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController — endpoints REST")
class BookingControllerTest {

    @Mock
    BookingService bookingService;
    @Mock
    BookingResultService bookingResultService;
    @Mock
    CourtAvailabilityService courtAvailabilityService;

    @InjectMocks
    BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockUser = User.builder()
                .idUser(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        var sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("TC-C06: POST /api/bookings → 201 con bookingId")
    void createBooking_returns_201_with_id() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PRIVATE);
        request.setPaymentMethod(PaymentMethod.CASH);

        when(bookingService.createBooking(any(), eq("testuser")))
                .thenReturn(new BookingCreateResponse(42L));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(42));
    }

    @Test
    @DisplayName("TC-C07: POST /api/bookings con karma bajo → 400 Bad Request")
    void createBooking_low_karma_returns_400() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PUBLIC);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setSplitPayment(true);

        when(bookingService.createBooking(any(), any()))
                .thenThrow(new BusinessLogicException("Tu karma es demasiado bajo para crear partidos públicos"));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-C08: DELETE /api/bookings/{id} → 200 con respuesta de cancelación")
    void cancelBooking_returns_200_with_response() throws Exception {
        CancellationResponse cancellationResponse = CancellationResponse.builder()
                .message("La reserva ha sido cancelada")
                .karmaDeducted(0)
                .karmaRemaining(80)
                .refundInfo("Reembolso completo")
                .build();

        when(bookingService.cancelBooking(eq(10L), eq("testuser")))
                .thenReturn(cancellationResponse);

        mockMvc.perform(delete("/api/bookings/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.karmaDeducted").value(0))
                .andExpect(jsonPath("$.message").value("La reserva ha sido cancelada"));
    }

    @Test
    @DisplayName("TC-C09: GET /api/bookings/next sin reservas → 204 No Content")
    void getNextBooking_no_booking_returns_204() throws Exception {
        when(bookingService.getNextBooking("testuser")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/bookings/next"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("TC-C10: DELETE /api/bookings/{id}/leave → 200 con penalización karma")
    void leaveBooking_returns_200_with_karma_penalty() throws Exception {
        CancellationResponse leaveResponse = CancellationResponse.builder()
                .message("Has abandonado la reserva")
                .karmaDeducted(15)
                .karmaRemaining(65)
                .refundInfo("Sin reembolso")
                .build();

        when(bookingService.leaveBooking(eq(10L), eq("testuser")))
                .thenReturn(leaveResponse);

        mockMvc.perform(delete("/api/bookings/10/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.karmaDeducted").value(15));
    }
}
