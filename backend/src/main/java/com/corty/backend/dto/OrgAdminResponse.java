package com.corty.backend.dto;

import lombok.Data;

@Data
public class OrgAdminResponse {
    private Long id;
    private String username;
    private String email;
    private String businessName;
    private String cif;
    private Long cityId;
    private String city;
    private String province;
}
