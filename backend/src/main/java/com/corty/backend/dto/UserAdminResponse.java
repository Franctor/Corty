package com.corty.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserAdminResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private List<String> authorities;
    private boolean enabled;
    private boolean locked;
    private LocalDateTime creationDate;
}
