package com.corty.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cortyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Corty API")
                        .description("API REST de la plataforma de reserva de pistas deportivas Corty")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Corty")
                                .email("fcastor2706@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Auth").description("Registro, login y activación de cuenta"),
                        new Tag().name("Bookings").description("Reservas de pistas"),
                        new Tag().name("Courts").description("Gestión de pistas"),
                        new Tag().name("Court Schedule").description("Horarios y bloqueos de pistas"),
                        new Tag().name("Clubs").description("Gestión de clubs"),
                        new Tag().name("Organizations").description("Gestión de organizaciones"),
                        new Tag().name("Players").description("Perfil y karma de jugadores"),
                        new Tag().name("Users").description("Administración de usuarios"),
                        new Tag().name("Sports").description("Deportes y superficies"),
                        new Tag().name("Social").description("Amistades y chat"),
                        new Tag().name("Notifications").description("Notificaciones push"),
                        new Tag().name("Payments").description("Métodos de pago y webhooks Stripe"),
                        new Tag().name("Dashboard").description("Estadísticas del panel de administración"),
                        new Tag().name("Media").description("Subida de imágenes"),
                        new Tag().name("Location").description("Búsqueda de localización")));
    }
}
