package com.corty.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class UserRoleRequest {
    @NotBlank
    private String role;
    private List<String> authorities;
}
