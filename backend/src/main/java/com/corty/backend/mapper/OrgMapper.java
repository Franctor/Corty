package com.corty.backend.mapper;

import com.corty.backend.dto.OrgAdminResponse;
import com.corty.backend.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrgMapper {

    @Mapping(target = "id",           source = "idOrganization")
    @Mapping(target = "username",     source = "user.username")
    @Mapping(target = "email",        source = "user.email")
    @Mapping(target = "cityId",       source = "fiscalCity.idCity")
    @Mapping(target = "city",         source = "fiscalCity.label")
    @Mapping(target = "province",     source = "fiscalCity.province.label")
    OrgAdminResponse toAdminResponse(Organization organization);

    List<OrgAdminResponse> toAdminResponseList(List<Organization> organizations);
}
