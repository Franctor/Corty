package com.corty.backend.mapper;

import com.corty.backend.dto.PlayerAdminResponse;
import com.corty.backend.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id",       source = "idPlayer")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email",    source = "user.email")
    @Mapping(target = "gender",   expression = "java(player.getGender() != null ? player.getGender().name() : null)")
    @Mapping(target = "cityId",   source = "city.idCity")
    @Mapping(target = "city",     source = "city.label")
    @Mapping(target = "province", source = "city.province.label")
    PlayerAdminResponse toAdminResponse(Player player);

    List<PlayerAdminResponse> toAdminResponseList(List<Player> players);
}
