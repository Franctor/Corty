package com.corty.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PlayerAdminResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private String biography;
    private String avatarUrl;
    private Integer karma;
    private Long cityId;
    private String city;
    private String province;
}
