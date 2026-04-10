package com.corty.backend.mapper;

import com.corty.backend.dto.UserAdminResponse;
import com.corty.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "idUser")
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "authorities", expression = "java(user.getExtraAuthorities() == null ? java.util.List.of() : user.getExtraAuthorities().stream().map(com.corty.backend.model.Authority::getName).sorted().toList())")
    UserAdminResponse toAdminResponse(User user);

    List<UserAdminResponse> toAdminResponseList(List<User> users);
}
